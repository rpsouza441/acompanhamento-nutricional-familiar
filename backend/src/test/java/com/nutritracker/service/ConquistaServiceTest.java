package com.nutritracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nutritracker.model.Conquista;
import com.nutritracker.model.RegistroDiario;
import com.nutritracker.model.TipoConquista;
import com.nutritracker.model.Usuario;
import com.nutritracker.model.UsuarioConquista;
import com.nutritracker.repository.ConquistaRepository;
import com.nutritracker.repository.UsuarioConquistaRepository;
import com.nutritracker.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConquistaServiceTest {
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private ConquistaRepository conquistaRepository;
  @Mock private UsuarioConquistaRepository usuarioConquistaRepository;
  @Mock private AdesaoMetricsService metricsService;

  private ConquistaService conquistaService;

  @BeforeEach
  void setUp() {
    conquistaService =
        new ConquistaService(
            usuarioRepository, conquistaRepository, usuarioConquistaRepository, metricsService);
  }

  @Test
  void calcularDesbloqueiaConquistaQuandoMetaEAlcancada() {
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    Conquista conquista = conquista(TipoConquista.dias_totais, 7);
    List<RegistroDiario> registros = List.of(new RegistroDiario());

    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
    when(metricsService.registrosDoUsuario(1L)).thenReturn(registros);
    when(conquistaRepository.findAll()).thenReturn(List.of(conquista));
    when(metricsService.diasComAltaAdesao(registros)).thenReturn(7);
    when(usuarioConquistaRepository.existsByUsuarioIdAndConquistaId(1L, 10L)).thenReturn(false);

    conquistaService.calcular(1L);

    verify(usuarioConquistaRepository).save(any(UsuarioConquista.class));
  }

  @Test
  void listarDoUsuarioIncluiProgressoEDesbloqueio() {
    Conquista conquista = conquista(TipoConquista.agua_diaria, 30);
    UsuarioConquista desbloqueada = new UsuarioConquista();
    desbloqueada.setConquista(conquista);
    List<RegistroDiario> registros = List.of(new RegistroDiario());

    when(metricsService.registrosDoUsuario(1L)).thenReturn(registros);
    when(usuarioConquistaRepository.findByUsuarioId(1L)).thenReturn(List.of(desbloqueada));
    when(conquistaRepository.findAll()).thenReturn(List.of(conquista));
    when(metricsService.diasMetaAgua(registros)).thenReturn(12);

    var response = conquistaService.listarDoUsuario(1L);

    assertThat(response).hasSize(1);
    assertThat(response.getFirst().progresso()).isEqualTo(12);
    assertThat(response.getFirst().desbloqueada()).isTrue();
  }

  private Conquista conquista(TipoConquista tipo, int meta) {
    Conquista conquista = new Conquista();
    conquista.setId(10L);
    conquista.setCodigo("codigo");
    conquista.setNome("Conquista");
    conquista.setTipo(tipo);
    conquista.setValorMeta(meta);
    return conquista;
  }
}
