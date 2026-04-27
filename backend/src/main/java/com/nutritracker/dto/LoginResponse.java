package com.nutritracker.dto;

public record LoginResponse(String accessToken, String refreshToken, UsuarioResponse usuario) {}
