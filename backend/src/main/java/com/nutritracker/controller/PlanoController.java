package com.nutritracker.controller;

import com.nutritracker.dto.ImportacaoPlanoResponse;
import com.nutritracker.dto.PlanoManualRequest;
import com.nutritracker.dto.PlanoManualResponse;
import com.nutritracker.dto.PlanoResponse;
import com.nutritracker.exception.BusinessException;
import com.nutritracker.repository.PlanoNutricionalRepository;
import com.nutritracker.service.PlanoImportacaoService;
import com.nutritracker.service.PlanoManualService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/planos")
public class PlanoController {
  private final PlanoImportacaoService importacaoService;
  private final PlanoManualService planoManualService;
  private final PlanoNutricionalRepository planoRepository;

  public PlanoController(
      PlanoImportacaoService importacaoService,
      PlanoManualService planoManualService,
      PlanoNutricionalRepository planoRepository) {
    this.importacaoService = importacaoService;
    this.planoManualService = planoManualService;
    this.planoRepository = planoRepository;
  }

  @PostMapping("/importar")
  public ImportacaoPlanoResponse importar(
      @RequestParam Long usuarioId, @RequestParam("file") MultipartFile file) {
    return importacaoService.importar(usuarioId, file);
  }

  @PostMapping("/manual")
  public PlanoManualResponse criarManual(@Valid @RequestBody PlanoManualRequest request) {
    return planoManualService.criar(request);
  }

  @PutMapping("/{id}/manual")
  public PlanoManualResponse atualizarManual(
      @PathVariable Long id, @Valid @RequestBody PlanoManualRequest request) {
    return planoManualService.atualizar(id, request);
  }

  @GetMapping
  public List<PlanoResponse> listar(@RequestParam Long usuarioId) {
    return planoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId).stream()
        .map(PlanoResponse::from)
        .toList();
  }

  @GetMapping("/{id}")
  public PlanoResponse buscar(@PathVariable Long id) {
    return planoRepository
        .findById(id)
        .map(PlanoResponse::from)
        .orElseThrow(() -> new BusinessException("Plano nao encontrado"));
  }

  @PatchMapping("/{id}/ativar")
  public PlanoResponse ativar(@PathVariable Long id) {
    var plano =
        planoRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException("Plano nao encontrado"));
    plano.setAtivo(true);
    return PlanoResponse.from(planoRepository.save(plano));
  }
}
