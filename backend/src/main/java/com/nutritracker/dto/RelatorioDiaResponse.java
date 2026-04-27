package com.nutritracker.dto;

import java.time.LocalDate;

public record RelatorioDiaResponse(
    LocalDate data,
    int aguaConsumidaMl,
    int metaAguaMl,
    int totalRefeicoes,
    int refeicoesConcluidas,
    double adesaoPercentual) {}
