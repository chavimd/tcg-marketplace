package com.tcg.tradeservice.repository;

import com.tcg.tradeservice.model.Publicacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Publicacion p where p.id = :id")
    Optional<Publicacion> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select p from Publicacion p
            where p.usuario.id = :usuarioId
              and p.carta.id = :cartaId
              and p.tipoItem = 'CARTA'
            """)
    Optional<Publicacion> findCartaDeUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("cartaId") Long cartaId);
}
