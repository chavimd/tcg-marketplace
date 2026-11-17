package com.tcg.catalogservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JuegoRequest(
        @NotBlank @Size(max = 50) String nombre,
        String descripcion) {
}
