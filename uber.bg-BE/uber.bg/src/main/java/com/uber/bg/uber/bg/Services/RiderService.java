package com.uber.bg.uber.bg.Services;

import com.uber.bg.uber.bg.Entities.Ride;
import com.uber.bg.uber.bg.Entities.User;
import com.uber.bg.uber.bg.Enumerations.RIDE_STATUS;
import com.uber.bg.uber.bg.Repositories.Jpa.RideRepository;
import com.uber.bg.uber.bg.Repositories.Jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class RiderService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RideRepository rideRepository;
    @Autowired
    public RiderService(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate, RideRepository rideRepository)
    {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.rideRepository = rideRepository;
    }

@Transactional
    public void requestRide(final UUID id, final String location) {

        if (redisTemplate.hasKey("passenger:to:ride:"+id.toString())){
            throw new IllegalStateException("already requested a ride");
        }

        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("No passenger with this id"));

        Ride ride = Ride.builder()
                .passenger(user)
                .driver(null)
                .status(RIDE_STATUS.REQUESTED)
                .pickupLocation(location)
                .date(Instant.now())
                .build();

    ride = rideRepository.save(ride);

    Map<String, String> redisRideState = Map.of(
            "rideId", ride.getId().toString(),
            "passengerId", id.toString(),
            "driverId", "",
            "pickupLocation", location,
            "status", ride.getStatus().name(),
            "people", String.valueOf(ride.getPeople()),
            "timestamp", ride.getDate().toString()
    );




    String redisKey = "ride:" + ride.getId().toString();
    redisTemplate.opsForHash().putAll(redisKey, redisRideState);
    redisTemplate.opsForValue().set("passenger:to:ride:"+ id.toString(), ride.getId().toString());
    redisTemplate.opsForSet().add("rides:open", ride.getId().toString());
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
