package com.uber.bg.uber.bg.Entities;

import com.uber.bg.uber.bg.Entities.BaseEntity;
import com.uber.bg.uber.bg.Enumerations.USER_ROLE;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

}



