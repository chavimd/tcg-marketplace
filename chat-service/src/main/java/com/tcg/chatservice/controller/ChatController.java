package com.tcg.chatservice.controller;

import com.tcg.chatservice.dto.ChatMessage;
import com.tcg.chatservice.dto.ChatMessageRequest;
import com.tcg.chatservice.service.ChatService;
import com.tcg.chatservice.service.TradeAuthorizationClient;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final TradeAuthorizationClient tradeAuthorizationClient;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(
            TradeAuthorizationClient tradeAuthorizationClient,
            ChatService chatService,
            SimpMessagingTemplate messagingTemplate) {
        this.tradeAuthorizationClient = tradeAuthorizationClient;
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/trades/{tradeId}/messages")
    public void sendMessage(
            @DestinationVariable long tradeId,
            ChatMessageRequest request,
            Authentication authentication) {
        String contenido = request == null ? null : request.content();
        if (contenido == null || contenido.isBlank() || contenido.length() > 2000) {
            throw new MessagingException("Message content must contain between 1 and 2000 characters");
        }
        Long emisorId = requireUserId(authentication);
        String token = requireToken(authentication);
        var participants = tradeAuthorizationClient.getParticipants(tradeId, token);
        String sender = authentication.getName();
        String recipient;
        if (sender.equals(participants.proposerUsername())) {
            recipient = participants.recipientUsername();
        } else if (sender.equals(participants.recipientUsername())) {
            recipient = participants.proposerUsername();
        } else {
            throw new MessagingException("User is not a trade participant");
        }

        ChatMessage message = chatService.guardar(tradeId, emisorId, contenido.trim());
        messagingTemplate.convertAndSendToUser(sender, "/queue/trades/" + tradeId, message);
        messagingTemplate.convertAndSendToUser(recipient, "/queue/trades/" + tradeId, message);
    }

    private Long requireUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Long userId)) {
            throw new MessagingException("Authenticated user has no identifier");
        }
        return userId;
    }

    private String requireToken(Authentication authentication) {
        if (authentication == null || !(authentication.getCredentials() instanceof String token)) {
            throw new MessagingException("Authenticated user has no bearer token");
        }
        return token;
    }
}
