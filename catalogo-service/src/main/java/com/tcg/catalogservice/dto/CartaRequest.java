package com.tcg.catalogservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CartaRequest(
        @NotNull Integer juegoId,
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 30) String numeroSet,
        @NotBlank @Size(max = 100) String setNombre,
        @NotBlank @Size(max = 20) String rareza,
        @Size(max = 50) String tipo,
        @Size(max = 30) String color,
        String textoCarta,
        String imagenUrl,
        Map<String, Object> metadataExtra) {
}
