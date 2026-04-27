package com.nutritracker.controller;

import com.nutritracker.dto.AlimentoConsumidoRequest;
import com.nutritracker.dto.ConcluirRefeicaoRequest;
import com.nutritracker.dto.RegistroResponse;
import com.nutritracker.dto.RegistroUpdateRequest;
import com.nutritracker.service.RegistroService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registros")
public class RegistroController {
  private final RegistroService registroService;

  public RegistroController(RegistroService registroService) {
    this.registroService = registroService;
  }

  @GetMapping
  public RegistroResponse buscarOuCriar(@RequestParam Long usuarioId, @RequestParam LocalDate data) {
    return registroService.buscarOuCriar(usuarioId, data);
  }

  @PutMapping("/{id}")
  public RegistroResponse atualizar(
      @PathVariable Long id, @Valid @RequestBody RegistroUpdateRequest request) {
    return registroService.atualizar(id, request);
  }

  @PostMapping("/{id}/refeicoes/{refeicaoId}/concluir")
  public RegistroResponse concluirRefeicao(
      @PathVariable Long id,
      @PathVariable Long refeicaoId,
      @RequestBody ConcluirRefeicaoRequest request) {
    return registroService.concluirRefeicao(id, refeicaoId, request);
  }

  @PostMapping("/{id}/refeicoes/{refeicaoId}/alimentos")
  public RegistroResponse adicionarAlimento(
      @PathVariable Long id,
      @PathVariable Long refeicaoId,
      @Valid @RequestBody AlimentoConsumidoRequest request) {
    return registroService.adicionarAlimento(id, refeicaoId, request);
  }

  @DeleteMapping("/{registroId}/alimentos/{alimentoId}")
  public RegistroResponse removerAlimento(@PathVariable Long registroId, @PathVariable Long alimentoId) {
    return registroService.removerAlimento(registroId, alimentoId);
  }
}
