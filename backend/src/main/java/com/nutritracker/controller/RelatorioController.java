package com.nutritracker.controller;

import com.nutritracker.dto.RelatorioResponse;
import com.nutritracker.service.RelatorioService;
import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {
  private final RelatorioService relatorioService;

  public RelatorioController(RelatorioService relatorioService) {
    this.relatorioService = relatorioService;
  }

  @GetMapping
  public RelatorioResponse gerar(
      @RequestParam Long usuarioId, @RequestParam LocalDate inicio, @RequestParam LocalDate fim) {
    return relatorioService.gerar(usuarioId, inicio, fim);
  }

  @GetMapping("/pdf")
  public ResponseEntity<byte[]> gerarPdf(
      @RequestParam Long usuarioId, @RequestParam LocalDate inicio, @RequestParam LocalDate fim) {
    byte[] pdf = relatorioService.gerarPdf(usuarioId, inicio, fim);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=nutritracker-relatorio.pdf")
        .body(pdf);
  }
}
