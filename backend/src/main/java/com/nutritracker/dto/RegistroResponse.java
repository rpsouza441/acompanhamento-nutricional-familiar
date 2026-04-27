package com.nutritracker.dto;

import com.nutritracker.model.RegistroDiario;
import java.time.LocalDate;
import java.util.List;

public record RegistroResponse(
    Long id,
    Long usuarioId,
    Long planoId,
    LocalDate dataRegistro,
    Integer aguaConsumidaMl,
    Integer metaAguaDiariaMl,
    String observacoesGerais,
    List<RefeicaoRegistradaResponse> refeicoes) {
  public static RegistroResponse from(
      RegistroDiario registro, List<RefeicaoRegistradaResponse> refeicoes) {
    return new RegistroResponse(
        registro.getId(),
        registro.getUsuario().getId(),
        registro.getPlano().getId(),
        registro.getDataRegistro(),
        registro.getAguaConsumidaMl(),
        registro.getPlano().getMetaAguaDiariaMl(),
        registro.getObservacoesGerais(),
        refeicoes);
  }
}
