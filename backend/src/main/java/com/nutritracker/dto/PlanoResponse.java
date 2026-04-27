package com.nutritracker.dto;

import com.nutritracker.model.PlanoNutricional;
import java.time.Instant;
import java.time.LocalDate;

public record PlanoResponse(
    Long id,
    Long usuarioId,
    String profissional,
    String objetivo,
    Integer metaAguaDiariaMl,
    LocalDate dataPrescricao,
    boolean ativo,
    Instant criadoEm) {
  public static PlanoResponse from(PlanoNutricional plano) {
    return new PlanoResponse(
        plano.getId(),
        plano.getUsuario().getId(),
        plano.getProfissional(),
        plano.getObjetivo(),
        plano.getMetaAguaDiariaMl(),
        plano.getDataPrescricao(),
        plano.isAtivo(),
        plano.getCriadoEm());
  }
}
