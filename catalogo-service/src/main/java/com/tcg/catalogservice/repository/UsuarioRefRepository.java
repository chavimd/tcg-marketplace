package com.tcg.catalogservice.repository;

import com.tcg.catalogservice.model.UsuarioRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRefRepository extends JpaRepository<UsuarioRef, Long> {

    Optional<UsuarioRef> findByUsername(String username);
}
