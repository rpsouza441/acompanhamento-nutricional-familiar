package com.nutritracker.repository;

import com.nutritracker.model.Refeicao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefeicaoRepository extends JpaRepository<Refeicao, Long> {
  List<Refeicao> findByPlanoIdOrderByOrdemAsc(Long planoId);
}
