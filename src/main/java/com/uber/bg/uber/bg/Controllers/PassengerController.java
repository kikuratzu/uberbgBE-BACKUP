package com.uber.bg.uber.bg.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.uber.bg.uber.bg.Services.RideMatchingService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passenger")
public class PassengerController {

    @Autowired
    private RideMatchingService rideMatchingService;

    @GetMapping("/getNearbyDrivers")
    @PreAuthorize("hasRole('PASSENGER')")
    public List<Map<String, Object>> getNearbyDrivers() {
        return rideMatchingService.getOnlineDrivers();
    }

    @GetMapping("/hello")
    @PreAuthorize("hasRole('PASSENGER')")
    public String hello(){
        return "hello";
    }
}
