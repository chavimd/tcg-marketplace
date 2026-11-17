package com.tcg.catalogservice.service;

import com.tcg.catalogservice.dto.CartaRequest;
import com.tcg.catalogservice.dto.CartaResponse;
import com.tcg.catalogservice.dto.JuegoResponse;
import com.tcg.catalogservice.dto.JuegoRequest;
import com.tcg.catalogservice.dto.PublicacionGrantRequest;
import com.tcg.catalogservice.dto.PublicacionResponse;
import com.tcg.catalogservice.model.CartaCatalogo;
import com.tcg.catalogservice.model.EstadoPublicacion;
import com.tcg.catalogservice.model.JuegoTcg;
import com.tcg.catalogservice.model.Publicacion;
import com.tcg.catalogservice.model.TipoItem;
import com.tcg.catalogservice.model.UsuarioRef;
import com.tcg.catalogservice.repository.CartaCatalogoRepository;
import com.tcg.catalogservice.repository.JuegoTcgRepository;
import com.tcg.catalogservice.repository.PublicacionRepository;
import com.tcg.catalogservice.repository.UsuarioRefRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CatalogoService {

    private final JuegoTcgRepository juegoRepository;
    private final CartaCatalogoRepository cartaRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRefRepository usuarioRepository;

    public CatalogoService(
            JuegoTcgRepository juegoRepository,
            CartaCatalogoRepository cartaRepository,
            PublicacionRepository publicacionRepository,
            UsuarioRefRepository usuarioRepository) {
        this.juegoRepository = juegoRepository;
        this.cartaRepository = cartaRepository;
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<JuegoResponse> listGames() {
        return juegoRepository.findAllByOrderByNombreAsc().stream()
                .map(JuegoResponse::from)
                .toList();
    }

    @Transactional
    public JuegoResponse createGame(JuegoRequest request) {
        try {
            return JuegoResponse.from(juegoRepository.save(
                    new JuegoTcg(request.nombre().trim(), blankAsNull(request.descripcion()))));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese juego TCG ya existe");
        }
    }

    @Transactional(readOnly = true)
    public Page<CartaResponse> searchCards(String game, String rarity, String query, Pageable pageable) {
        return cartaRepository.search(blankAsEmpty(game), blankAsEmpty(rarity), blankAsEmpty(query), pageable)
                .map(CartaResponse::from);
    }

    @Transactional(readOnly = true)
    public CartaResponse getCard(Long id) {
        return CartaResponse.from(findCard(id));
    }

    @Transactional
    public CartaResponse createCard(CartaRequest request) {
        JuegoTcg juego = juegoRepository.findById(request.juegoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Juego TCG no encontrado"));
        CartaCatalogo carta = new CartaCatalogo(
                juego,
                request.nombre().trim(),
                request.numeroSet().trim(),
                request.setNombre().trim(),
                request.rareza().trim(),
                blankAsNull(request.tipo()),
                blankAsNull(request.color()),
                blankAsNull(request.textoCarta()),
                blankAsNull(request.imagenUrl()),
                request.metadataExtra());
        try {
            return CartaResponse.from(cartaRepository.save(carta));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe una carta con ese juego, set y nombre");
        }
    }

    @Transactional(readOnly = true)
    public Page<PublicacionResponse> searchAvailable(
            Long excludeUserId, String game, String rarity, String query, Pageable pageable) {
        return publicacionRepository
                .searchAvailable(
                        excludeUserId == null ? -1L : excludeUserId,
                        blankAsEmpty(game),
                        blankAsEmpty(rarity),
                        blankAsEmpty(query),
                        pageable)
                .map(PublicacionResponse::from);
    }

    @Transactional(readOnly = true)
    public PublicacionResponse getPublication(Long id) {
        return PublicacionResponse.from(publicacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada")));
    }

    @Transactional(readOnly = true)
    public List<PublicacionResponse> myPublications(Long usuarioId) {
        return publicacionRepository.findAllByUsuario_IdOrderByIdDesc(usuarioId).stream()
                .map(PublicacionResponse::from)
                .toList();
    }

    @Transactional
    public PublicacionResponse grantInventory(PublicacionGrantRequest request) {
        UsuarioRef usuario = usuarioRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado: " + request.username()));
        CartaCatalogo carta = findCard(request.cardId());
        BigDecimal precio = request.precioUnitario() == null ? BigDecimal.ONE : request.precioUnitario();
        if (precio.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor que cero");
        }

        Publicacion publicacion = publicacionRepository
                .findByUsuario_IdAndCarta_IdAndTipoItem(usuario.getId(), carta.getId(), TipoItem.CARTA)
                .orElse(null);
        if (publicacion == null) {
            publicacion = new Publicacion(usuario, carta, precio, request.quantity(), request.estadoConservacion());
        } else {
            publicacion.setStock(Math.addExact(publicacion.getStock(), request.quantity()));
            if (request.precioUnitario() != null) {
                publicacion.setPrecioUnitario(precio);
            }
            if (publicacion.getEstadoPublicacion() == EstadoPublicacion.OCULTA) {
                publicacion.setEstadoPublicacion(EstadoPublicacion.DISPONIBLE);
            }
        }
        return PublicacionResponse.from(publicacionRepository.save(publicacion));
    }

    @Transactional
    public PublicacionResponse updateStatus(Long usuarioId, Long publicacionId, EstadoPublicacion estado) {
        Publicacion publicacion = publicacionRepository.findByIdAndUsuario_Id(publicacionId, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));
        if (estado == EstadoPublicacion.RESERVADA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El estado RESERVADA solo lo gestiona el sistema de trueques");
        }
        if (estado == EstadoPublicacion.DISPONIBLE && publicacion.getStock() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes publicar una carta sin stock");
        }
        publicacion.setEstadoPublicacion(estado);
        return PublicacionResponse.from(publicacion);
    }

    private CartaCatalogo findCard(Long id) {
        return cartaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carta no encontrada"));
    }

    private String blankAsNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String blankAsEmpty(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }
}
