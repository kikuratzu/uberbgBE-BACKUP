package com.uber.bg.uber.bg.Controllers;

import com.uber.bg.uber.bg.DTOs.CreateUserDTO;
import com.uber.bg.uber.bg.DTOs.LoginUserDTO;
import com.uber.bg.uber.bg.Services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("/createUser")
    public HttpStatus createUser(
            @RequestBody CreateUserDTO dto
            ) {
        service.createUser(dto);
        return HttpStatus.CREATED;
    }

    @PostMapping("/loginUser")
    public Map<UUID, String> loginUser(
            @RequestBody final LoginUserDTO loginUserDTO
            ) {
           return service.login(loginUserDTO);
    }

    @DeleteMapping("/logOutUser")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    public HttpStatus logoutUser(@RequestHeader("Authorization") final String token) {
     service.logout(token.substring(7));
     return HttpStatus.ACCEPTED;
    }


}
