package com.nutritracker.repository;

import com.nutritracker.model.OpcaoAlimento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpcaoAlimentoRepository extends JpaRepository<OpcaoAlimento, Long> {
  List<OpcaoAlimento> findByCategoriaId(Long categoriaId);
}
