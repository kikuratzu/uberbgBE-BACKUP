package com.uber.bg.uber.bg.Controllers;

import com.uber.bg.uber.bg.DTOs.ActivityDTO;
import com.uber.bg.uber.bg.DTOs.LocationPingDTO;
import com.uber.bg.uber.bg.DTOs.RequestRideDTO;
import com.uber.bg.uber.bg.Entities.LocationPing;
import com.uber.bg.uber.bg.Entities.User;
import com.uber.bg.uber.bg.Services.RiderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("api/auth/passenger/")
public class RiderController {


    private final RiderService service;

    @Autowired
    public RiderController(RiderService riderService) {
        this.service = riderService;
    }

    @PostMapping("requestRide/{passengerId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public HttpStatus requestRider(@PathVariable final UUID passengerId, @RequestBody RequestRideDTO dto) {
        service.requestRide(passengerId, dto.getPickup(), dto.getDestination(), dto.getPeople());
        return HttpStatus.ACCEPTED;
    }

    @DeleteMapping("cancelRide/{passengerId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public HttpStatus cancelRide(@PathVariable final UUID passengerId) {
        service.CancelRide(passengerId);
        return HttpStatus.ACCEPTED;
    }

    @PatchMapping("rateRide/{rideId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public void rateRide(@PathVariable final UUID rideId, @RequestParam double rating) {
        service.rateRide(rideId, rating);
    }


    @GetMapping("/getActivity/{userId}")
    @PreAuthorize("hasAnyRole('DRIVER','PASSENGER')")
    public Page<ActivityDTO> getActivity(@PathVariable final UUID userId,
                                         @PageableDefault(size = 5, sort = "date", direction = Sort.Direction.DESC) final Pageable pageable) {
        return service.getActivity(userId, pageable);
    }

}

