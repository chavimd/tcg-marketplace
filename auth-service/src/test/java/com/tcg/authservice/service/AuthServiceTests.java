package com.tcg.authservice.service;

import com.tcg.authservice.dto.LoginRequest;
import com.tcg.authservice.dto.RegisterRequest;
import com.tcg.authservice.model.Usuario;
import com.tcg.authservice.repository.UsuarioRepository;
import com.tcg.authservice.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTests {

    @Test
    void registrationHashesPasswordAndReturnsSignedToken() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        JwtService jwtService = new JwtService("a-test-secret-that-is-at-least-thirty-two-bytes", 3600);
        AuthService authService = new AuthService(repository, passwordEncoder, jwtService);

        when(repository.existsByUsername("collector")).thenReturn(false);
        when(repository.existsByEmail("collector@example.com")).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(7L);
            return usuario;
        });

        var response = authService.register(
                new RegisterRequest("collector", "collector@example.com", "password123"));

        assertEquals(7L, response.user().id());
        assertEquals("collector", jwtService.parseToken(response.accessToken()).getSubject());
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(
                usuario -> passwordEncoder.matches("password123", usuario.getPasswordHash())));
    }

    @Test
    void loginAcceptsMatchingPasswordAndReturnsToken() {
        Usuario usuario = new Usuario();
        usuario.setUsername("collector");
        usuario.setEmail("collector@example.com");
        usuario.setPasswordHash(new BCryptPasswordEncoder().encode("password123"));

        UsuarioRepository repository = mock(UsuarioRepository.class);
        when(repository.findByUsername("collector")).thenReturn(java.util.Optional.of(usuario));
        JwtService jwtService = new JwtService("a-test-secret-that-is-at-least-thirty-two-bytes", 3600);
        AuthService authService = new AuthService(
                repository,
                new BCryptPasswordEncoder(),
                jwtService);

        var response = authService.login(new LoginRequest("collector", "password123"));

        assertEquals("collector", jwtService.parseToken(response.accessToken()).getSubject());
        assertEquals("collector", response.user().username());
        assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("collector", "wrong-password")));
    }
}
