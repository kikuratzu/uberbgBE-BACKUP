package com.uber.bg.uber.bg.Repositories.Jpa;

import com.uber.bg.uber.bg.Entities.Ride;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRepository extends JpaRepository<Ride, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.id = :id")
    Optional<Ride> findByIdWithLock(@Param("id") UUID id);

    @Modifying
    @Query(value = "UPDATE rides SET driver_location = (" +
            "SELECT ST_MakeLine(ST_SetSRID(ST_MakePoint(longitude, latitude), 4326) ORDER BY created_at ASC" +
            "FROM temp_ride_coordinates WHERE ride_id = :rideId" +
            "WHERE id = :rideId", nativeQuery = true)
    void compileRouteHistoryToLineString(@Param("rideId") UUID rideId);
}