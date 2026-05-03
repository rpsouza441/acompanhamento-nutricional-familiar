package com.nutritracker.repository;

import com.nutritracker.model.CategoriaRefeicao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRefeicaoRepository extends JpaRepository<CategoriaRefeicao, Long> {
  List<CategoriaRefeicao> findByRefeicaoId(Long refeicaoId);

  List<CategoriaRefeicao> findByRefeicaoIdOrderByIdAsc(Long refeicaoId);
}
