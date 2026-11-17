package com.tcg.catalogservice.repository;

import com.tcg.catalogservice.model.Publicacion;
import com.tcg.catalogservice.model.TipoItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    @EntityGraph(attributePaths = {"usuario", "carta", "carta.juego", "producto"})
    List<Publicacion> findAllByUsuario_IdOrderByIdDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "carta", "carta.juego", "producto"})
    Optional<Publicacion> findByIdAndUsuario_Id(Long id, Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "carta", "carta.juego", "producto"})
    Optional<Publicacion> findByUsuario_IdAndCarta_IdAndTipoItem(Long usuarioId, Long cartaId, TipoItem tipoItem);

    @EntityGraph(attributePaths = {"usuario", "carta", "carta.juego"})
    @Query("""
            select p from Publicacion p
            where p.estadoPublicacion = com.tcg.catalogservice.model.EstadoPublicacion.DISPONIBLE
              and p.stock > 0
              and p.carta is not null
              and p.usuario.id <> :excludeUserId
              and (:game = '' or lower(p.carta.juego.nombre) = lower(:game))
              and (:rarity = '' or lower(p.carta.rareza) = lower(:rarity))
              and (:query = '' or lower(p.carta.nombre) like lower(concat('%', :query, '%')))
            """)
    Page<Publicacion> searchAvailable(
            @Param("excludeUserId") Long excludeUserId,
            @Param("game") String game,
            @Param("rarity") String rarity,
            @Param("query") String query,
            Pageable pageable);
}
