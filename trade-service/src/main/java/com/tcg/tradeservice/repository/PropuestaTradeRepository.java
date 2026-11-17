package com.tcg.tradeservice.repository;

import com.tcg.tradeservice.model.PropuestaTrade;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropuestaTradeRepository extends JpaRepository<PropuestaTrade, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PropuestaTrade p where p.id = :id")
    Optional<PropuestaTrade> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {
            "emisor", "receptor",
            "items", "items.publicacion", "items.publicacion.carta",
            "items.publicacion.carta.juego", "items.publicacion.usuario"})
    @Query("""
            select p from PropuestaTrade p
            where p.emisor.username = :username or p.receptor.username = :username
            order by p.createdAt desc, p.id desc
            """)
    List<PropuestaTrade> findAllForUser(@Param("username") String username);

    @EntityGraph(attributePaths = {
            "emisor", "receptor",
            "items", "items.publicacion", "items.publicacion.carta",
            "items.publicacion.carta.juego", "items.publicacion.usuario"})
    @Query("select p from PropuestaTrade p where p.id = :id")
    Optional<PropuestaTrade> findDetailedById(@Param("id") Long id);
}
