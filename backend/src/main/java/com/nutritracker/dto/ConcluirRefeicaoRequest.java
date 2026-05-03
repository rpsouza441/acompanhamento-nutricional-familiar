package com.nutritracker.dto;

import java.time.LocalTime;

public record ConcluirRefeicaoRequest(Boolean concluida, LocalTime horarioRealizado, String observacoes) {}
