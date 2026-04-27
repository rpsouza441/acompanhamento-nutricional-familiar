package com.nutritracker.controller;

import com.nutritracker.dto.AtivoRequest;
import com.nutritracker.dto.UsuarioRequest;
import com.nutritracker.dto.UsuarioResponse;
import com.nutritracker.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
  private final UsuarioService usuarioService;

  public UsuarioController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @GetMapping
  public List<UsuarioResponse> listar() {
    return usuarioService.listar().stream().map(UsuarioResponse::from).toList();
  }

  @PostMapping
  public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request) {
    return UsuarioResponse.from(usuarioService.criar(request));
  }

  @PutMapping("/{id}")
  public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
    return UsuarioResponse.from(usuarioService.atualizar(id, request));
  }

  @PatchMapping("/{id}/ativo")
  public UsuarioResponse alterarAtivo(@PathVariable Long id, @Valid @RequestBody AtivoRequest request) {
    return UsuarioResponse.from(usuarioService.alterarAtivo(id, request.ativo()));
  }
}
