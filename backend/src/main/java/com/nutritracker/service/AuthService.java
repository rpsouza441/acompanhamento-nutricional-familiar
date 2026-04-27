package com.nutritracker.service;

import com.nutritracker.config.JwtService;
import com.nutritracker.dto.LoginRequest;
import com.nutritracker.dto.LoginResponse;
import com.nutritracker.dto.RefreshResponse;
import com.nutritracker.dto.UsuarioResponse;
import com.nutritracker.exception.BusinessException;
import com.nutritracker.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UsuarioRepository usuarioRepository;

  public AuthService(
      AuthenticationManager authenticationManager,
      JwtService jwtService,
      UsuarioRepository usuarioRepository) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.usuarioRepository = usuarioRepository;
  }

  public LoginResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
    var usuario =
        usuarioRepository
            .findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new BusinessException("Usuario nao encontrado"));
    return new LoginResponse(
        jwtService.generateAccessToken(usuario),
        jwtService.generateRefreshToken(usuario),
        UsuarioResponse.from(usuario));
  }

  public RefreshResponse refresh(String refreshToken) {
    if (!jwtService.isRefreshToken(refreshToken)) {
      throw new BusinessException("Refresh token invalido");
    }
    String email = jwtService.getSubject(refreshToken);
    var usuario =
        usuarioRepository
            .findByEmailIgnoreCase(email)
            .filter(found -> found.isAtivo())
            .orElseThrow(() -> new BusinessException("Usuario nao encontrado"));
    return new RefreshResponse(jwtService.generateAccessToken(usuario));
  }
}
