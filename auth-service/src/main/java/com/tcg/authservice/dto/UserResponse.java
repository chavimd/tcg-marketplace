package com.tcg.authservice.dto;

import com.tcg.authservice.model.Usuario;

public record UserResponse(Long id, String username, String email, String rol) {

    public static UserResponse from(Usuario usuario) {
        return new UserResponse(usuario.getId(), usuario.getUsername(), usuario.getEmail(), usuario.getRol());
    }
}
