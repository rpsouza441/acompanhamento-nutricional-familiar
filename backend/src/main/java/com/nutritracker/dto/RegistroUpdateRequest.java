package com.nutritracker.dto;

import jakarta.validation.constraints.Min;

public record RegistroUpdateRequest(@Min(0) Integer aguaConsumidaMl, String observacoesGerais) {}
