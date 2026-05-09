package com.nutritracker.controller;

import com.nutritracker.dto.ConquistaUsuarioResponse;
import com.nutritracker.model.Usuario;
import com.nutritracker.service.AuthorizationService;
import com.nutritracker.service.ConquistaService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conquistas")
public class ConquistaController {
  private final ConquistaService conquistaService;
  private final AuthorizationService authorizationService;

  public ConquistaController(
      ConquistaService conquistaService, AuthorizationService authorizationService) {
    this.conquistaService = conquistaService;
    this.authorizationService = authorizationService;
  }

  @GetMapping("/usuario/{id}")
  public List<ConquistaUsuarioResponse> listarDoUsuario(
      @PathVariable Long id, @AuthenticationPrincipal Usuario autenticado) {
    Long usuarioId = authorizationService.resolveUsuarioId(autenticado, id);
    return conquistaService.listarDoUsuario(usuarioId);
  }

  @PostMapping("/calcular/{usuarioId}")
  public List<ConquistaUsuarioResponse> calcular(
      @PathVariable Long usuarioId, @AuthenticationPrincipal Usuario autenticado) {
    Long usuarioAutorizadoId = authorizationService.resolveUsuarioId(autenticado, usuarioId);
    conquistaService.calcular(usuarioAutorizadoId);
    return conquistaService.listarDoUsuario(usuarioAutorizadoId);
  }
}
