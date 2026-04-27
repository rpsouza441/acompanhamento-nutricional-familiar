package com.nutritracker.dto;

import com.nutritracker.model.AlimentoConsumido;

public record AlimentoConsumidoResponse(
    Long id, Long opcaoId, String descricao, String quantidadePersonalizada) {
  public static AlimentoConsumidoResponse from(AlimentoConsumido alimento) {
    String descricao =
        alimento.getOpcao() != null ? alimento.getOpcao().getAlimento() : alimento.getDescricaoManual();
    Long opcaoId = alimento.getOpcao() != null ? alimento.getOpcao().getId() : null;
    return new AlimentoConsumidoResponse(
        alimento.getId(), opcaoId, descricao, alimento.getQuantidadePersonalizada());
  }
}
