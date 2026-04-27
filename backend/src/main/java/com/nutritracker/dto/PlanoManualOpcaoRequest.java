package com.nutritracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PlanoManualOpcaoRequest(
    @NotBlank @Size(max = 200) String alimento,
    @Size(max = 100) String porcao,
    BigDecimal pesoValor,
    @Size(max = 20) String unidade) {}
