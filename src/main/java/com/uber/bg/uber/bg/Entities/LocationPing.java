package com.uber.bg.uber.bg.Entities;

import jakarta.persistence.Embeddable;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class LocationPing{
    private double longitude;
    private double latitude;
}
