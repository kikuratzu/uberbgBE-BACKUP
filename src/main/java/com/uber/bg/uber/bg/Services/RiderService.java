package com.uber.bg.uber.bg.Services;

import com.uber.bg.uber.bg.DTOs.LocationPingDTO;
import com.uber.bg.uber.bg.Entities.LocationPing;
import com.uber.bg.uber.bg.Entities.Ride;
import com.uber.bg.uber.bg.Entities.User;
import com.uber.bg.uber.bg.Enumerations.RIDE_STATUS;
import com.uber.bg.uber.bg.Repositories.Jpa.RideRepository;
import com.uber.bg.uber.bg.Repositories.Jpa.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.Utilities;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class RiderService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RideRepository rideRepository;
    private final RideMatchingService rideMatchingService;
    @Autowired
    public RiderService(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate, RideRepository rideRepository, RideMatchingService rideMatchingService)
    {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.rideRepository = rideRepository;
        this.rideMatchingService = rideMatchingService;
    }

@Transactional
    public void requestRide(final UUID id, final LocationPingDTO pickupDto, final LocationPingDTO destinationDto, final int people) {

        if (redisTemplate.hasKey("passenger:to:ride:"+id.toString())){
            throw new IllegalStateException("already requested a ride");
        }

        LocationPing pickup = new LocationPing();
        BeanUtils.copyProperties(pickupDto, pickup);
        LocationPing destination = new LocationPing();
        BeanUtils.copyProperties(destinationDto, destination);

        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("No passenger with this id"));

        Ride ride = Ride.builder()
                .passenger(user)
                .driver(null)
                .status(RIDE_STATUS.REQUESTED)
                .pickupLocation(pickup)
                .destinationLocation(destination)
                .people(people)
                .date(Instant.now())
                .build();

    ride = rideRepository.save(ride);

    Map<String, String> redisRideState = Map.of(
            "rideId", ride.getId().toString(),
            "passengerId", id.toString(),
            "driverId", "",
            "pickupLongitude", String.valueOf(pickup.getLongitude()),
            "pickupLatitude", String.valueOf(pickup.getLatitude()),
            "destinationLongitude", String.valueOf(destination.getLongitude()),
            "destinationLatitude", String.valueOf(destination.getLatitude()),
            "status", ride.getStatus().name(),
            "people", String.valueOf(people),
            "timestamp", ride.getDate().toString()
    );




    String redisKey = "ride:" + ride.getId().toString();
    redisTemplate.opsForHash().putAll(redisKey, redisRideState);
    redisTemplate.opsForValue().set("passenger:to:ride:"+ id.toString(), ride.getId().toString());
    redisTemplate.opsForSet().add("rides:open", ride.getId().toString());

    rideMatchingService.notifyThreeClosestDrivers(ride.getId(), ride.getPeople(), pickupDto.getLongitude(), pickupDto.getLatitude(), destinationDto.getLongitude(), destinationDto.getLatitude());
    }

    @Transactional
    public void CancelRide (final UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("No passenger with this id"));

        String rideIdStr = (String) redisTemplate.opsForValue().get("passenger:to:ride:" + id);
        if (rideIdStr == null) {
            throw new IllegalStateException("No active ride found for this passenger.");
        }

        UUID rideId = UUID.fromString(rideIdStr);

        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new IllegalArgumentException("no ride with this id"));
        ride.setStatus(RIDE_STATUS.CANCELED);

        redisTemplate.delete("ride:"+ride.getId().toString());
        redisTemplate.delete("passenger:to:ride:"+id);
        redisTemplate.opsForSet().remove("rides:open", ride.getId().toString());
        rideRepository.save(ride);
    }

    @Transactional
    public void rateRide(final UUID rideId) {

    }


}
