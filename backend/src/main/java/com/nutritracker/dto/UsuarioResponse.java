package com.nutritracker.dto;

import com.nutritracker.model.Role;
import com.nutritracker.model.Usuario;
import java.time.Instant;

public record UsuarioResponse(
    Long id, String nome, String email, Role role, boolean ativo, Instant criadoEm) {
  public static UsuarioResponse from(Usuario usuario) {
    return new UsuarioResponse(
        usuario.getId(),
        usuario.getNome(),
        usuario.getEmail(),
        usuario.getRole(),
        usuario.isAtivo(),
        usuario.getCriadoEm());
  }
}
