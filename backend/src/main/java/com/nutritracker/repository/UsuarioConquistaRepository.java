package com.nutritracker.repository;

import com.nutritracker.model.UsuarioConquista;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioConquistaRepository extends JpaRepository<UsuarioConquista, Long> {
  List<UsuarioConquista> findByUsuarioId(Long usuarioId);

  boolean existsByUsuarioIdAndConquistaId(Long usuarioId, Long conquistaId);
}
