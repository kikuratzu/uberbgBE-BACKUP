package com.uber.bg.uber.bg.Controllers;

import com.uber.bg.uber.bg.DTOs.LocationPingDTO;
import com.uber.bg.uber.bg.Entities.Ride;
import com.uber.bg.uber.bg.Services.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("api/auth/driver")
public class DriverController {

    private final DriverService service;
    private final KafkaTemplate<String, LocationPingDTO> kafkaTemplate;
    private static final String TOPIC = "driver-locations";

    @Autowired
    public DriverController(DriverService driverService, final KafkaTemplate<String, LocationPingDTO> kafkaTemplate){
        this.service = driverService;
        this.kafkaTemplate = kafkaTemplate;
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

@PostMapping("/streamLocation/{rideId}/{driverId}")
    @PreAuthorize("hasRole('DRIVER')")
    public void streamLocation(
            @PathVariable final UUID rideId,
            @PathVariable final UUID driverId,
            @RequestBody final LocationPingDTO locationPingDTO) {
    ProducerRecord<String, LocationPingDTO> record = new ProducerRecord<>(TOPIC, driverId.toString(), locationPingDTO);
    record.headers().add("rideId", rideId.toString().getBytes(StandardCharsets.UTF_8));

    this.kafkaTemplate.send(record);
}

@DeleteMapping("/endRide/{rideId}")
    @PreAuthorize("hasRole('DRIVER')")
    public void endRide(@PathVariable final UUID rideId) {
service.endRide(rideId);
}

}
