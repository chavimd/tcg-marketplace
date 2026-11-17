package com.tcg.tradeservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemTradeRequest(
        @NotNull Long publicacionId,
        @Min(1) int cantidad) {
}
