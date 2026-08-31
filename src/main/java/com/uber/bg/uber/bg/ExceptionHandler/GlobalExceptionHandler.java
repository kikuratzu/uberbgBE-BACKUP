package com.uber.bg.uber.bg.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentExceptions(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());

        if (ex.getMessage().contains("taken")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @MessageExceptionHandler(IllegalStateException.class)
    @SendToUser("/queue/errors")
    public String handleChatError(IllegalStateException ex) {
        log.warn("Chat rejected: {}", ex.getMessage());
        return ex.getMessage();
    }
}
