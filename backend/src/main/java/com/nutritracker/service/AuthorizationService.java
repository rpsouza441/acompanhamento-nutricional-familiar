package com.nutritracker.service;

import com.nutritracker.exception.BusinessException;
import com.nutritracker.model.Role;
import com.nutritracker.model.Usuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
  public Long resolveUsuarioId(Usuario autenticado, Long usuarioIdSolicitado) {
    requireAuthenticated(autenticado);
    if (isAdmin(autenticado)) {
      if (usuarioIdSolicitado == null) {
        throw new BusinessException("Informe o usuario");
      }
      return usuarioIdSolicitado;
    }
    if (usuarioIdSolicitado != null && !autenticado.getId().equals(usuarioIdSolicitado)) {
      deny();
    }
    return autenticado.getId();
  }

  public void requireOwnerOrAdmin(Usuario autenticado, Long usuarioIdRecurso) {
    requireAuthenticated(autenticado);
    if (!isAdmin(autenticado) && !autenticado.getId().equals(usuarioIdRecurso)) {
      deny();
    }
  }

  private boolean isAdmin(Usuario usuario) {
    return usuario.getRole() == Role.ADMIN;
  }

  private void requireAuthenticated(Usuario autenticado) {
    if (autenticado == null || autenticado.getId() == null) {
      deny();
    }
  }

  private void deny() {
    throw new AccessDeniedException("Acesso negado");
  }
}
