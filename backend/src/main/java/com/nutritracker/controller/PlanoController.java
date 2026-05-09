package com.nutritracker.controller;

import com.nutritracker.dto.ImportacaoPlanoResponse;
import com.nutritracker.dto.PlanoManualRequest;
import com.nutritracker.dto.PlanoManualResponse;
import com.nutritracker.dto.PlanoResponse;
import com.nutritracker.exception.BusinessException;
import com.nutritracker.model.PlanoNutricional;
import com.nutritracker.model.Usuario;
import com.nutritracker.repository.PlanoNutricionalRepository;
import com.nutritracker.service.AuthorizationService;
import com.nutritracker.service.PlanoImportacaoService;
import com.nutritracker.service.PlanoManualService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
  private final AuthorizationService authorizationService;

  public PlanoController(
      PlanoImportacaoService importacaoService,
      PlanoManualService planoManualService,
      PlanoNutricionalRepository planoRepository,
      AuthorizationService authorizationService) {
    this.importacaoService = importacaoService;
    this.planoManualService = planoManualService;
    this.planoRepository = planoRepository;
    this.authorizationService = authorizationService;
  }

  @PostMapping("/importar")
  public ImportacaoPlanoResponse importar(
      @RequestParam("file") MultipartFile file, @AuthenticationPrincipal Usuario autenticado) {
    return importacaoService.importar(autenticado.getEmail(), file);
  }

  @PostMapping("/manual")
  public PlanoManualResponse criarManual(
      @Valid @RequestBody PlanoManualRequest request, @AuthenticationPrincipal Usuario autenticado) {
    authorizationService.resolveUsuarioId(autenticado, request.usuarioId());
    return planoManualService.criar(request);
  }

  @PutMapping("/{id}/manual")
  public PlanoManualResponse atualizarManual(
      @PathVariable Long id,
      @Valid @RequestBody PlanoManualRequest request,
      @AuthenticationPrincipal Usuario autenticado) {
    authorizationService.resolveUsuarioId(autenticado, request.usuarioId());
    planoRepository
        .findById(id)
        .ifPresent(plano -> authorizationService.requireOwnerOrAdmin(autenticado, plano.getUsuario().getId()));
    return planoManualService.atualizar(id, request);
  }

  @GetMapping
  public List<PlanoResponse> listar(
      @RequestParam(required = false) Long usuarioId, @AuthenticationPrincipal Usuario autenticado) {
    Long usuarioAutorizadoId = authorizationService.resolveUsuarioId(autenticado, usuarioId);
    return planoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioAutorizadoId).stream()
        .map(PlanoResponse::from)
        .toList();
  }

  @GetMapping("/{id}")
  public PlanoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Usuario autenticado) {
    PlanoNutricional plano =
        planoRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException("Plano nao encontrado"));
    authorizationService.requireOwnerOrAdmin(autenticado, plano.getUsuario().getId());
    return PlanoResponse.from(plano);
  }

  @PatchMapping("/{id}/ativar")
  public PlanoResponse ativar(@PathVariable Long id, @AuthenticationPrincipal Usuario autenticado) {
    var plano =
        planoRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException("Plano nao encontrado"));
    authorizationService.requireOwnerOrAdmin(autenticado, plano.getUsuario().getId());
    planoRepository.findByUsuarioIdOrderByCriadoEmDesc(plano.getUsuario().getId()).stream()
        .filter(PlanoNutricional::isAtivo)
        .filter(item -> !item.getId().equals(plano.getId()))
        .forEach(
            item -> {
              item.setAtivo(false);
              planoRepository.save(item);
            });
    plano.setAtivo(true);
    return PlanoResponse.from(planoRepository.save(plano));
  }
}
