package com.uber.bg.uber.bg.Services;

import com.uber.bg.uber.bg.DTOs.ChatEntityDTO;
import com.uber.bg.uber.bg.Entities.ChatEntity;
import com.uber.bg.uber.bg.Enumerations.RIDE_STATUS;
import com.uber.bg.uber.bg.Repositories.Mongo.ChatRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public ChatService(ChatRepository chatRepository, SimpMessagingTemplate messagingTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.chatRepository = chatRepository;
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
    }


    public ChatEntityDTO handleRideChat(final UUID rideId, final ChatEntityDTO dto) {
        Object cachedStatus = redisTemplate.opsForHash().get("ride:" + rideId.toString(), "status");

        if (cachedStatus == null || !cachedStatus.toString().equals(RIDE_STATUS.ACCEPTED.name())) {
            throw new IllegalStateException("Ride is not accepted, it is: " + (cachedStatus != null ? cachedStatus : "NON-EXISTENT"));
        }

        ChatEntity chatEntity = new ChatEntity();
        BeanUtils.copyProperties(dto,chatEntity);
        chatEntity.setTime(Instant.now());
        chatEntity.setRideId(rideId.toString());
        chatEntity.setId(UUID.randomUUID());

        ChatEntity savedChat = chatRepository.save(chatEntity);
        ChatEntityDTO response = new ChatEntityDTO();
        BeanUtils.copyProperties(savedChat, response);
        return response;


    }


}
