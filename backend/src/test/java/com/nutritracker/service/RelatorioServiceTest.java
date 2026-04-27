package com.nutritracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nutritracker.model.PlanoNutricional;
import com.nutritracker.model.Refeicao;
import com.nutritracker.model.RefeicaoRegistrada;
import com.nutritracker.model.RegistroDiario;
import com.nutritracker.model.Usuario;
import com.nutritracker.repository.AlimentoConsumidoRepository;
import com.nutritracker.repository.RefeicaoRegistradaRepository;
import com.nutritracker.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private AdesaoMetricsService metricsService;
  @Mock private RefeicaoRegistradaRepository refeicaoRegistradaRepository;
  @Mock private AlimentoConsumidoRepository alimentoConsumidoRepository;
  @Mock private ConquistaService conquistaService;

  private RelatorioService relatorioService;

  @BeforeEach
  void setUp() {
    relatorioService =
        new RelatorioService(
            usuarioRepository,
            metricsService,
            refeicaoRegistradaRepository,
            alimentoConsumidoRepository,
            conquistaService);
  }

  @Test
  void gerarRelatorioAgregaAdesaoAguaERefeicoes() {
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    usuario.setNome("Maria");
    RegistroDiario registro = registro(usuario);
    RefeicaoRegistrada refeicao = refeicaoRegistrada(registro, true);

    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
    when(metricsService.registrosNoPeriodo(1L, LocalDate.parse("2026-04-01"), LocalDate.parse("2026-04-30")))
        .thenReturn(List.of(registro));
    when(refeicaoRegistradaRepository.findByRegistroDiarioId(100L)).thenReturn(List.of(refeicao));
    when(alimentoConsumidoRepository.findByRefeicaoRegistradaId(200L)).thenReturn(List.of());
    when(metricsService.maiorSequenciaAltaAdesao(List.of(registro))).thenReturn(1);
    when(conquistaService.listarDoUsuario(1L)).thenReturn(List.of());

    var relatorio =
        relatorioService.gerar(1L, LocalDate.parse("2026-04-01"), LocalDate.parse("2026-04-30"));

    assertThat(relatorio.usuarioNome()).isEqualTo("Maria");
    assertThat(relatorio.adesaoGeralPercentual()).isEqualTo(100.0);
    assertThat(relatorio.diasMetaAgua()).isEqualTo(1);
    assertThat(relatorio.refeicoes()).hasSize(1);
    assertThat(relatorio.detalhes()).hasSize(1);
  }

  private RegistroDiario registro(Usuario usuario) {
    PlanoNutricional plano = new PlanoNutricional();
    plano.setId(50L);
    plano.setUsuario(usuario);
    plano.setProfissional("Nutricionista");
    plano.setMetaAguaDiariaMl(3000);
    plano.setJsonOriginal("{}");

    RegistroDiario registro = new RegistroDiario();
    registro.setId(100L);
    registro.setUsuario(usuario);
    registro.setPlano(plano);
    registro.setDataRegistro(LocalDate.parse("2026-04-10"));
    registro.setAguaConsumidaMl(3000);
    return registro;
  }

  private RefeicaoRegistrada refeicaoRegistrada(RegistroDiario registro, boolean concluida) {
    Refeicao refeicao = new Refeicao();
    refeicao.setId(300L);
    refeicao.setNome("Desjejum");

    RefeicaoRegistrada registrada = new RefeicaoRegistrada();
    registrada.setId(200L);
    registrada.setRegistroDiario(registro);
    registrada.setRefeicao(refeicao);
    registrada.setConcluida(concluida);
    return registrada;
  }
}
