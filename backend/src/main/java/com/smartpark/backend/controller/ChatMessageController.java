package com.smartpark.backend.controller;

import com.smartpark.backend.model.ChatMessage;
import com.smartpark.backend.service.ChatMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @GetMapping("/messages")
    public List<ChatMessage> getMessages() {
        return chatMessageService.getPublicMessages();
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessage sendMessage(@RequestBody ChatMessage payload) {
        try {
            return chatMessageService.sendMessage(payload);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PutMapping("/messages/{messageId}")
    public ChatMessage updateMessage(
            @PathVariable String messageId,
            @RequestBody Map<String, String> payload,
            Authentication authentication
    ) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        String content = payload.getOrDefault("content", "");
        String requesterId = payload.getOrDefault("requesterId", "");
        String requesterEmail = payload.getOrDefault("requesterEmail", "");

        // Fallback: l'utilisateur authentifié (email JWT) si le front n'envoie pas requesterEmail
        if ((requesterEmail == null || requesterEmail.isBlank()) && authentication != null) {
            requesterEmail = authentication.getName();
        }

        try {
            return chatMessageService.updateMessage(messageId, content, requesterId, requesterEmail, isAdmin);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() == null ? "Erreur modification message" : ex.getMessage();
            if (msg.toLowerCase().contains("introuvable")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
            }
            if (msg.toLowerCase().contains("propres messages") || msg.toLowerCase().contains("non autorisé")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, msg);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }
    }

    @DeleteMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable String messageId, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul l'admin peut supprimer un message");
        }

        ChatMessage existing = chatMessageService.getById(messageId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message introuvable");
        }

        chatMessageService.deleteMessage(messageId);
    }
}
