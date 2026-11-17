package com.tcg.tradeservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateTradeRequest(
        @NotBlank @Size(max = 50) String receptorUsername,
        @Size(max = 20) List<@NotNull @Valid ItemTradeRequest> ofertas,
        @Size(max = 20) List<@NotNull @Valid ItemTradeRequest> solicitudes) {
}
