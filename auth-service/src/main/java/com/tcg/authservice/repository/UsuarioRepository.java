package com.tcg.authservice.repository;

import com.tcg.authservice.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar un usuario por su nombre para autenticación.
    Optional<Usuario> findByUsername(String username);

    // Verificar duplicados antes de registrar.
    boolean existsByEmail(String email);

    // Verificar si un nombre de usuario ya está ocupado
    boolean existsByUsername(String username);
}