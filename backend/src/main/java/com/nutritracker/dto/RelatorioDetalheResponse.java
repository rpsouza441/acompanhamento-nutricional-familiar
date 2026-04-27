package com.nutritracker.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record RelatorioDetalheResponse(
    LocalDate data,
    String refeicao,
    String alimentosConsumidos,
    LocalTime horario,
    String observacoes) {}
