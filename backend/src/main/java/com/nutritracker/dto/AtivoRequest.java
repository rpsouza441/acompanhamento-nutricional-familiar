package com.nutritracker.dto;

import jakarta.validation.constraints.NotNull;

public record AtivoRequest(@NotNull Boolean ativo) {}
