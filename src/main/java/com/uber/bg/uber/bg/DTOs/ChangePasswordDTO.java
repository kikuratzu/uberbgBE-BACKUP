package com.uber.bg.uber.bg.DTOs;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ChangePasswordDTO {
    private String username;
    private String oldPassword;
    private String newPassword;
}
