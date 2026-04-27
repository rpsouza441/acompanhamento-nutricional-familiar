package com.nutritracker.repository;

import com.nutritracker.model.RefeicaoRegistrada;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefeicaoRegistradaRepository extends JpaRepository<RefeicaoRegistrada, Long> {
  List<RefeicaoRegistrada> findByRegistroDiarioId(Long registroDiarioId);

  Optional<RefeicaoRegistrada> findByRegistroDiarioIdAndRefeicaoId(Long registroDiarioId, Long refeicaoId);
}
