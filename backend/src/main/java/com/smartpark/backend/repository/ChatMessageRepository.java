package com.smartpark.backend.repository;

import com.smartpark.backend.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findAllByOrderByCreatedAtAsc();
}
