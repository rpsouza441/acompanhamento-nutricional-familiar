package com.nutritracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nutritracker.exception.BusinessException;
import com.nutritracker.model.Role;
import com.nutritracker.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class AuthorizationServiceTest {
  private final AuthorizationService service = new AuthorizationService();

  @Test
  void userAcessaProprioUsuarioMesmoSemUsuarioIdNaRequisicao() {
    assertThat(service.resolveUsuarioId(usuario(1L, Role.USER), null)).isEqualTo(1L);
  }

  @Test
  void userNaoAcessaOutroUsuario() {
    Usuario autenticado = usuario(1L, Role.USER);

    assertThatThrownBy(() -> service.resolveUsuarioId(autenticado, 2L))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void adminAcessaQualquerUsuarioQuandoIdEInformado() {
    assertThat(service.resolveUsuarioId(usuario(1L, Role.ADMIN), 2L)).isEqualTo(2L);
  }

  @Test
  void adminPrecisaInformarUsuarioAlvo() {
    assertThatThrownBy(() -> service.resolveUsuarioId(usuario(1L, Role.ADMIN), null))
        .isInstanceOf(BusinessException.class)
        .hasMessage("Informe o usuario");
  }

  private Usuario usuario(Long id, Role role) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setRole(role);
    return usuario;
  }
}
