package com.uber.bg.uber.bg.Components;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class WebSocketInterceptor implements ChannelInterceptor {
    private final UserDetailsService userDetailsService;


    public WebSocketInterceptor(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // Fix 1: Use StompHeaderAccessor.wrap() to extract the accessor safely
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // Fix 2: Prevent NullPointerExceptions on background socket heartbeats
        if (accessor.getCommand() != null && "CONNECT".equals(accessor.getCommand().name())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = "test_user_or_extract_from_jwt"; // Replace with your JWT service extract logic

                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    accessor.setUser(authentication);
                }
            }
        }
        return message;
    }
}
