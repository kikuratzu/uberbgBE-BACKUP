package com.uber.bg.uber.bg.Controllers;

import com.uber.bg.uber.bg.DTOs.ChatEntityDTO;
import com.uber.bg.uber.bg.Services.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    public String handleRideChat(
          @DestinationVariable final UUID rideId,
          @Payload ChatEntityDTO dto
          ) {
      log.info("Received chat message for ride: {}", rideId);
      chatService.handleRideChat(rideId, dto);
      return "a";
  }



}
