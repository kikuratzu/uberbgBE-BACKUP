package com.uber.bg.uber.bg.Controllers;

import com.uber.bg.uber.bg.Entities.Ride;
import com.uber.bg.uber.bg.Services.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("api/auth/driver")
public class DriverController {

    private final DriverService service;

    @Autowired
    public DriverController(DriverService driverService){
        this.service = driverService;
    }

@GetMapping("/getAllRides")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    public List<Map<String, String>> getAllRidesAvailableRides() {
    return service.getAllAvailableRides();
}

@PostMapping("/acceptRide/{rideId}/{driverId}")
    @PreAuthorize("hasRole('DRIVER')")
    public HttpStatus acceptRide(
        @PathVariable final UUID rideId,
        @PathVariable final UUID driverId
        ) {
        service.acceptRide(rideId, driverId);
        return HttpStatus.ACCEPTED;
}

}
