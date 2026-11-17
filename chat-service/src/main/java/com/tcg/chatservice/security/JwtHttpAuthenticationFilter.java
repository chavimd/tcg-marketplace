package com.tcg.chatservice.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtHttpAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtHttpAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        try {
            String token = header.substring(7);
            var claims = jwtService.parse(token);
            String subject = claims.getSubject();
            String role = claims.get("role", String.class);
            Number uidClaim = claims.get("uid", Number.class);
            Long userId = uidClaim == null ? null : uidClaim.longValue();
            if (subject == null || subject.isBlank() || role == null || userId == null
                    || !List.of("CLIENTE", "ADMIN").contains(role)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid bearer token");
                return;
            }
            var authentication = new UsernamePasswordAuthenticationToken(
                    subject, token, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            authentication.setDetails(userId);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired bearer token");
            return;
        }
        chain.doFilter(request, response);
    }
}
