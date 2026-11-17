package com.tcg.tradeservice.dto;

import com.tcg.tradeservice.model.PropuestaTrade;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public record PropuestaResponse(
        Long id,
        String estado,
        Instant createdAt,
        ParticipanteResponse emisor,
        ParticipanteResponse receptor,
        List<ItemTradeResponse> items) {

    public static PropuestaResponse from(PropuestaTrade propuesta) {
        return new PropuestaResponse(
                propuesta.getId(),
                propuesta.getEstado().name(),
                propuesta.getCreatedAt(),
                ParticipanteResponse.from(propuesta.getEmisor()),
                ParticipanteResponse.from(propuesta.getReceptor()),
                propuesta.getItems().stream()
                        .sorted(Comparator.comparing(item -> item.getId() == null ? 0L : item.getId()))
                        .map(ItemTradeResponse::from)
                        .toList());
    }
}
