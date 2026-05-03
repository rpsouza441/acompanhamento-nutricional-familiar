package com.nutritracker.dto;

import com.nutritracker.model.CategoriaRefeicao;
import com.nutritracker.model.TipoSelecao;
import java.util.List;

public record CategoriaRefeicaoResponse(
    Long id,
    String nome,
    TipoSelecao tipoSelecao,
    boolean obrigatorio,
    List<OpcaoAlimentoResponse> opcoes) {
  public static CategoriaRefeicaoResponse from(
      CategoriaRefeicao categoria, List<OpcaoAlimentoResponse> opcoes) {
    return new CategoriaRefeicaoResponse(
        categoria.getId(),
        categoria.getNome(),
        categoria.getTipoSelecao(),
        categoria.isObrigatorio(),
        opcoes);
  }
}
