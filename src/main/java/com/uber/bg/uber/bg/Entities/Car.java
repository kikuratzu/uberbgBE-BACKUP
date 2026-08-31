package com.uber.bg.uber.bg.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cars")
public class Car extends BaseEntity {

    @Column(name = "brand", nullable = false)
    private String brand;
    @Column(name = "model", nullable = false)
    private String model;
    @Column(name = "plate_number", nullable = false)
    private String plateNumber;
    @Lob
    @Column(name = "car_photo", nullable = false)
    private byte[] carPhoto;

    @ManyToMany(mappedBy = "vehicles")
    private Set<User> drivers = new HashSet<>();
}
