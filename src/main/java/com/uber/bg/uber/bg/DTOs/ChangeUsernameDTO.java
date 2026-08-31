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
public class ChangeUsernameDTO extends BaseDTO{

    private String username;
    private String newUsername;
    private String password;

}