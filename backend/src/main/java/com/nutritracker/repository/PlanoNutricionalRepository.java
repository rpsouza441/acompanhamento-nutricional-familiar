package com.nutritracker.repository;

import com.nutritracker.model.PlanoNutricional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoNutricionalRepository extends JpaRepository<PlanoNutricional, Long> {
  List<PlanoNutricional> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

  Optional<PlanoNutricional> findFirstByUsuarioIdAndAtivoTrueOrderByCriadoEmDesc(Long usuarioId);
}
