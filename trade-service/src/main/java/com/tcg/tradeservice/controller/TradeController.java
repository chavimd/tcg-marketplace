package com.tcg.tradeservice.controller;

import com.tcg.tradeservice.dto.CreateTradeRequest;
import com.tcg.tradeservice.dto.PropuestaResponse;
import com.tcg.tradeservice.service.TradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropuestaResponse create(
            Authentication authentication,
            @Valid @RequestBody CreateTradeRequest request) {
        return tradeService.create(requireUserId(authentication), request);
    }

    @GetMapping
    public List<PropuestaResponse> list(Authentication authentication) {
        return tradeService.listForUser(requireUserId(authentication));
    }

    @GetMapping("/{id}")
    public PropuestaResponse get(Authentication authentication, @PathVariable Long id) {
        return tradeService.getForUser(id, requireUserId(authentication));
    }

    @PostMapping("/{id}/accept")
    public PropuestaResponse accept(Authentication authentication, @PathVariable Long id) {
        return tradeService.accept(id, requireUserId(authentication));
    }

    @PostMapping("/{id}/reject")
    public PropuestaResponse reject(Authentication authentication, @PathVariable Long id) {
        return tradeService.reject(id, requireUserId(authentication));
    }

    @PostMapping("/{id}/cancel")
    public PropuestaResponse cancel(Authentication authentication, @PathVariable Long id) {
        return tradeService.cancel(id, requireUserId(authentication));
    }

    private Long requireUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token sin identificador de usuario");
        }
        return userId;
    }
}
