package com.uber.bg.uber.bg.Entities;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.locationtech.jts.geom.LineString;
import com.uber.bg.uber.bg.Enumerations.RIDE_STATUS;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import java.time.Instant;

import java.time.Instant;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rides")
public class Ride extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    @JsonIgnoreProperties({"rideHistory", "driveHistory"})
    private User passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    @JsonIgnoreProperties({"rideHistory", "driveHistory"})
    private User driver;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude"))
    })
    private LocationPing pickupLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "destination_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "destination_longitude"))
    })
    private LocationPing destinationLocation;

    @Column(name = "driver_location", columnDefinition = "geometry(LineString,4326)")
    private LineString driverLocationHistory;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RIDE_STATUS status;

    @Column(name = "people")
    private Integer people;

    @Column(name = "date")
    private Instant date;

}
