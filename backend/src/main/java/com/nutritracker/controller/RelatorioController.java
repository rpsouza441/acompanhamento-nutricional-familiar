package com.nutritracker.controller;

import com.nutritracker.dto.RelatorioResponse;
import com.nutritracker.model.Usuario;
import com.nutritracker.service.AuthorizationService;
import com.nutritracker.service.RelatorioService;
import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {
  private final RelatorioService relatorioService;
  private final AuthorizationService authorizationService;

  public RelatorioController(
      RelatorioService relatorioService, AuthorizationService authorizationService) {
    this.relatorioService = relatorioService;
    this.authorizationService = authorizationService;
  }

  @GetMapping
  public RelatorioResponse gerar(
      @RequestParam(required = false) Long usuarioId,
      @RequestParam LocalDate inicio,
      @RequestParam LocalDate fim,
      @AuthenticationPrincipal Usuario autenticado) {
    Long usuarioAutorizadoId = authorizationService.resolveUsuarioId(autenticado, usuarioId);
    return relatorioService.gerar(usuarioAutorizadoId, inicio, fim);
  }

  @GetMapping("/pdf")
  public ResponseEntity<byte[]> gerarPdf(
      @RequestParam(required = false) Long usuarioId,
      @RequestParam LocalDate inicio,
      @RequestParam LocalDate fim,
      @AuthenticationPrincipal Usuario autenticado) {
    Long usuarioAutorizadoId = authorizationService.resolveUsuarioId(autenticado, usuarioId);
    byte[] pdf = relatorioService.gerarPdf(usuarioAutorizadoId, inicio, fim);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=nutritracker-relatorio.pdf")
        .body(pdf);
  }
}
