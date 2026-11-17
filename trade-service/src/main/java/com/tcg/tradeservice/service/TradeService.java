package com.tcg.tradeservice.service;

import com.tcg.tradeservice.dto.CreateTradeRequest;
import com.tcg.tradeservice.dto.ItemTradeRequest;
import com.tcg.tradeservice.dto.PropuestaResponse;
import com.tcg.tradeservice.model.EstadoPropuesta;
import com.tcg.tradeservice.model.ItemTrade;
import com.tcg.tradeservice.model.PropuestaTrade;
import com.tcg.tradeservice.model.Publicacion;
import com.tcg.tradeservice.model.RolItem;
import com.tcg.tradeservice.model.UsuarioRef;
import com.tcg.tradeservice.repository.PropuestaTradeRepository;
import com.tcg.tradeservice.repository.PublicacionRepository;
import com.tcg.tradeservice.repository.UsuarioRefRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TradeService {

    private static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    private static final String TIPO_CARTA = "CARTA";

    private final PropuestaTradeRepository propuestaRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRefRepository usuarioRepository;

    public TradeService(
            PropuestaTradeRepository propuestaRepository,
            PublicacionRepository publicacionRepository,
            UsuarioRefRepository usuarioRepository) {
        this.propuestaRepository = propuestaRepository;
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public PropuestaResponse create(Long emisorId, CreateTradeRequest request) {
        UsuarioRef emisor = usuarioRepository.findById(emisorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        UsuarioRef receptor = usuarioRepository.findByUsername(request.receptorUsername().trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Receptor no encontrado: " + request.receptorUsername()));
        if (emisor.getId().equals(receptor.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes proponerte un trueque a ti mismo");
        }

        Map<Long, Integer> ofertas = normalizar(request.ofertas());
        Map<Long, Integer> solicitudes = normalizar(request.solicitudes());
        if (ofertas.isEmpty() && solicitudes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La propuesta debe incluir al menos una carta");
        }
        for (Long publicacionId : ofertas.keySet()) {
            if (solicitudes.containsKey(publicacionId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Una publicación no puede estar ofrecida y solicitada a la vez");
            }
        }

        List<Linea> lineasOferta = validarLineas(ofertas, emisor, "ofrecer");
        List<Linea> lineasSolicitud = validarLineas(solicitudes, receptor, "solicitar");

        Set<Long> cartasOfertadas = cartasDe(lineasOferta);
        for (Linea linea : lineasSolicitud) {
            if (cartasOfertadas.contains(linea.publicacion().getCarta().getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No puedes ofrecer y solicitar la misma carta");
            }
        }

        PropuestaTrade propuesta = new PropuestaTrade(emisor, receptor);
        lineasOferta.forEach(linea ->
                propuesta.addItem(new ItemTrade(linea.publicacion(), RolItem.OFERTA, linea.cantidad())));
        lineasSolicitud.forEach(linea ->
                propuesta.addItem(new ItemTrade(linea.publicacion(), RolItem.SOLICITUD, linea.cantidad())));

        return PropuestaResponse.from(propuestaRepository.save(propuesta));
    }

    @Transactional(readOnly = true)
    public List<PropuestaResponse> listForUser(Long userId) {
        UsuarioRef usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        return propuestaRepository.findAllForUser(usuario.getUsername()).stream()
                .map(PropuestaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PropuestaResponse getForUser(Long tradeId, Long userId) {
        PropuestaTrade propuesta = propuestaRepository.findDetailedById(tradeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trueque no encontrado"));
        if (!participa(propuesta, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trueque no encontrado");
        }
        return PropuestaResponse.from(propuesta);
    }

    @Transactional
    public PropuestaResponse accept(Long tradeId, Long userId) {
        PropuestaTrade propuesta = findPendiente(tradeId);
        if (!propuesta.getReceptor().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Solo el receptor puede aceptar esta propuesta");
        }

        List<ItemTrade> items = new ArrayList<>(propuesta.getItems());
        items.sort(Comparator.comparing(item -> item.getPublicacion().getId()));

        Map<Long, Publicacion> bloqueadas = new HashMap<>();
        for (ItemTrade item : items) {
            Long publicacionId = item.getPublicacion().getId();
            bloqueadas.computeIfAbsent(publicacionId, id -> publicacionRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT, "Una de las publicaciones ya no existe")));
        }

        for (ItemTrade item : items) {
            Publicacion publicacion = bloqueadas.get(item.getPublicacion().getId());
            Long propietarioEsperado = item.getRolItem() == RolItem.OFERTA
                    ? propuesta.getEmisor().getId()
                    : propuesta.getReceptor().getId();
            if (!publicacion.getUsuario().getId().equals(propietarioEsperado)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "La publicación ya no pertenece al usuario esperado");
            }
            if (!ESTADO_DISPONIBLE.equals(publicacion.getEstadoPublicacion())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Una de las cartas ya no está disponible");
            }
            if (publicacion.getStock() < item.getCantidad()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Una de las cartas ya no tiene stock suficiente");
            }
        }

        for (ItemTrade item : items) {
            Publicacion origen = bloqueadas.get(item.getPublicacion().getId());
            UsuarioRef destinoUsuario = item.getRolItem() == RolItem.OFERTA
                    ? propuesta.getReceptor()
                    : propuesta.getEmisor();
            origen.setStock(origen.getStock() - item.getCantidad());

            Publicacion destino = publicacionRepository
                    .findCartaDeUsuario(destinoUsuario.getId(), origen.getCarta().getId())
                    .orElseGet(() -> new Publicacion(
                            destinoUsuario,
                            origen.getCarta(),
                            origen.getPrecioUnitario(),
                            0,
                            origen.getEstadoConservacion(),
                            ESTADO_DISPONIBLE));
            destino.setStock(Math.addExact(destino.getStock(), item.getCantidad()));
            publicacionRepository.save(destino);
        }

        propuesta.setEstado(EstadoPropuesta.ACEPTADA);
        return PropuestaResponse.from(propuesta);
    }

    @Transactional
    public PropuestaResponse reject(Long tradeId, Long userId) {
        PropuestaTrade propuesta = findPendiente(tradeId);
        if (!propuesta.getReceptor().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Solo el receptor puede rechazar esta propuesta");
        }
        propuesta.setEstado(EstadoPropuesta.RECHAZADA);
        return PropuestaResponse.from(propuesta);
    }

    @Transactional
    public PropuestaResponse cancel(Long tradeId, Long userId) {
        PropuestaTrade propuesta = findPendiente(tradeId);
        if (!propuesta.getEmisor().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Solo el emisor puede cancelar esta propuesta");
        }
        propuesta.setEstado(EstadoPropuesta.CANCELADA);
        return PropuestaResponse.from(propuesta);
    }

    private PropuestaTrade findPendiente(Long tradeId) {
        PropuestaTrade propuesta = propuestaRepository.findByIdForUpdate(tradeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trueque no encontrado"));
        if (propuesta.getEstado() != EstadoPropuesta.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El trueque ya no está pendiente");
        }
        return propuesta;
    }

    private boolean participa(PropuestaTrade propuesta, Long userId) {
        return propuesta.getEmisor().getId().equals(userId) || propuesta.getReceptor().getId().equals(userId);
    }

    private Map<Long, Integer> normalizar(Collection<ItemTradeRequest> lineas) {
        Map<Long, Integer> resultado = new LinkedHashMap<>();
        if (lineas == null) {
            return resultado;
        }
        for (ItemTradeRequest linea : lineas) {
            if (linea.cantidad() < 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "La cantidad de cada carta debe ser mayor que cero");
            }
            resultado.merge(linea.publicacionId(), linea.cantidad(), Math::addExact);
        }
        return resultado;
    }

    private List<Linea> validarLineas(Map<Long, Integer> cantidades, UsuarioRef propietario, String accion) {
        List<Linea> lineas = new ArrayList<>();
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Publicacion publicacion = publicacionRepository.findById(entrada.getKey())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Publicación no encontrada: " + entrada.getKey()));
            if (!publicacion.getUsuario().getId().equals(propietario.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No puedes " + accion + " una publicación que no te pertenece");
            }
            if (!TIPO_CARTA.equals(publicacion.getTipoItem()) || publicacion.getCarta() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Solo se pueden intercambiar cartas");
            }
            if (!ESTADO_DISPONIBLE.equals(publicacion.getEstadoPublicacion())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "La carta '" + publicacion.getCarta().getNombre()
                        + "' no está disponible para intercambio");
            }
            if (publicacion.getStock() < entrada.getValue()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "No hay stock suficiente de la carta '"
                        + publicacion.getCarta().getNombre() + "'");
            }
            lineas.add(new Linea(publicacion, entrada.getValue()));
        }
        return lineas;
    }

    private Set<Long> cartasDe(List<Linea> lineas) {
        Set<Long> ids = new HashSet<>();
        lineas.forEach(linea -> ids.add(linea.publicacion().getCarta().getId()));
        return ids;
    }

    private record Linea(Publicacion publicacion, int cantidad) {
    }
}
