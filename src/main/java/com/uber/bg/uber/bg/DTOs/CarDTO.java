package com.uber.bg.uber.bg.DTOs;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
public class CarDTO extends BaseDTO {
    private String brand;
    private String model;
    private String plateNumber;
    private String carPhoto;
}
