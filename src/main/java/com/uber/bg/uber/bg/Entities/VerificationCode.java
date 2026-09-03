package com.uber.bg.uber.bg.Entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@RedisHash(value = "verification_codes", timeToLive = 300)
public class VerificationCode {

    @Id
    private String code;

    @Indexed
    private String email;
}
