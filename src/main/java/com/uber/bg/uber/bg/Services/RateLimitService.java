package com.uber.bg.uber.bg.Services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;
import java.time.Duration;



@Service
public class RateLimitService {
private final Cache<String, Bucket> cache = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15))
        .maximumSize(50000)
        .build();

private Bucket createNewBucket() {
    return Bucket.builder()
            .addLimit(Bandwidth.builder()
                    .capacity(3)
                    .refillGreedy(3, Duration.ofMinutes(10))
                    .build())
            .build();

}
public boolean tryConsume(String ipAddress) {
    Bucket bucket = cache.get(ipAddress, k -> createNewBucket());
    return bucket != null && bucket.tryConsume(1);
}

}
