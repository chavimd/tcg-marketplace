package com.tcg.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutes {

    @Bean
    RouteLocator routes(
            RouteLocatorBuilder builder,
            @Value("${app.services.auth-url}") String authUrl,
            @Value("${app.services.catalog-url}") String catalogUrl,
            @Value("${app.services.trade-url}") String tradeUrl,
            @Value("${app.services.chat-ws-url}") String chatWsUrl,
            @Value("${app.services.chat-url}") String chatUrl) {
        return builder.routes()
                .route("auth-service", route -> route
                        .path("/api/auth/**")
                        .uri(authUrl))
                .route("catalog-games", route -> route
                        .path("/api/games")
                        .uri(catalogUrl))
                .route("catalog-publications", route -> route
                        .path("/api/publications", "/api/publications/**")
                        .uri(catalogUrl))
                .route("catalog-card-root", route -> route
                        .path("/api/cards")
                        .uri(catalogUrl))
                .route("catalog-cards", route -> route
                        .path("/api/cards/**", "/api/inventory/**")
                        .uri(catalogUrl))
                .route("chat-history", route -> route
                        .path("/api/trades/*/messages")
                        .uri(chatUrl))
                .route("trade-service", route -> route
                        .path("/api/trades/**")
                        .uri(tradeUrl))
                .route("chat-websocket", route -> route
                        .path("/ws", "/ws/**")
                        .uri(chatWsUrl))
                .build();
    }
}
