package com.nutritracker.repository;

import com.nutritracker.model.AlimentoConsumido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlimentoConsumidoRepository extends JpaRepository<AlimentoConsumido, Long> {
  List<AlimentoConsumido> findByRefeicaoRegistradaId(Long refeicaoRegistradaId);
}
