package com.uber.bg.uber.bg.Entities;

import com.mongodb.lang.Nullable;
import com.uber.bg.uber.bg.Entities.BaseEntity;
import com.uber.bg.uber.bg.Enumerations.USER_ROLE;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phoneNumber", nullable = false, length = 10)
    private String phoneNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "createdOn")
    private LocalDateTime date = LocalDateTime.now();

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private USER_ROLE role;

   @Column(name= "rating")
    private Double rating;

    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ride> rideHistory = new ArrayList<>();

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ride> driveHistory = new ArrayList<>();


    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "driver_cars",
            joinColumns = @JoinColumn(name = "driver_id"),
            inverseJoinColumns = @JoinColumn(name = "car_id")
    )
    private Set<Car> vehicles = new HashSet<>();

}



