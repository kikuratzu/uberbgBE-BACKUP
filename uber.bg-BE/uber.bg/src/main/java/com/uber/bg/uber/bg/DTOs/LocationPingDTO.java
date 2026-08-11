package com.uber.bg.uber.bg.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LocationPingDTO extends BaseDTO {
    private double longitude;
    private double latitude;
}
