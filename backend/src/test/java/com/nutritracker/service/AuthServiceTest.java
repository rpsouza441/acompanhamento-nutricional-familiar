package com.nutritracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

import com.nutritracker.config.JwtService;
import com.nutritracker.dto.LoginRequest;
import com.nutritracker.exception.BusinessException;
import com.nutritracker.model.Role;
import com.nutritracker.model.Usuario;
import com.nutritracker.repository.UsuarioRepository;
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

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(authenticationManager, jwtService, usuarioRepository);
  }

  @Test
  void loginAutenticaEGeraTokens() {
    Usuario usuario = usuario();
    LoginRequest request = new LoginRequest("admin@example.com", "password");

    when(usuarioRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(usuario));
    when(jwtService.generateAccessToken(usuario)).thenReturn("access");
    when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh");

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
  }

  @Test
  void refreshRejeitaTokenQueNaoERefresh() {
    when(jwtService.isRefreshToken("token")).thenReturn(false);

    assertThatThrownBy(() -> authService.refresh("token"))
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
}
