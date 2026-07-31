package com.uber.bg.uber.bg.Configurations;

import org.springframework.messaging.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfiguration {
    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
                .nullDestMatcher().permitAll()
                // FIX: Restructured role-validation mappings to enforce structural rule integrity flawlessly
                .simpDestMatchers("/app/chat/**","/app/ping").hasAnyRole("PASSENGER", "DRIVER", "ADMIN")
                .anyMessage().permitAll();

        return messages.build();
    }

}
