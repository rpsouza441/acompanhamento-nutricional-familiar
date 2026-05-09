package com.nutritracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

import com.nutritracker.config.JwtService;
import com.nutritracker.dto.LoginRequest;
import com.nutritracker.exception.BusinessException;
import com.nutritracker.model.RefreshToken;
import com.nutritracker.model.Role;
import com.nutritracker.model.Usuario;
import com.nutritracker.repository.RefreshTokenRepository;
import com.nutritracker.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtService jwtService;
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private RefreshTokenRepository refreshTokenRepository;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(authenticationManager, jwtService, usuarioRepository, refreshTokenRepository);
  }

  @Test
  void loginAutenticaEGeraTokens() {
    Usuario usuario = usuario();
    LoginRequest request = new LoginRequest("admin@example.com", "password");

    when(usuarioRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(usuario));
    when(jwtService.generateAccessToken(usuario)).thenReturn("access");
    when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh");
    when(jwtService.getExpiration("refresh")).thenReturn(Instant.now().plusSeconds(3600));

    var response = authService.login(request);

    verify(authenticationManager)
        .authenticate(
            argThat(
                authentication ->
                    authentication instanceof UsernamePasswordAuthenticationToken
                        && "admin@example.com".equals(authentication.getPrincipal())
                        && "password".equals(authentication.getCredentials())));
    assertThat(response.accessToken()).isEqualTo("access");
    assertThat(response.refreshToken()).isEqualTo("refresh");
    assertThat(response.usuario().email()).isEqualTo("admin@example.com");
    verify(refreshTokenRepository)
        .save(
            argThat(
                token ->
                    token.getUsuario().equals(usuario)
                        && hash("refresh").equals(token.getTokenHash())
                        && token.getExpiraEm() != null));
  }

  @Test
  void refreshRejeitaTokenQueNaoERefresh() {
    when(jwtService.isRefreshToken("token")).thenReturn(false);

    assertThatThrownBy(() -> authService.refresh("token"))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Refresh token invalido");
  }

  @Test
  void refreshGeraAccessTokenQuandoTokenEstaPersistidoEAtivo() {
    Usuario usuario = usuario();
    RefreshToken refreshToken = refreshToken(usuario, null, Instant.now().plusSeconds(3600));

    when(jwtService.isRefreshToken("refresh")).thenReturn(true);
    when(jwtService.getSubject("refresh")).thenReturn("admin@example.com");
    when(refreshTokenRepository.findByTokenHash(hash("refresh"))).thenReturn(Optional.of(refreshToken));
    when(jwtService.generateAccessToken(usuario)).thenReturn("access-novo");

    var response = authService.refresh("refresh");

    assertThat(response.accessToken()).isEqualTo("access-novo");
  }

  @Test
  void logoutRevogaRefreshTokenPersistido() {
    RefreshToken refreshToken = refreshToken(usuario(), null, Instant.now().plusSeconds(3600));
    when(refreshTokenRepository.findByTokenHash(hash("refresh"))).thenReturn(Optional.of(refreshToken));

    authService.logout("refresh");

    assertThat(refreshToken.getRevogadoEm()).isNotNull();
    verify(refreshTokenRepository).save(refreshToken);
  }

  @Test
  void refreshRejeitaTokenDepoisDoLogout() {
    RefreshToken refreshToken = refreshToken(usuario(), Instant.now(), Instant.now().plusSeconds(3600));

    when(jwtService.isRefreshToken("refresh")).thenReturn(true);
    when(jwtService.getSubject("refresh")).thenReturn("admin@example.com");
    when(refreshTokenRepository.findByTokenHash(hash("refresh"))).thenReturn(Optional.of(refreshToken));

    assertThatThrownBy(() -> authService.refresh("refresh"))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Refresh token invalido");
  }

  private Usuario usuario() {
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    usuario.setNome("Admin");
    usuario.setEmail("admin@example.com");
    usuario.setSenhaHash("hash");
    usuario.setRole(Role.ADMIN);
    usuario.setAtivo(true);
    return usuario;
  }

  private RefreshToken refreshToken(Usuario usuario, Instant revogadoEm, Instant expiraEm) {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUsuario(usuario);
    refreshToken.setTokenHash(hash("refresh"));
    refreshToken.setRevogadoEm(revogadoEm);
    refreshToken.setExpiraEm(expiraEm);
    return refreshToken;
  }

  private String hash(String value) {
    try {
      return HexFormat.of()
          .formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }
}
