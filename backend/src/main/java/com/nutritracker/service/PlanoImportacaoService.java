package com.nutritracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutritracker.dto.ImportacaoPlanoResponse;
import com.nutritracker.dto.PlanoResponse;
import com.nutritracker.exception.ApiError;
import com.nutritracker.exception.BusinessException;
import com.nutritracker.exception.ValidationException;
import com.nutritracker.model.CategoriaRefeicao;
import com.nutritracker.model.OpcaoAlimento;
import com.nutritracker.model.PlanoNutricional;
import com.nutritracker.model.Refeicao;
import com.nutritracker.model.TipoSelecao;
import com.nutritracker.repository.CategoriaRefeicaoRepository;
import com.nutritracker.repository.OpcaoAlimentoRepository;
import com.nutritracker.repository.PlanoNutricionalRepository;
import com.nutritracker.repository.RefeicaoRepository;
import com.nutritracker.repository.UsuarioRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PlanoImportacaoService {
  private final ObjectMapper objectMapper;
  private final UsuarioRepository usuarioRepository;
  private final PlanoNutricionalRepository planoRepository;
  private final RefeicaoRepository refeicaoRepository;
  private final CategoriaRefeicaoRepository categoriaRepository;
  private final OpcaoAlimentoRepository opcaoRepository;

  public PlanoImportacaoService(
      ObjectMapper objectMapper,
      UsuarioRepository usuarioRepository,
      PlanoNutricionalRepository planoRepository,
      RefeicaoRepository refeicaoRepository,
      CategoriaRefeicaoRepository categoriaRepository,
      OpcaoAlimentoRepository opcaoRepository) {
    this.objectMapper = objectMapper;
    this.usuarioRepository = usuarioRepository;
    this.planoRepository = planoRepository;
    this.refeicaoRepository = refeicaoRepository;
    this.categoriaRepository = categoriaRepository;
    this.opcaoRepository = opcaoRepository;
  }

  @Transactional
  public ImportacaoPlanoResponse importar(String emailUsuario, MultipartFile arquivo) {
    if (arquivo == null || arquivo.isEmpty()) {
      throw new BusinessException("Arquivo JSON e obrigatorio");
    }

    JsonNode root = parse(arquivo);
    validar(root);

    var usuario =
        usuarioRepository
            .findByEmailIgnoreCase(emailUsuario)
            .orElseThrow(() -> new BusinessException("Usuario nao encontrado"));
    desativarPlanosAtivos(usuario.getId());

    JsonNode configuracoes = root.path("configuracoes");
    PlanoNutricional plano = new PlanoNutricional();
    plano.setUsuario(usuario);
    plano.setProfissional(text(configuracoes, "profissional"));
    plano.setObjetivo(text(configuracoes, "objetivo"));
    plano.setMetaAguaDiariaMl(configuracoes.path("meta_agua_diaria_ml").asInt(3000));
    plano.setDataPrescricao(date(configuracoes, "data_prescricao"));
    plano.setJsonOriginal(root.toString());
    plano.setAtivo(true);
    plano = planoRepository.save(plano);

    int refeicoes = 0;
    int categorias = 0;
    int opcoes = 0;

    for (JsonNode refeicaoNode : root.path("refeicoes")) {
      Refeicao refeicao = new Refeicao();
      refeicao.setPlano(plano);
      refeicao.setIdentificador(refeicaoNode.path("id").asText());
      refeicao.setNome(refeicaoNode.path("nome").asText());
      refeicao.setOrdem(refeicaoNode.path("ordem").asInt());
      refeicao.setHorarioSugerido(time(refeicaoNode, "horario_sugerido"));
      refeicao = refeicaoRepository.save(refeicao);
      refeicoes++;

      for (JsonNode categoriaNode : refeicaoNode.path("categorias")) {
        CategoriaRefeicao categoria = new CategoriaRefeicao();
        categoria.setRefeicao(refeicao);
        categoria.setNome(categoriaNode.path("nome").asText());
        categoria.setTipoSelecao(TipoSelecao.valueOf(categoriaNode.path("tipo_selecao").asText()));
        categoria.setObrigatorio(categoriaNode.path("obrigatorio").asBoolean(true));
        categoria = categoriaRepository.save(categoria);
        categorias++;

        for (JsonNode opcaoNode : categoriaNode.path("opcoes")) {
          OpcaoAlimento opcao = new OpcaoAlimento();
          opcao.setCategoria(categoria);
          opcao.setAlimento(opcaoNode.path("alimento").asText());
          opcao.setPorcao(text(opcaoNode, "porcao"));
          if (opcaoNode.hasNonNull("peso_valor")) {
            opcao.setPesoValor(new BigDecimal(opcaoNode.path("peso_valor").asText()));
          }
          opcao.setUnidade(text(opcaoNode, "unidade"));
          opcaoRepository.save(opcao);
          opcoes++;
        }
      }
    }

    return new ImportacaoPlanoResponse(PlanoResponse.from(plano), refeicoes, categorias, opcoes);
  }

  private void desativarPlanosAtivos(Long usuarioId) {
    planoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId).stream()
        .filter(PlanoNutricional::isAtivo)
        .forEach(
            plano -> {
              plano.setAtivo(false);
              planoRepository.save(plano);
            });
  }

  private JsonNode parse(MultipartFile arquivo) {
    try {
      return objectMapper.readTree(arquivo.getInputStream());
    } catch (IOException exception) {
      throw new BusinessException("JSON invalido ou ilegivel");
    }
  }

  private void validar(JsonNode root) {
    List<ApiError.FieldError> errors = new ArrayList<>();
    JsonNode configuracoes = root.path("configuracoes");
    JsonNode metaAgua = configuracoes.path("meta_agua_diaria_ml");
    if (!metaAgua.isInt() || metaAgua.asInt() <= 0) {
      errors.add(new ApiError.FieldError("configuracoes.meta_agua_diaria_ml", "Deve ser inteiro positivo"));
    }

    JsonNode refeicoes = root.path("refeicoes");
    if (!refeicoes.isArray() || refeicoes.isEmpty()) {
      errors.add(new ApiError.FieldError("refeicoes", "Deve conter pelo menos uma refeicao"));
    } else {
      for (int i = 0; i < refeicoes.size(); i++) {
        validarRefeicao(refeicoes.get(i), i, errors);
      }
    }

    if (!errors.isEmpty()) {
      throw new ValidationException("JSON invalido", errors);
    }
  }

  private void validarRefeicao(JsonNode refeicao, int index, List<ApiError.FieldError> errors) {
    requireText(refeicao, "refeicoes[" + index + "].id", "id", errors);
    requireText(refeicao, "refeicoes[" + index + "].nome", "nome", errors);
    if (!refeicao.path("ordem").isInt()) {
      errors.add(new ApiError.FieldError("refeicoes[" + index + "].ordem", "Campo obrigatorio inteiro"));
    }

    JsonNode categorias = refeicao.path("categorias");
    if (!categorias.isArray() || categorias.isEmpty()) {
      errors.add(
          new ApiError.FieldError(
              "refeicoes[" + index + "].categorias", "Deve conter pelo menos uma categoria"));
      return;
    }

    for (int c = 0; c < categorias.size(); c++) {
      JsonNode categoria = categorias.get(c);
      String base = "refeicoes[" + index + "].categorias[" + c + "]";
      requireText(categoria, base + ".nome", "nome", errors);
      String tipoSelecao = categoria.path("tipo_selecao").asText("");
      if (tipoSelecao.isBlank()
          || EnumSet.allOf(TipoSelecao.class).stream().noneMatch(tipo -> tipo.name().equals(tipoSelecao))) {
        errors.add(new ApiError.FieldError(base + ".tipo_selecao", "Valor nao permitido"));
      }
      JsonNode opcoes = categoria.path("opcoes");
      if (!opcoes.isArray() || opcoes.isEmpty()) {
        errors.add(new ApiError.FieldError(base + ".opcoes", "A categoria deve possuir pelo menos uma opcao"));
      }
    }
  }

  private void requireText(JsonNode node, String path, String field, List<ApiError.FieldError> errors) {
    if (!node.hasNonNull(field) || node.path(field).asText().isBlank()) {
      errors.add(new ApiError.FieldError(path, "Campo obrigatorio"));
    }
  }

  private String text(JsonNode node, String field) {
    return node.hasNonNull(field) ? node.path(field).asText() : null;
  }

  private LocalDate date(JsonNode node, String field) {
    return node.hasNonNull(field) ? LocalDate.parse(node.path(field).asText()) : null;
  }

  private LocalTime time(JsonNode node, String field) {
    return node.hasNonNull(field) ? LocalTime.parse(node.path(field).asText()) : null;
  }
}
