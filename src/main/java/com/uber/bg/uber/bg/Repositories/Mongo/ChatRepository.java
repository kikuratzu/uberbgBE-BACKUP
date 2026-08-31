package com.uber.bg.uber.bg.Repositories.Mongo;

import com.uber.bg.uber.bg.Entities.ChatEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatRepository extends MongoRepository<ChatEntity, UUID> {
}
