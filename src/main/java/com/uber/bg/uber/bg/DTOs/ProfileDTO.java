package com.uber.bg.uber.bg.DTOs;

import jakarta.persistence.Column;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
public class ProfileDTO {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phoneNumber;
    private Integer rating;
    private String role;
    private Set<CarDTO> vehicles;
}
