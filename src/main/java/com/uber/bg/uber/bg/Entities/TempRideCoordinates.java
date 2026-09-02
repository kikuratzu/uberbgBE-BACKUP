package com.uber.bg.uber.bg.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "temp_ride_coordinates", indexes = {
        @Index(name = "idx_temp_coordinates_ride_time", columnList = "ride_id, created_at ASC")
})
public class TempRideCoordinates extends BaseEntity {
    @Column(name = "ride_id", nullable = false)
    private UUID rideId;
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime time = LocalDateTime.now();


}
