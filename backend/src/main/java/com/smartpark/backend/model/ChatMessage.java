package com.smartpark.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_messages")
@Data
@NoArgsConstructor
public class ChatMessage {

    @Id
    private String id;

    private String senderId;
    private String senderName;

    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
