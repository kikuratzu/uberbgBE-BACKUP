package com.uber.bg.uber.bg.Entities;


import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.awt.*;
import java.time.Instant;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatEntity extends BaseEntity {

    private String rideId;
    private String sender;
    private String content;
    private Instant time = Instant.now();
}
