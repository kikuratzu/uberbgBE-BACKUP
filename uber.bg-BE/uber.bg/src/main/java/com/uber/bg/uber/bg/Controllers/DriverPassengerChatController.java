package com.uber.bg.uber.bg.Controllers;

import com.uber.bg.uber.bg.DTOs.ChatEntityDTO;
import com.uber.bg.uber.bg.Services.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;


import java.security.Principal;
import java.util.UUID;

@Controller
@Slf4j

public class DriverPassengerChatController {


    private final ChatService chatService;

    @Autowired
    public DriverPassengerChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat/{rideId}")
    @SendTo("/topic/ride/{rideId}")
    @PreAuthorize("hasAnyRole('DRIVER','PASSENGER','ADMIN')")
    public ChatEntityDTO handleRideChat(
            @DestinationVariable("rideId") final UUID rideId,
            @Payload ChatEntityDTO dto

    ) {
        System.out.println("hehehehaw");
        return chatService.handleRideChat(rideId, dto);
    }

    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public String handlePing(String message) {
        System.out.println("!!! PING METHOD EXECUTED: " + message);
        log.info("Ping message received: {}", message);
        return "PONG: " + message;
    }



}
