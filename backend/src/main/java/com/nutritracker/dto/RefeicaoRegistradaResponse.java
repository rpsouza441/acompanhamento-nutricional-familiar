package com.nutritracker.dto;

import com.nutritracker.model.RefeicaoRegistrada;
import java.time.LocalTime;
import java.util.List;

public record RefeicaoRegistradaResponse(
    Long id,
    Long refeicaoId,
    String nome,
    LocalTime horarioSugerido,
    LocalTime horarioRealizado,
    boolean concluida,
    String observacoes,
    List<AlimentoConsumidoResponse> alimentos) {
  public static RefeicaoRegistradaResponse from(
      RefeicaoRegistrada refeicao, List<AlimentoConsumidoResponse> alimentos) {
    return new RefeicaoRegistradaResponse(
        refeicao.getId(),
        refeicao.getRefeicao().getId(),
        refeicao.getRefeicao().getNome(),
        refeicao.getRefeicao().getHorarioSugerido(),
        refeicao.getHorarioRealizado(),
        refeicao.isConcluida(),
        refeicao.getObservacoes(),
        alimentos);
  }
}
