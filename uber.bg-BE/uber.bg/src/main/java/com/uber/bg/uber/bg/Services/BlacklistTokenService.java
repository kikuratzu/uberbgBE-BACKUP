package com.uber.bg.uber.bg.Services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class BlacklistTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    public BlacklistTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Blacklist a token by saving it to Redis with a strict Time-To-Live (TTL).
     */
    public void blacklistToken(String token, long remainingTimeMillis) {
        // Prefix the key to maintain clear cache organization namespaces
        String redisKey = "blacklist:" + token;

        // Save the key with value "true" and enforce auto-deletion on expiration
        redisTemplate.opsForValue().set(
                redisKey,
                "true",
                Duration.ofMillis(remainingTimeMillis)
        );
    }

    /**
     * Checks whether a token currently exists in the blacklisted space.
     */
    public boolean isTokenBlacklisted(String token) {
        Boolean hasKey = redisTemplate.hasKey("blacklist:" + token);
        return hasKey != null && hasKey;
    }
}
