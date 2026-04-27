package com.nutritracker.dto;

import com.nutritracker.model.Conquista;
import java.time.Instant;

public record ConquistaUsuarioResponse(
    Long id,
    String codigo,
    String nome,
    String descricao,
    String icone,
    String tipo,
    Integer valorMeta,
    Integer progresso,
    boolean desbloqueada,
    Instant desbloqueadaEm) {
  public static ConquistaUsuarioResponse of(
      Conquista conquista, int progresso, boolean desbloqueada, Instant desbloqueadaEm) {
    return new ConquistaUsuarioResponse(
        conquista.getId(),
        conquista.getCodigo(),
        conquista.getNome(),
        conquista.getDescricao(),
        conquista.getIcone(),
        conquista.getTipo().name(),
        conquista.getValorMeta(),
        Math.min(progresso, conquista.getValorMeta()),
        desbloqueada,
        desbloqueadaEm);
  }
}
