package com.uber.bg.uber.bg.DTOs;

import com.uber.bg.uber.bg.Enumerations.RIDE_STATUS;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
public class ActivityDTO extends BaseDTO {
     private Double pickUpLongitude;
     private Double destinationLongitude;
     private Double pickUpLatitude;
     private Double destinationLatitude;
     private Double Price;
     private String driverName;
     private RIDE_STATUS status;
     private UUID rideId;
     private Instant date;
     private Integer people;
}
