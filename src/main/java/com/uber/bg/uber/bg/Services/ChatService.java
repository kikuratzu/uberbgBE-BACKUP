package com.uber.bg.uber.bg.Services;

import com.uber.bg.uber.bg.DTOs.ChatEntityDTO;
import com.uber.bg.uber.bg.Entities.ChatEntity;
import com.uber.bg.uber.bg.Enumerations.RIDE_STATUS;
import com.uber.bg.uber.bg.Repositories.Mongo.ChatRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;      // original — for ride status hash
    private final RedisTemplate<String, Object> chatRedisTemplate;  // JSON one — for chat list

    @Autowired
    public ChatService(
            ChatRepository chatRepository,
            SimpMessagingTemplate messagingTemplate,
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("chatRedisTemplate") RedisTemplate<String, Object> chatRedisTemplate) {
        this.chatRepository = chatRepository;
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
        this.chatRedisTemplate = chatRedisTemplate;
    }

    public ChatEntityDTO handleRideChat(final UUID rideId, final ChatEntityDTO dto) {
        Object cachedStatus = redisTemplate.opsForHash().get("ride:" + rideId.toString(), "status");
        String statusStr = cachedStatus != null ? cachedStatus.toString().replaceAll("^\"|\"$", "") : null;

        if (statusStr == null || !statusStr.equals(RIDE_STATUS.ACCEPTED.name())) {
            throw new IllegalStateException("Ride is not accepted, it is: " + (statusStr != null ? statusStr : "NON-EXISTENT"));
        }

        ChatEntity chatEntity = new ChatEntity();
        BeanUtils.copyProperties(dto, chatEntity);
        chatEntity.setTime(Instant.now());
        chatEntity.setRideId(rideId.toString());
        chatEntity.setId(UUID.randomUUID());

        ChatEntity savedChat = chatRepository.save(chatEntity);
        ChatEntityDTO response = new ChatEntityDTO();
        BeanUtils.copyProperties(savedChat, response);

        chatRedisTemplate.opsForList().rightPush("chatHistory:rideId:" + rideId, response);
        chatRedisTemplate.expire("chatHistory:rideId:" + rideId, Duration.ofHours(2));

        messagingTemplate.convertAndSend("/topic/ride/" + rideId, response);

        return response;
    }

    public List<ChatEntityDTO> getChatHistory(final UUID rideId) {
        List<Object> raw = chatRedisTemplate.opsForList()
                .range("chatHistory:rideId:" + rideId, 0, -1);

        if (raw == null || raw.isEmpty()) {
            return List.of();
        }

        return raw.stream()
                .map(obj -> (ChatEntityDTO) obj)
                .toList();
    }
}
