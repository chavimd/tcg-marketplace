package com.tcg.catalogservice.dto;

import com.tcg.catalogservice.model.EstadoPublicacion;
import jakarta.validation.constraints.NotNull;

public record EstadoPublicacionRequest(@NotNull EstadoPublicacion estado) {
}
