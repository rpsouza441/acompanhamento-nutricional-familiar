package com.nutritracker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record PlanoManualRequest(
    @NotNull Long usuarioId,
    @Size(max = 200) String profissional,
    String objetivo,
    @NotNull @Min(1) Integer metaAguaDiariaMl,
    LocalDate dataPrescricao,
    Boolean ativo,
    @Valid @NotEmpty List<PlanoManualRefeicaoRequest> refeicoes) {}
