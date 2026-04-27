package com.nutritracker.controller;

import com.nutritracker.dto.ConquistaUsuarioResponse;
import com.nutritracker.service.ConquistaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conquistas")
public class ConquistaController {
  private final ConquistaService conquistaService;

  public ConquistaController(ConquistaService conquistaService) {
    this.conquistaService = conquistaService;
  }

  @GetMapping("/usuario/{id}")
  public List<ConquistaUsuarioResponse> listarDoUsuario(@PathVariable Long id) {
    return conquistaService.listarDoUsuario(id);
  }

  @PostMapping("/calcular/{usuarioId}")
  public List<ConquistaUsuarioResponse> calcular(@PathVariable Long usuarioId) {
    conquistaService.calcular(usuarioId);
    return conquistaService.listarDoUsuario(usuarioId);
  }
}
