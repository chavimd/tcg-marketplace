package com.tcg.chatservice.controller;

import com.tcg.chatservice.dto.ChatMessage;
import com.tcg.chatservice.service.ChatService;
import com.tcg.chatservice.service.TradeAuthorizationClient;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class ChatHistoryController {

    private final TradeAuthorizationClient tradeAuthorizationClient;
    private final ChatService chatService;

    public ChatHistoryController(TradeAuthorizationClient tradeAuthorizationClient, ChatService chatService) {
        this.tradeAuthorizationClient = tradeAuthorizationClient;
        this.chatService = chatService;
    }

    @GetMapping("/api/trades/{tradeId}/messages")
    public List<ChatMessage> history(Authentication authentication, @PathVariable long tradeId) {
        if (tradeId < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador de trueque inválido");
        }
        if (authentication == null || !(authentication.getCredentials() instanceof String token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token sin credenciales");
        }
        var participants = tradeAuthorizationClient.getParticipants(tradeId, token);
        String username = authentication.getName();
        if (!username.equals(participants.proposerUsername())
                && !username.equals(participants.recipientUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No participas en este trueque");
        }
        return chatService.historial(tradeId);
    }
}
