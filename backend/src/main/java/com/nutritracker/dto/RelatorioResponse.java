package com.nutritracker.dto;

import java.time.LocalDate;
import java.util.List;

public record RelatorioResponse(
    Long usuarioId,
    String usuarioNome,
    LocalDate inicio,
    LocalDate fim,
    String profissional,
    double adesaoGeralPercentual,
    int maiorSequenciaDias,
    int diasRegistrados,
    int diasMetaAgua,
    List<RelatorioDiaResponse> dias,
    List<RelatorioRefeicaoResponse> refeicoes,
    List<RelatorioDetalheResponse> detalhes,
    List<ConquistaUsuarioResponse> conquistas) {}
