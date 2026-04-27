package com.nutritracker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;

public record PlanoManualRefeicaoRequest(
    @NotBlank @Size(max = 50) String identificador,
    @NotBlank @Size(max = 100) String nome,
    LocalTime horarioSugerido,
    @NotNull Integer ordem,
    @Valid @NotEmpty List<PlanoManualCategoriaRequest> categorias) {}
