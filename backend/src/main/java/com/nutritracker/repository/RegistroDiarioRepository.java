package com.nutritracker.repository;

import com.nutritracker.model.RegistroDiario;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroDiarioRepository extends JpaRepository<RegistroDiario, Long> {
  Optional<RegistroDiario> findByUsuarioIdAndDataRegistro(Long usuarioId, LocalDate dataRegistro);

  List<RegistroDiario> findByUsuarioIdAndDataRegistroBetweenOrderByDataRegistroAsc(
      Long usuarioId, LocalDate inicio, LocalDate fim);
}
