package com.nutritracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutritracker.exception.ValidationException;
import com.nutritracker.model.CategoriaRefeicao;
import com.nutritracker.model.OpcaoAlimento;
import com.nutritracker.model.PlanoNutricional;
import com.nutritracker.model.Refeicao;
import com.nutritracker.model.Role;
import com.nutritracker.model.Usuario;
import com.nutritracker.repository.CategoriaRefeicaoRepository;
import com.nutritracker.repository.OpcaoAlimentoRepository;
import com.nutritracker.repository.PlanoNutricionalRepository;
import com.nutritracker.repository.RefeicaoRepository;
import com.nutritracker.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class PlanoImportacaoServiceTest {
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private PlanoNutricionalRepository planoRepository;
  @Mock private RefeicaoRepository refeicaoRepository;
  @Mock private CategoriaRefeicaoRepository categoriaRepository;
  @Mock private OpcaoAlimentoRepository opcaoRepository;

  private PlanoImportacaoService service;

  @BeforeEach
  void setUp() {
    service =
        new PlanoImportacaoService(
            new ObjectMapper(),
            usuarioRepository,
            planoRepository,
            refeicaoRepository,
            categoriaRepository,
            opcaoRepository);
  }

  @Test
  void importarPersistePlanoComRefeicoesCategoriasEOpcoes() {
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

    var response = service.importar(1L, arquivo("plano.json", jsonValido()));

    assertThat(response.plano().id()).isEqualTo(10L);
    assertThat(response.plano().metaAguaDiariaMl()).isEqualTo(3000);
    assertThat(response.refeicoes()).isEqualTo(1);
    assertThat(response.categorias()).isEqualTo(1);
    assertThat(response.opcoes()).isEqualTo(2);
    verify(planoRepository).save(any(PlanoNutricional.class));
    verify(refeicaoRepository).save(any(Refeicao.class));
    verify(categoriaRepository).save(any(CategoriaRefeicao.class));
  }

  @Test
  void importarRetornaErrosDetalhadosParaJsonInvalido() {
    String json =
        """
        {
          "configuracoes": { "meta_agua_diaria_ml": -1 },
          "refeicoes": [
            {
              "id": "",
              "nome": "Desjejum",
              "categorias": [
                {
                  "nome": "Proteina",
                  "tipo_selecao": "qualquer",
                  "opcoes": []
                }
              ]
            }
          ]
        }
        """;

    assertThatThrownBy(() -> service.importar(1L, arquivo("invalido.json", json)))
        .isInstanceOfSatisfying(
            ValidationException.class,
            exception ->
                assertThat(exception.getErrors())
                    .map(error -> error.field())
                    .contains(
                        "configuracoes.meta_agua_diaria_ml",
                        "refeicoes[0].id",
                        "refeicoes[0].ordem",
                        "refeicoes[0].categorias[0].tipo_selecao",
                        "refeicoes[0].categorias[0].opcoes"));
  }

  private MockMultipartFile arquivo(String nome, String conteudo) {
    return new MockMultipartFile("file", nome, "application/json", conteudo.getBytes());
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

  private String jsonValido() {
    return """
        {
          "configuracoes": {
            "meta_agua_diaria_ml": 3000,
            "objetivo": "Reeducacao alimentar",
            "profissional": "Nutricionista",
            "data_prescricao": "2025-10-29"
          },
          "refeicoes": [
            {
              "id": "desjejum",
              "nome": "Desjejum",
              "horario_sugerido": "06:20",
              "ordem": 1,
              "categorias": [
                {
                  "nome": "Proteina",
                  "tipo_selecao": "escolha_uma",
                  "obrigatorio": true,
                  "opcoes": [
                    {
                      "alimento": "Leite desnatado UHT",
                      "porcao": "0.5 copo",
                      "peso_valor": 120,
                      "unidade": "ml"
                    },
                    {
                      "alimento": "Iogurte natural desnatado",
                      "porcao": "1 pote",
                      "peso_valor": 170,
                      "unidade": "g"
                    }
                  ]
                }
              ]
            }
          ]
        }
        """;
  }
}
