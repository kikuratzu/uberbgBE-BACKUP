package com.uber.bg.uber.bg.Services;

import com.uber.bg.uber.bg.DTOs.LocationPingDTO;
import com.uber.bg.uber.bg.Entities.Ride;
import com.uber.bg.uber.bg.Enumerations.RIDE_STATUS;
import com.uber.bg.uber.bg.Repositories.Jpa.RideRepository;
import com.uber.bg.uber.bg.Repositories.Jpa.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.transaction.annotation.Transactional;


import java.net.ConnectException;
import java.util.*;

@Slf4j
@Service
public class DriverService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
   private final RedisTemplate<String, Object> redisTemplate;
   private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public DriverService(RideRepository rideRepository, RedisTemplate<String, Object> redisTemplate, UserRepository userRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.rideRepository = rideRepository;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Retryable(
            retryFor = {org.springframework.data.redis.RedisConnectionFailureException.class, ConnectException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    @Transactional(readOnly = true)
    public List<Map<String, String>> getAllAvailableRides() {
   Set<Object> allAvailableRides = redisTemplate.opsForSet().members("rides:open");
   List<Ride> rides =
   allAvailableRides.stream()
           .map(Object::toString)
           .map(UUID::fromString)
           .map(rideRepository::findById)
           .flatMap(Optional::stream)
           .toList();

            List<Map<String,String>> availableRides = new ArrayList<>();
            rides.forEach(x-> availableRides.add(Map.of(
                    "location", x.getPickupLocation(),
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
        redisTemplate.delete("passenger:to:ride:"+ride.getPassenger().getId());
        redisTemplate.opsForSet().remove("rides:open", ride.getId().toString());
        rideRepository.saveAndFlush(ride);
    }

    @KafkaListener(topics = "driver-locations", groupId = "uber-core-group")
    public void streamLocation(@Header("rideId") final String rideId,
                               @Header(KafkaHeaders.RECEIVED_KEY) final String driverId,
                               @Payload final LocationPingDTO dto) {
        String coordinates = dto.getLongitude() + "," + dto.getLatitude();
        redisTemplate.opsForValue().set("driver:current:"+driverId, coordinates);
        redisTemplate.opsForList().rightPush("ride:location:history:"+rideId, coordinates);
        String wsDestination = "/topic/ride/" + rideId;
        simpMessagingTemplate.convertAndSend(wsDestination, coordinates);

    }


}
