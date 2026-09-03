package com.uber.bg.uber.bg.Services;

import com.uber.bg.uber.bg.DTOs.LocationPingDTO;
import com.uber.bg.uber.bg.Entities.Ride;
import com.uber.bg.uber.bg.Enumerations.RIDE_STATUS;
import com.uber.bg.uber.bg.Repositories.Jpa.RideRepository;
import com.uber.bg.uber.bg.Repositories.Jpa.TempRideCoordinatesRepository;
import com.uber.bg.uber.bg.Repositories.Jpa.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.transaction.annotation.Transactional;


import java.net.ConnectException;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
public class DriverService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
   private final RedisTemplate<String, Object> redisTemplate;
   private final SimpMessagingTemplate simpMessagingTemplate;
  private final KafkaTemplate<String, String> kafkaTemplate;
   private final TempRideCoordinatesRepository tempRideCoordinatesRepository;


    @Autowired
    public DriverService(RideRepository rideRepository, RedisTemplate<String, Object> redisTemplate, UserRepository userRepository, SimpMessagingTemplate simpMessagingTemplate, KafkaTemplate<String, String> kafkaTemplate, TempRideCoordinatesRepository tempRideCoordinatesRepository) {
        this.rideRepository = rideRepository;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.tempRideCoordinatesRepository = tempRideCoordinatesRepository;
    }

    @Retryable(
            retryFor = {org.springframework.data.redis.RedisConnectionFailureException.class, ConnectException.class},
            maxAttempts = 4,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    @Transactional(readOnly = true)
    public List<Map<String, String>> getAllAvailableRides() {
   Set<Object> allAvailableRides = redisTemplate.opsForSet().members("rides:open");
   List<Ride> rides =
   Objects.requireNonNull(allAvailableRides).stream()
           .map(Object::toString)
           .map(UUID::fromString)
           .map(rideRepository::findById)
           .flatMap(Optional::stream)
           .toList();

            List<Map<String,String>> availableRides = new ArrayList<>();
            rides.forEach(x-> availableRides.add(Map.of(
                    "pickupLongitude", String.valueOf(x.getPickupLocation().getLongitude()),
                    "pickupLatitude", String.valueOf(x.getPickupLocation().getLatitude()),
                    "destinationLongitude", String.valueOf(x.getDestinationLocation().getLongitude()),
                    "destinationLatitude", String.valueOf(x.getDestinationLocation().getLatitude()),
                    "people", String.valueOf(x.getPeople()),
                    "rideId", x.getId().toString()
            )));
           return availableRides;
    }

    @Transactional
    public void acceptRide(final UUID rideId, final UUID driverId) {
        Ride ride = rideRepository.findByIdWithLock(rideId).orElseThrow(() -> new IllegalArgumentException("no ride with this id"));

        if (ride.getStatus() != RIDE_STATUS.REQUESTED) {
            throw new IllegalStateException("Ride has already been accepted or cancelled");
        }
        ride.setStatus(RIDE_STATUS.ACCEPTED);
        ride.setDriver(userRepository.findById(driverId).orElseThrow(() -> new IllegalArgumentException("no driver with this id")));

        redisTemplate.opsForHash().put("ride:"+ride.getId().toString(), "status", ride.getStatus().name());
        redisTemplate.opsForHash().put("ride:"+ride.getId().toString(), "driverId", driverId.toString());
        redisTemplate.delete("passenger:to:ride:"+ride.getPassenger().getId());
        redisTemplate.opsForSet().remove("rides:open", ride.getId().toString());
        rideRepository.saveAndFlush(ride);
    }

    @KafkaListener(topics = "driver-locations", groupId = "uber-core-group")
    public void streamLocation(@Header(value = "rideId", required = false) final String rideId,
                               @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) final String driverId,
                               @Payload final LocationPingDTO dto) {


        if (driverId == null || rideId == null || dto == null) {
            log.error("Kafka consumer received an invalid null payload stream sequence. " +
                    "Headers -> driverId: {}, rideId: {}. DTO present: {}", driverId, rideId, (dto != null));
            throw new IllegalArgumentException("no data provided");
        }

        log.trace("Location ingestion heartbeat from Kafka. Driver: {}, Ride: {}, Long: {}, Lat: {}",
                driverId, rideId, dto.getLongitude(), dto.getLatitude());

        String coordinates = dto.getLongitude() + "," + dto.getLatitude();
        String currentKey = "driver:current:stream:" + driverId;

        redisTemplate.opsForValue().set(currentKey, coordinates, Duration.ofHours(2));

        String wsDestination = "/topic/ride/" + rideId;
        simpMessagingTemplate.convertAndSend(wsDestination, coordinates);

        kafkaTemplate.send("ride-coordinates-db-store", rideId, dto.getLongitude() + "|" + dto.getLatitude());

    }

    @Transactional
    public void endRide(final UUID id){
        Ride ride = rideRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("ride with this id is not present in the database"));
        ride.setStatus(RIDE_STATUS.ARRIVED);

        rideRepository.save(ride);

        rideRepository.compileRouteHistoryToLineString(id);

        tempRideCoordinatesRepository.deleteByRideId(id);

        redisTemplate.delete("ride:history:coordinates:"+id.toString());
        redisTemplate.delete("ride:"+id.toString());
    }

    public void goOnline(final UUID id, final LocationPingDTO dto) {
        if (id == null || dto == null) {
            throw new IllegalArgumentException("Driver ID and complete coordinate parameters must be provided.");
        }

        String driverIdStr = id.toString();
        String hashKey = "driver:current:" + driverIdStr;
        String geoIndexKey = "drivers:active";

        log.info("Processing go-online initialization request for driverId: {} at Lng: {}, Lat: {}",
                driverIdStr, dto.getLongitude(), dto.getLatitude());

        // 1. Store rich structured status metadata inside a Redis Hash
        Map<String, String> driverProfileMap = Map.of(
                "status", "ONLINE",
                "longitude", String.valueOf(dto.getLongitude()),
                "latitude", String.valueOf(dto.getLatitude()),
                "updatedAt", java.time.Instant.now().toString()
        );

        redisTemplate.opsForHash().putAll(hashKey, driverProfileMap);

        redisTemplate.expire(hashKey, java.time.Duration.ofMinutes(30));

        redisTemplate.opsForGeo().add(
                geoIndexKey,
                new org.springframework.data.geo.Point(dto.getLongitude(), dto.getLatitude()),
                driverIdStr
        );

        log.debug("Successfully created hash index '{}' and updated geospatial matching ring cluster.", hashKey);
    }

    public void goOffline(final UUID driverId) {
        redisTemplate.opsForZSet().remove("drivers:active", driverId.toString());
        redisTemplate.opsForHash().put("driver:current:"+driverId.toString(),"status","OFFLINE");

    }


}
