package com.nutritracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nutritracker.dto.UsuarioRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private UsuarioService usuarioService;

  @BeforeEach
  void setUp() {
    usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
  }

  @Test
  void criarUsuarioCodificaSenhaEAplicaDefaults() {
    UsuarioRequest request =
        new UsuarioRequest("Maria Silva", "maria@example.com", "secret123", null, null);

    when(usuarioRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(false);
    when(passwordEncoder.encode("secret123")).thenReturn("hash");
    when(usuarioRepository.save(any(Usuario.class)))
        .thenAnswer(
            invocation -> {
              Usuario usuario = invocation.getArgument(0);
              usuario.setId(1L);
              return usuario;
            });

    Usuario usuario = usuarioService.criar(request);

    assertThat(usuario.getId()).isEqualTo(1L);
    assertThat(usuario.getNome()).isEqualTo("Maria Silva");
    assertThat(usuario.getEmail()).isEqualTo("maria@example.com");
    assertThat(usuario.getSenhaHash()).isEqualTo("hash");
    assertThat(usuario.getRole()).isEqualTo(Role.USER);
    assertThat(usuario.isAtivo()).isTrue();
  }

  @Test
  void criarUsuarioRejeitaEmailDuplicado() {
    UsuarioRequest request =
        new UsuarioRequest("Maria Silva", "maria@example.com", "secret123", Role.USER, true);

    when(usuarioRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(true);

    assertThatThrownBy(() -> usuarioService.criar(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage("E-mail ja cadastrado");

    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void alterarAtivoFazSoftDelete() {
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    usuario.setNome("Maria Silva");
    usuario.setEmail("maria@example.com");
    usuario.setSenhaHash("hash");
    usuario.setAtivo(true);

    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Usuario atualizado = usuarioService.alterarAtivo(1L, false);

    assertThat(atualizado.isAtivo()).isFalse();
  }
}
