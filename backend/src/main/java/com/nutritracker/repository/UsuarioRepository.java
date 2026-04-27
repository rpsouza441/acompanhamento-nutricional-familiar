package com.nutritracker.repository;

import com.nutritracker.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);
}
