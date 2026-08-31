package com.uber.bg.uber.bg.Controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/passenger")
public class PassengerController {

    @GetMapping("/hello")
    @PreAuthorize("hasRole('PASSENGER')")
    public String hello(){
        return "hello";
    }
}
