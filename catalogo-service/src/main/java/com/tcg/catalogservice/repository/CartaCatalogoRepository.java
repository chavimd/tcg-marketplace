package com.tcg.catalogservice.repository;

import com.tcg.catalogservice.model.CartaCatalogo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartaCatalogoRepository extends JpaRepository<CartaCatalogo, Long> {

    @EntityGraph(attributePaths = "juego")
    @Query("""
            select c from CartaCatalogo c
            where (:game = '' or lower(c.juego.nombre) = lower(:game))
              and (:rarity = '' or lower(c.rareza) = lower(:rarity))
              and (:query = '' or lower(c.nombre) like lower(concat('%', :query, '%')))
            """)
    Page<CartaCatalogo> search(
            @Param("game") String game,
            @Param("rarity") String rarity,
            @Param("query") String query,
            Pageable pageable);
}
