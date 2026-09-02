package com.uber.bg.uber.bg.Repositories.Jpa;

import com.uber.bg.uber.bg.Entities.TempRideCoordinates;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface TempRideCoordinatesRepository extends JpaRepository<TempRideCoordinates, UUID> {
    @Modifying
    @Query(value = "DELETE FROM  temp_ride_coordinates WHERE ride_id = :rideId", nativeQuery = true)
    void deleteByRideId(@Param("rideId") UUID rideId);
}
