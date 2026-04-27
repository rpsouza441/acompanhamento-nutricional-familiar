package com.nutritracker.dto;

import java.time.LocalTime;

public record ConcluirRefeicaoRequest(LocalTime horarioRealizado, String observacoes) {}
