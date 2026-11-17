package com.tcg.catalogservice.repository;

import com.tcg.catalogservice.model.JuegoTcg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JuegoTcgRepository extends JpaRepository<JuegoTcg, Integer> {

    List<JuegoTcg> findAllByOrderByNombreAsc();

    Optional<JuegoTcg> findByNombreIgnoreCase(String nombre);
}
