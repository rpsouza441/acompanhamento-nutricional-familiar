package com.nutritracker.dto;

import com.nutritracker.model.TipoSelecao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PlanoManualCategoriaRequest(
    @NotBlank @Size(max = 100) String nome,
    @NotNull TipoSelecao tipoSelecao,
    Boolean obrigatorio,
    @Valid @NotEmpty List<PlanoManualOpcaoRequest> opcoes) {}
