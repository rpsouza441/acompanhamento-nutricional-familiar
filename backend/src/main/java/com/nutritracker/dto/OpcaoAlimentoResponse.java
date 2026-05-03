package com.nutritracker.dto;

import com.nutritracker.model.OpcaoAlimento;
import java.math.BigDecimal;

public record OpcaoAlimentoResponse(
    Long id, String alimento, String porcao, BigDecimal pesoValor, String unidade) {
  public static OpcaoAlimentoResponse from(OpcaoAlimento opcao) {
    return new OpcaoAlimentoResponse(
        opcao.getId(), opcao.getAlimento(), opcao.getPorcao(), opcao.getPesoValor(), opcao.getUnidade());
  }
}
