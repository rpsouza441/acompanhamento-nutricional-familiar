package com.nutritracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutritracker.dto.PlanoManualCategoriaRequest;
import com.nutritracker.dto.PlanoManualOpcaoRequest;
import com.nutritracker.dto.PlanoManualRequest;
import com.nutritracker.dto.PlanoManualResponse;
import com.nutritracker.dto.PlanoManualRefeicaoRequest;
import com.nutritracker.dto.PlanoResponse;
import com.nutritracker.exception.BusinessException;
import com.nutritracker.model.CategoriaRefeicao;
import com.nutritracker.model.OpcaoAlimento;
import com.nutritracker.model.PlanoNutricional;
import com.nutritracker.model.Refeicao;
import com.nutritracker.repository.CategoriaRefeicaoRepository;
import com.nutritracker.repository.OpcaoAlimentoRepository;
import com.nutritracker.repository.PlanoNutricionalRepository;
import com.nutritracker.repository.RefeicaoRepository;
import com.nutritracker.repository.UsuarioRepository;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanoManualService {
  private final ObjectMapper objectMapper;
  private final UsuarioRepository usuarioRepository;
  private final PlanoNutricionalRepository planoRepository;
  private final RefeicaoRepository refeicaoRepository;
  private final CategoriaRefeicaoRepository categoriaRepository;
  private final OpcaoAlimentoRepository opcaoRepository;

  public PlanoManualService(
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
  public PlanoManualResponse criar(PlanoManualRequest request) {
    var usuario =
        usuarioRepository
            .findById(request.usuarioId())
            .orElseThrow(() -> new BusinessException("Usuario nao encontrado"));

    PlanoNutricional plano = new PlanoNutricional();
    plano.setUsuario(usuario);
    aplicarDados(plano, request);
    plano = planoRepository.save(plano);
    Contagem contagem = salvarEstrutura(plano, request);
    return new PlanoManualResponse(
        PlanoResponse.from(plano), contagem.refeicoes, contagem.categorias, contagem.opcoes);
  }

  @Transactional
  public PlanoManualResponse atualizar(Long id, PlanoManualRequest request) {
    PlanoNutricional plano =
        planoRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException("Plano nao encontrado"));
    if (!plano.getUsuario().getId().equals(request.usuarioId())) {
      var usuario =
          usuarioRepository
              .findById(request.usuarioId())
              .orElseThrow(() -> new BusinessException("Usuario nao encontrado"));
      plano.setUsuario(usuario);
    }
    aplicarDados(plano, request);
    plano = planoRepository.save(plano);
    removerEstrutura(plano.getId());
    Contagem contagem = salvarEstrutura(plano, request);
    return new PlanoManualResponse(
        PlanoResponse.from(plano), contagem.refeicoes, contagem.categorias, contagem.opcoes);
  }

  private void aplicarDados(PlanoNutricional plano, PlanoManualRequest request) {
    plano.setProfissional(request.profissional());
    plano.setObjetivo(request.objetivo());
    plano.setMetaAguaDiariaMl(request.metaAguaDiariaMl());
    plano.setDataPrescricao(request.dataPrescricao());
    plano.setAtivo(request.ativo() == null || request.ativo());
    plano.setJsonOriginal(toJson(request));
  }

  private Contagem salvarEstrutura(PlanoNutricional plano, PlanoManualRequest request) {
    Contagem contagem = new Contagem();
    var refeicoes =
        request.refeicoes().stream()
            .sorted(Comparator.comparing(PlanoManualRefeicaoRequest::ordem))
            .toList();

    for (PlanoManualRefeicaoRequest refeicaoRequest : refeicoes) {
      Refeicao refeicao = new Refeicao();
      refeicao.setPlano(plano);
      refeicao.setIdentificador(refeicaoRequest.identificador());
      refeicao.setNome(refeicaoRequest.nome());
      refeicao.setHorarioSugerido(refeicaoRequest.horarioSugerido());
      refeicao.setOrdem(refeicaoRequest.ordem());
      refeicao = refeicaoRepository.save(refeicao);
      contagem.refeicoes++;

      for (PlanoManualCategoriaRequest categoriaRequest : refeicaoRequest.categorias()) {
        CategoriaRefeicao categoria = new CategoriaRefeicao();
        categoria.setRefeicao(refeicao);
        categoria.setNome(categoriaRequest.nome());
        categoria.setTipoSelecao(categoriaRequest.tipoSelecao());
        categoria.setObrigatorio(categoriaRequest.obrigatorio() == null || categoriaRequest.obrigatorio());
        categoria = categoriaRepository.save(categoria);
        contagem.categorias++;

        for (PlanoManualOpcaoRequest opcaoRequest : categoriaRequest.opcoes()) {
          OpcaoAlimento opcao = new OpcaoAlimento();
          opcao.setCategoria(categoria);
          opcao.setAlimento(opcaoRequest.alimento());
          opcao.setPorcao(opcaoRequest.porcao());
          opcao.setPesoValor(opcaoRequest.pesoValor());
          opcao.setUnidade(opcaoRequest.unidade());
          opcaoRepository.save(opcao);
          contagem.opcoes++;
        }
      }
    }
    return contagem;
  }

  private void removerEstrutura(Long planoId) {
    for (Refeicao refeicao : refeicaoRepository.findByPlanoIdOrderByOrdemAsc(planoId)) {
      for (CategoriaRefeicao categoria : categoriaRepository.findByRefeicaoId(refeicao.getId())) {
        for (OpcaoAlimento opcao : opcaoRepository.findByCategoriaId(categoria.getId())) {
          opcaoRepository.delete(opcao);
        }
        categoriaRepository.delete(categoria);
      }
      refeicaoRepository.delete(refeicao);
    }
  }

  private String toJson(PlanoManualRequest request) {
    try {
      return objectMapper.writeValueAsString(request);
    } catch (JsonProcessingException exception) {
      throw new BusinessException("Nao foi possivel serializar o plano manual");
    }
  }

  private static class Contagem {
    private int refeicoes;
    private int categorias;
    private int opcoes;
  }
}
