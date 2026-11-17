package com.tcg.catalogservice.controller;

import com.tcg.catalogservice.dto.CartaRequest;
import com.tcg.catalogservice.dto.CartaResponse;
import com.tcg.catalogservice.dto.EstadoPublicacionRequest;
import com.tcg.catalogservice.dto.JuegoResponse;
import com.tcg.catalogservice.dto.JuegoRequest;
import com.tcg.catalogservice.dto.PublicacionGrantRequest;
import com.tcg.catalogservice.dto.PublicacionResponse;
import com.tcg.catalogservice.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/games")
    public List<JuegoResponse> games() {
        return catalogoService.listGames();
    }

    @PostMapping("/games")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public JuegoResponse createGame(@Valid @RequestBody JuegoRequest request) {
        return catalogoService.createGame(request);
    }

    @GetMapping("/cards")
    public Page<CartaResponse> searchCards(
            @RequestParam(required = false) String game,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return catalogoService.searchCards(
                game, rarity, q, pageRequest(page, size, Sort.by("nombre").ascending()));
    }

    @GetMapping("/cards/{id}")
    public CartaResponse getCard(@PathVariable Long id) {
        return catalogoService.getCard(id);
    }

    @PostMapping("/cards")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CartaResponse createCard(@Valid @RequestBody CartaRequest request) {
        return catalogoService.createCard(request);
    }

    @GetMapping("/publications")
    public Page<PublicacionResponse> publications(
            Authentication authentication,
            @RequestParam(required = false) String game,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return catalogoService.searchAvailable(
                optionalUserId(authentication),
                game,
                rarity,
                q,
                pageRequest(page, size, Sort.by("createdAt").descending()));
    }

    @GetMapping("/publications/{id}")
    public PublicacionResponse publication(@PathVariable Long id) {
        return catalogoService.getPublication(id);
    }

    @GetMapping("/inventory/me")
    public List<PublicacionResponse> myInventory(Authentication authentication) {
        return catalogoService.myPublications(requireUserId(authentication));
    }

    @PostMapping("/inventory/grants")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public PublicacionResponse grantInventory(@Valid @RequestBody PublicacionGrantRequest request) {
        return catalogoService.grantInventory(request);
    }

    @PatchMapping("/inventory/{id}/status")
    public PublicacionResponse updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody EstadoPublicacionRequest request) {
        return catalogoService.updateStatus(requireUserId(authentication), id, request.estado());
    }

    private PageRequest pageRequest(int page, int size, Sort sort) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La página debe ser >= 0 y el tamaño debe estar entre 1 y 100");
        }
        return PageRequest.of(page, size, sort);
    }

    private Long requireUserId(Authentication authentication) {
        Long userId = optionalUserId(authentication);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token sin identificador de usuario");
        }
        return userId;
    }

    private Long optionalUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getDetails() instanceof Long userId ? userId : null;
    }
}
