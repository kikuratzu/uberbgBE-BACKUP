package com.uber.bg.uber.bg.Components;

import com.uber.bg.uber.bg.Services.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketInterceptor implements ChannelInterceptor {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public WebSocketInterceptor(UserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("STOMP CONNECT with no/invalid Authorization header — proceeding unauthenticated");
                return message;
            }

            String token = authHeader.substring(7);
            try {
                String username = jwtService.extractUsername(token);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    accessor.setUser(authentication);
                }
            } catch (Exception e) {
                log.warn("STOMP CONNECT auth failed for token: {}", e.getMessage());
            }
        }
        return message;
    }
}