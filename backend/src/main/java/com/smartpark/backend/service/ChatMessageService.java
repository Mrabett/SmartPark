package com.smartpark.backend.service;

import com.smartpark.backend.model.ChatMessage;
import com.smartpark.backend.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public List<ChatMessage> getPublicMessages() {
        return chatMessageRepository.findAllByOrderByCreatedAtAsc();
    }

    public ChatMessage sendMessage(ChatMessage payload) {
        if (payload.getSenderId() == null || payload.getSenderId().isBlank()) {
            throw new IllegalArgumentException("Expéditeur invalide");
        }
        if (payload.getContent() == null || payload.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Le message est vide");
        }

        String trimmed = payload.getContent().trim();
        if (trimmed.length() > 1000) {
            throw new IllegalArgumentException("Le message dépasse 1000 caractères");
        }

        ChatMessage msg = new ChatMessage();
        msg.setSenderId(payload.getSenderId());
        msg.setSenderName(safeName(payload.getSenderName()));
        msg.setContent(trimmed);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setUpdatedAt(null);

        return chatMessageRepository.save(msg);
    }

    public ChatMessage updateMessage(String messageId, String content, String requesterId, String requesterEmail, boolean isAdmin) {
        ChatMessage existing = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message introuvable"));

        if (!isAdmin) {
            boolean hasRequesterId = requesterId != null && !requesterId.isBlank();
            boolean hasRequesterEmail = requesterEmail != null && !requesterEmail.isBlank();

            if (!hasRequesterId && !hasRequesterEmail) {
                throw new IllegalArgumentException("Utilisateur non autorisé");
            }

            boolean canEditOwn = (hasRequesterId && requesterId.equals(existing.getSenderId()))
                    || (hasRequesterEmail && requesterEmail.equals(existing.getSenderId()));

            if (!canEditOwn) {
                throw new IllegalArgumentException("Vous ne pouvez modifier que vos propres messages");
            }
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Le message est vide");
        }

        String trimmed = content.trim();
        if (trimmed.length() > 1000) {
            throw new IllegalArgumentException("Le message dépasse 1000 caractères");
        }

        existing.setContent(trimmed);
        existing.setUpdatedAt(LocalDateTime.now());
        return chatMessageRepository.save(existing);
    }

    public void deleteMessage(String messageId) {
        chatMessageRepository.deleteById(messageId);
    }

    public ChatMessage getById(String messageId) {
        return chatMessageRepository.findById(messageId).orElse(null);
    }

    private String safeName(String value) {
        return value == null ? "" : value.trim();
    }
}
