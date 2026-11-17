package com.tcg.chatservice.dto;

import com.tcg.chatservice.model.MensajeChat;

import java.time.Instant;

public record ChatMessage(
        long id,
        long tradeId,
        String senderUsername,
        String content,
        Instant sentAt) {

    public static ChatMessage from(MensajeChat mensaje) {
        return new ChatMessage(
                mensaje.getId(),
                mensaje.getPropuestaId(),
                mensaje.getEmisor().getUsername(),
                mensaje.getContenido(),
                mensaje.getCreatedAt());
    }
}
