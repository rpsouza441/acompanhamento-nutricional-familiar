package com.nutritracker.service;

import com.nutritracker.dto.ConquistaUsuarioResponse;
import com.nutritracker.exception.BusinessException;
import com.nutritracker.model.Conquista;
import com.nutritracker.model.TipoConquista;
import com.nutritracker.model.UsuarioConquista;
import com.nutritracker.repository.ConquistaRepository;
import com.nutritracker.repository.UsuarioConquistaRepository;
import com.nutritracker.repository.UsuarioRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConquistaService {
  private final UsuarioRepository usuarioRepository;
  private final ConquistaRepository conquistaRepository;
  private final UsuarioConquistaRepository usuarioConquistaRepository;
  private final AdesaoMetricsService metricsService;

  public ConquistaService(
      UsuarioRepository usuarioRepository,
      ConquistaRepository conquistaRepository,
      UsuarioConquistaRepository usuarioConquistaRepository,
      AdesaoMetricsService metricsService) {
    this.usuarioRepository = usuarioRepository;
    this.conquistaRepository = conquistaRepository;
    this.usuarioConquistaRepository = usuarioConquistaRepository;
    this.metricsService = metricsService;
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void calcularTodas() {
    usuarioRepository.findAll().stream()
        .filter(usuario -> usuario.isAtivo())
        .forEach(usuario -> calcular(usuario.getId()));
  }

  @Transactional
  public void calcular(Long usuarioId) {
    var usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new BusinessException("Usuario nao encontrado"));
    var registros = metricsService.registrosDoUsuario(usuarioId);

    for (Conquista conquista : conquistaRepository.findAll()) {
      int progresso = progresso(conquista, registros);
      if (progresso >= conquista.getValorMeta()
          && !usuarioConquistaRepository.existsByUsuarioIdAndConquistaId(
              usuarioId, conquista.getId())) {
        UsuarioConquista usuarioConquista = new UsuarioConquista();
        usuarioConquista.setUsuario(usuario);
        usuarioConquista.setConquista(conquista);
        usuarioConquistaRepository.save(usuarioConquista);
      }
    }
  }

  @Transactional(readOnly = true)
  public List<ConquistaUsuarioResponse> listarDoUsuario(Long usuarioId) {
    var registros = metricsService.registrosDoUsuario(usuarioId);
    Map<Long, UsuarioConquista> desbloqueadas =
        usuarioConquistaRepository.findByUsuarioId(usuarioId).stream()
            .collect(Collectors.toMap(item -> item.getConquista().getId(), item -> item));

    return conquistaRepository.findAll().stream()
        .map(
            conquista -> {
              UsuarioConquista usuarioConquista = desbloqueadas.get(conquista.getId());
              Instant desbloqueadaEm =
                  usuarioConquista == null ? null : usuarioConquista.getDesbloqueadaEm();
              return ConquistaUsuarioResponse.of(
                  conquista,
                  progresso(conquista, registros),
                  usuarioConquista != null,
                  desbloqueadaEm);
            })
        .toList();
  }

  private int progresso(Conquista conquista, List<com.nutritracker.model.RegistroDiario> registros) {
    TipoConquista tipo = conquista.getTipo();
    if (tipo == TipoConquista.dias_consecutivos) {
      return metricsService.maiorSequenciaAltaAdesao(registros);
    }
    if (tipo == TipoConquista.dias_totais) {
      return metricsService.diasComAltaAdesao(registros);
    }
    if (tipo == TipoConquista.agua_diaria) {
      return metricsService.diasMetaAgua(registros);
    }
    if (tipo == TipoConquista.adesao_percentual) {
      return registros.isEmpty()
          ? 0
          : (int)
              Math.round(
                  registros.stream().mapToDouble(metricsService::adesaoPercentual).average().orElse(0));
    }
    return 0;
  }
}
