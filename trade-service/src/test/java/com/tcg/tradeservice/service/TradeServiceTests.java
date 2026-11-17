package com.tcg.tradeservice.service;

import com.tcg.tradeservice.dto.CreateTradeRequest;
import com.tcg.tradeservice.dto.ItemTradeRequest;
import com.tcg.tradeservice.dto.PropuestaResponse;
import com.tcg.tradeservice.model.CartaRef;
import com.tcg.tradeservice.model.EstadoPropuesta;
import com.tcg.tradeservice.model.ItemTrade;
import com.tcg.tradeservice.model.PropuestaTrade;
import com.tcg.tradeservice.model.Publicacion;
import com.tcg.tradeservice.model.RolItem;
import com.tcg.tradeservice.model.UsuarioRef;
import com.tcg.tradeservice.repository.PropuestaTradeRepository;
import com.tcg.tradeservice.repository.PublicacionRepository;
import com.tcg.tradeservice.repository.UsuarioRefRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeServiceTests {

    private final PropuestaTradeRepository propuestaRepository = mock(PropuestaTradeRepository.class);
    private final PublicacionRepository publicacionRepository = mock(PublicacionRepository.class);
    private final UsuarioRefRepository usuarioRepository = mock(UsuarioRefRepository.class);
    private final TradeService tradeService = new TradeService(
            propuestaRepository, publicacionRepository, usuarioRepository);

    private final UsuarioRef alice = usuario(1L, "alice");
    private final UsuarioRef bob = usuario(2L, "bob");
    private final CartaRef pikachu = carta(100L, "Pikachu");
    private final CartaRef squirtle = carta(200L, "Squirtle");

    @Test
    void cannotProposeTradeToSelf() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(usuarioRepository.findByUsername("alice")).thenReturn(Optional.of(alice));

        var request = new CreateTradeRequest("alice", List.of(), List.of());

        var error = assertThrows(ResponseStatusException.class, () -> tradeService.create(1L, request));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(propuestaRepository, never()).save(any());
    }

    @Test
    void requiresAtLeastOneCard() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(usuarioRepository.findByUsername("bob")).thenReturn(Optional.of(bob));

        var request = new CreateTradeRequest("bob", List.of(), List.of());

        var error = assertThrows(ResponseStatusException.class, () -> tradeService.create(1L, request));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(propuestaRepository, never()).save(any());
    }

    @Test
    void cannotOfferAPublicationOfAnotherUser() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(usuarioRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        Publicacion ofertaAjena = publicacion(
                20L, bob, squirtle, 1, "DISPONIBLE", new BigDecimal("5.00"));
        when(publicacionRepository.findById(20L)).thenReturn(Optional.of(ofertaAjena));

        var request = new CreateTradeRequest("bob",
                List.of(new ItemTradeRequest(20L, 1)), List.of());

        var error = assertThrows(ResponseStatusException.class, () -> tradeService.create(1L, request));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(propuestaRepository, never()).save(any());
    }

    @Test
    void onlyRecipientCanAccept() {
        PropuestaTrade propuesta = propuesta(alice, bob);
        when(propuestaRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(propuesta));

        var error = assertThrows(ResponseStatusException.class, () -> tradeService.accept(4L, 3L));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(publicacionRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void acceptanceMovesStockBetweenParticipants() {
        Publicacion aliceOffer = publicacion(10L, alice, pikachu, 2, "DISPONIBLE", new BigDecimal("10.00"));
        Publicacion bobRequest = publicacion(20L, bob, squirtle, 3, "DISPONIBLE", new BigDecimal("7.00"));
        Publicacion aliceSquirtle = publicacion(11L, alice, squirtle, 1, "DISPONIBLE", new BigDecimal("7.00"));

        PropuestaTrade propuesta = propuesta(alice, bob);
        propuesta.addItem(new ItemTrade(aliceOffer, RolItem.OFERTA, 1));
        propuesta.addItem(new ItemTrade(bobRequest, RolItem.SOLICITUD, 2));

        when(propuestaRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(propuesta));
        when(publicacionRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(aliceOffer));
        when(publicacionRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(bobRequest));
        when(publicacionRepository.findCartaDeUsuario(2L, 100L)).thenReturn(Optional.empty());
        when(publicacionRepository.findCartaDeUsuario(1L, 200L)).thenReturn(Optional.of(aliceSquirtle));

        PropuestaResponse response = tradeService.accept(4L, 2L);

        assertEquals("ACEPTADA", response.estado());
        assertEquals(EstadoPropuesta.ACEPTADA, propuesta.getEstado());
        assertEquals(1, aliceOffer.getStock());
        assertEquals(1, bobRequest.getStock());
        assertEquals(3, aliceSquirtle.getStock());
        verify(publicacionRepository, org.mockito.Mockito.times(2)).save(any(Publicacion.class));
    }

    @Test
    void onlyProposerCanCancel() {
        PropuestaTrade propuesta = propuesta(alice, bob);
        when(propuestaRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(propuesta));

        var error = assertThrows(ResponseStatusException.class, () -> tradeService.cancel(4L, 2L));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        assertEquals(EstadoPropuesta.PENDIENTE, propuesta.getEstado());
    }

    private static UsuarioRef usuario(Long id, String username) {
        UsuarioRef usuario = mock(UsuarioRef.class);
        when(usuario.getId()).thenReturn(id);
        when(usuario.getUsername()).thenReturn(username);
        return usuario;
    }

    private static CartaRef carta(Long id, String nombre) {
        CartaRef carta = mock(CartaRef.class);
        when(carta.getId()).thenReturn(id);
        when(carta.getNombre()).thenReturn(nombre);
        return carta;
    }

    private static Publicacion publicacion(
            Long id, UsuarioRef usuario, CartaRef carta, int stock, String estado, BigDecimal precio) {
        Publicacion publicacion = mock(Publicacion.class);
        AtomicInteger stockActual = new AtomicInteger(stock);
        when(publicacion.getId()).thenReturn(id);
        when(publicacion.getUsuario()).thenReturn(usuario);
        when(publicacion.getCarta()).thenReturn(carta);
        when(publicacion.getPrecioUnitario()).thenReturn(precio);
        when(publicacion.getEstadoConservacion()).thenReturn("Mint");
        when(publicacion.getEstadoPublicacion()).thenReturn(estado);
        when(publicacion.getStock()).thenAnswer(invocation -> stockActual.get());
        doAnswer(invocation -> {
            stockActual.set(invocation.getArgument(0));
            return null;
        }).when(publicacion).setStock(anyInt());
        return publicacion;
    }

    private static PropuestaTrade propuesta(UsuarioRef emisor, UsuarioRef receptor) {
        return new PropuestaTrade(emisor, receptor);
    }
}
