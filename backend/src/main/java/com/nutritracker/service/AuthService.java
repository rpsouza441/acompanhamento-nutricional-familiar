package com.nutritracker.service;

import com.nutritracker.config.JwtService;
import com.nutritracker.dto.LoginRequest;
import com.nutritracker.dto.LoginResponse;
import com.nutritracker.dto.RefreshResponse;
import com.nutritracker.dto.UsuarioResponse;
import com.nutritracker.exception.BusinessException;
import com.nutritracker.model.RefreshToken;
import com.nutritracker.model.Usuario;
import com.nutritracker.repository.RefreshTokenRepository;
import com.nutritracker.repository.UsuarioRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UsuarioRepository usuarioRepository;
  private final RefreshTokenRepository refreshTokenRepository;

  public AuthService(
      AuthenticationManager authenticationManager,
      JwtService jwtService,
      UsuarioRepository usuarioRepository,
      RefreshTokenRepository refreshTokenRepository) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.usuarioRepository = usuarioRepository;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Transactional
  public LoginResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
    var usuario =
        usuarioRepository
            .findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new BusinessException("Usuario nao encontrado"));
    String refreshToken = jwtService.generateRefreshToken(usuario);
    salvarRefreshToken(usuario, refreshToken);
    return new LoginResponse(
        jwtService.generateAccessToken(usuario),
        refreshToken,
        UsuarioResponse.from(usuario));
  }

  @Transactional(readOnly = true)
  public RefreshResponse refresh(String refreshToken) {
    String email = validarRefreshJwt(refreshToken);
    RefreshToken tokenPersistido =
        refreshTokenRepository
            .findByTokenHash(hash(refreshToken))
            .filter(token -> !token.isRevogado())
            .filter(token -> token.getExpiraEm().isAfter(Instant.now()))
            .orElseThrow(() -> new BusinessException("Refresh token invalido"));
    var usuario = tokenPersistido.getUsuario();
    if (!usuario.getEmail().equalsIgnoreCase(email) || !usuario.isAtivo()) {
      throw new BusinessException("Refresh token invalido");
    }
    return new RefreshResponse(jwtService.generateAccessToken(usuario));
  }

  @Transactional
  public void logout(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return;
    }
    refreshTokenRepository
        .findByTokenHash(hash(refreshToken))
        .filter(token -> !token.isRevogado())
        .ifPresent(
            token -> {
              token.setRevogadoEm(Instant.now());
              refreshTokenRepository.save(token);
            });
  }

  private void salvarRefreshToken(Usuario usuario, String token) {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUsuario(usuario);
    refreshToken.setTokenHash(hash(token));
    refreshToken.setExpiraEm(jwtService.getExpiration(token));
    refreshTokenRepository.save(refreshToken);
  }

  private String validarRefreshJwt(String refreshToken) {
    try {
      if (!jwtService.isRefreshToken(refreshToken)) {
        throw new BusinessException("Refresh token invalido");
      }
      return jwtService.getSubject(refreshToken);
    } catch (BusinessException exception) {
      throw exception;
    } catch (RuntimeException exception) {
      throw new BusinessException("Refresh token invalido");
    }
  }

  private String hash(String value) {
    try {
      return HexFormat.of()
          .formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 indisponivel", exception);
    }
  }
}
