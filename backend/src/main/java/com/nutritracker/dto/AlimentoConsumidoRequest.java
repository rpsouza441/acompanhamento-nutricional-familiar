package com.nutritracker.dto;

import jakarta.validation.constraints.Size;

public record AlimentoConsumidoRequest(
    Long opcaoId,
    @Size(max = 300) String descricaoManual,
    @Size(max = 100) String quantidadePersonalizada) {}
