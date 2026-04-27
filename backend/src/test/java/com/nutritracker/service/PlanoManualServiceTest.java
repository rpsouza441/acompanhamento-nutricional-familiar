package com.nutritracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutritracker.dto.PlanoManualCategoriaRequest;
import com.nutritracker.dto.PlanoManualOpcaoRequest;
import com.nutritracker.dto.PlanoManualRefeicaoRequest;
import com.nutritracker.dto.PlanoManualRequest;
import com.nutritracker.model.CategoriaRefeicao;
import com.nutritracker.model.OpcaoAlimento;
import com.nutritracker.model.PlanoNutricional;
import com.nutritracker.model.Refeicao;
import com.nutritracker.model.Role;
import com.nutritracker.model.TipoSelecao;
import com.nutritracker.model.Usuario;
import com.nutritracker.repository.CategoriaRefeicaoRepository;
import com.nutritracker.repository.OpcaoAlimentoRepository;
import com.nutritracker.repository.PlanoNutricionalRepository;
import com.nutritracker.repository.RefeicaoRepository;
import com.nutritracker.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanoManualServiceTest {
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private PlanoNutricionalRepository planoRepository;
  @Mock private RefeicaoRepository refeicaoRepository;
  @Mock private CategoriaRefeicaoRepository categoriaRepository;
  @Mock private OpcaoAlimentoRepository opcaoRepository;

  private PlanoManualService service;

  @BeforeEach
  void setUp() {
    service =
        new PlanoManualService(
            new ObjectMapper().findAndRegisterModules(),
            usuarioRepository,
            planoRepository,
            refeicaoRepository,
            categoriaRepository,
            opcaoRepository);
  }

  @Test
  void criarPlanoManualPersisteEstruturaCompleta() {
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario()));
    when(planoRepository.save(any(PlanoNutricional.class)))
        .thenAnswer(
            invocation -> {
              PlanoNutricional plano = invocation.getArgument(0);
              plano.setId(10L);
              return plano;
            });
    when(refeicaoRepository.save(any(Refeicao.class)))
        .thenAnswer(
            invocation -> {
              Refeicao refeicao = invocation.getArgument(0);
              refeicao.setId(20L);
              return refeicao;
            });
    when(categoriaRepository.save(any(CategoriaRefeicao.class)))
        .thenAnswer(
            invocation -> {
              CategoriaRefeicao categoria = invocation.getArgument(0);
              categoria.setId(30L);
              return categoria;
            });
    when(opcaoRepository.save(any(OpcaoAlimento.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var response = service.criar(request());

    assertThat(response.plano().id()).isEqualTo(10L);
    assertThat(response.plano().metaAguaDiariaMl()).isEqualTo(2500);
    assertThat(response.refeicoes()).isEqualTo(1);
    assertThat(response.categorias()).isEqualTo(1);
    assertThat(response.opcoes()).isEqualTo(2);
  }

  @Test
  void atualizarPlanoManualRemoveEstruturaAnteriorESalvaNova() {
    PlanoNutricional plano = new PlanoNutricional();
    plano.setId(10L);
    plano.setUsuario(usuario());
    plano.setJsonOriginal("{}");

    Refeicao refeicao = new Refeicao();
    refeicao.setId(20L);
    refeicao.setPlano(plano);
    CategoriaRefeicao categoria = new CategoriaRefeicao();
    categoria.setId(30L);
    categoria.setRefeicao(refeicao);
    OpcaoAlimento opcao = new OpcaoAlimento();
    opcao.setId(40L);
    opcao.setCategoria(categoria);

    when(planoRepository.findById(10L)).thenReturn(Optional.of(plano));
    when(planoRepository.save(any(PlanoNutricional.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(refeicaoRepository.findByPlanoIdOrderByOrdemAsc(10L)).thenReturn(List.of(refeicao));
    when(categoriaRepository.findByRefeicaoId(20L)).thenReturn(List.of(categoria));
    when(opcaoRepository.findByCategoriaId(30L)).thenReturn(List.of(opcao));
    when(refeicaoRepository.save(any(Refeicao.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(categoriaRepository.save(any(CategoriaRefeicao.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(opcaoRepository.save(any(OpcaoAlimento.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var response = service.atualizar(10L, request());

    assertThat(response.refeicoes()).isEqualTo(1);
    verify(opcaoRepository).delete(opcao);
    verify(categoriaRepository).delete(categoria);
    verify(refeicaoRepository).delete(refeicao);
  }

  private PlanoManualRequest request() {
    return new PlanoManualRequest(
        1L,
        "Nutricionista",
        "Reeducacao alimentar",
        2500,
        LocalDate.parse("2026-04-27"),
        true,
        List.of(
            new PlanoManualRefeicaoRequest(
                "desjejum",
                "Desjejum",
                LocalTime.parse("06:20"),
                1,
                List.of(
                    new PlanoManualCategoriaRequest(
                        "Proteina",
                        TipoSelecao.escolha_uma,
                        true,
                        List.of(
                            new PlanoManualOpcaoRequest(
                                "Leite desnatado", "1 copo", BigDecimal.valueOf(200), "ml"),
                            new PlanoManualOpcaoRequest(
                                "Iogurte natural", "1 pote", BigDecimal.valueOf(170), "g")))))));
  }

  private Usuario usuario() {
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    usuario.setNome("Maria");
    usuario.setEmail("maria@example.com");
    usuario.setSenhaHash("hash");
    usuario.setRole(Role.USER);
    usuario.setAtivo(true);
    return usuario;
  }
}
