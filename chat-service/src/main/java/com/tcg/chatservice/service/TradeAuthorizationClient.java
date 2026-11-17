package com.tcg.chatservice.service;

import com.tcg.chatservice.dto.TradeParticipants;
import com.tcg.chatservice.dto.TradeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TradeAuthorizationClient {

    private final RestClient restClient;

    public TradeAuthorizationClient(@Value("${app.trade-service.url}") String tradeServiceUrl) {
        restClient = RestClient.builder().baseUrl(tradeServiceUrl).build();
    }

    public TradeParticipants getParticipants(long tradeId, String token) {
        try {
            TradeResponse trade = restClient.get()
                    .uri("/api/trades/{id}", tradeId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(TradeResponse.class);
            if (trade == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Trade service returned an empty response");
            }
            return trade.participantes();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "User is not a participant in this trade", exception);
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Trade service could not authorize this chat", exception);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Trade service is unavailable", exception);
        }
    }
}
