package com.nutritracker.service;

import com.nutritracker.dto.AlimentoConsumidoRequest;
import com.nutritracker.dto.AlimentoConsumidoResponse;
import com.nutritracker.dto.ConcluirRefeicaoRequest;
import com.nutritracker.dto.RegistroResponse;
import com.nutritracker.dto.RegistroUpdateRequest;
import com.nutritracker.dto.RefeicaoRegistradaResponse;
import com.nutritracker.exception.BusinessException;
import com.nutritracker.model.AlimentoConsumido;
import com.nutritracker.model.RefeicaoRegistrada;
import com.nutritracker.model.RegistroDiario;
import com.nutritracker.repository.AlimentoConsumidoRepository;
import com.nutritracker.repository.OpcaoAlimentoRepository;
import com.nutritracker.repository.PlanoNutricionalRepository;
import com.nutritracker.repository.RefeicaoRegistradaRepository;
import com.nutritracker.repository.RefeicaoRepository;
import com.nutritracker.repository.RegistroDiarioRepository;
import com.nutritracker.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroService {
  private final UsuarioRepository usuarioRepository;
  private final PlanoNutricionalRepository planoRepository;
  private final RefeicaoRepository refeicaoRepository;
  private final RegistroDiarioRepository registroRepository;
  private final RefeicaoRegistradaRepository refeicaoRegistradaRepository;
  private final OpcaoAlimentoRepository opcaoRepository;
  private final AlimentoConsumidoRepository alimentoRepository;
  private final ConquistaService conquistaService;

  public RegistroService(
      UsuarioRepository usuarioRepository,
      PlanoNutricionalRepository planoRepository,
      RefeicaoRepository refeicaoRepository,
      RegistroDiarioRepository registroRepository,
      RefeicaoRegistradaRepository refeicaoRegistradaRepository,
      OpcaoAlimentoRepository opcaoRepository,
      AlimentoConsumidoRepository alimentoRepository,
      ConquistaService conquistaService) {
    this.usuarioRepository = usuarioRepository;
    this.planoRepository = planoRepository;
    this.refeicaoRepository = refeicaoRepository;
    this.registroRepository = registroRepository;
    this.refeicaoRegistradaRepository = refeicaoRegistradaRepository;
    this.opcaoRepository = opcaoRepository;
    this.alimentoRepository = alimentoRepository;
    this.conquistaService = conquistaService;
  }

  @Transactional
  public RegistroResponse buscarOuCriar(Long usuarioId, LocalDate data) {
    RegistroDiario registro =
        registroRepository
            .findByUsuarioIdAndDataRegistro(usuarioId, data)
            .orElseGet(() -> criarRegistro(usuarioId, data));
    return toResponse(registro);
  }

  @Transactional
  public RegistroResponse atualizar(Long id, RegistroUpdateRequest request) {
    RegistroDiario registro = buscarRegistro(id);
    if (request.aguaConsumidaMl() != null) {
      registro.setAguaConsumidaMl(request.aguaConsumidaMl());
    }
    registro.setObservacoesGerais(request.observacoesGerais());
    registro = registroRepository.save(registro);
    conquistaService.calcular(registro.getUsuario().getId());
    return toResponse(registro);
  }

  @Transactional
  public RegistroResponse concluirRefeicao(
      Long registroId, Long refeicaoId, ConcluirRefeicaoRequest request) {
    RegistroDiario registro = buscarRegistro(registroId);
    RefeicaoRegistrada refeicao =
        refeicaoRegistradaRepository
            .findByRegistroDiarioIdAndRefeicaoId(registroId, refeicaoId)
            .orElseThrow(() -> new BusinessException("Refeicao do registro nao encontrada"));
    ConcluirRefeicaoRequest payload =
        request == null ? new ConcluirRefeicaoRequest(null, null) : request;
    refeicao.setConcluida(true);
    refeicao.setHorarioRealizado(
        payload.horarioRealizado() == null ? LocalTime.now() : payload.horarioRealizado());
    refeicao.setObservacoes(payload.observacoes());
    refeicaoRegistradaRepository.save(refeicao);
    conquistaService.calcular(registro.getUsuario().getId());
    return toResponse(registro);
  }

  @Transactional
  public RegistroResponse adicionarAlimento(
      Long registroId, Long refeicaoId, AlimentoConsumidoRequest request) {
    RegistroDiario registro = buscarRegistro(registroId);
    RefeicaoRegistrada refeicao =
        refeicaoRegistradaRepository
            .findByRegistroDiarioIdAndRefeicaoId(registroId, refeicaoId)
            .orElseThrow(() -> new BusinessException("Refeicao do registro nao encontrada"));
    if (request.opcaoId() == null
        && (request.descricaoManual() == null || request.descricaoManual().isBlank())) {
      throw new BusinessException("Informe uma opcao do plano ou uma descricao manual");
    }

    AlimentoConsumido alimento = new AlimentoConsumido();
    alimento.setRefeicaoRegistrada(refeicao);
    if (request.opcaoId() != null) {
      alimento.setOpcao(
          opcaoRepository
              .findById(request.opcaoId())
              .orElseThrow(() -> new BusinessException("Opcao de alimento nao encontrada")));
    }
    alimento.setDescricaoManual(request.descricaoManual());
    alimento.setQuantidadePersonalizada(request.quantidadePersonalizada());
    alimentoRepository.save(alimento);
    return toResponse(registro);
  }

  @Transactional
  public RegistroResponse removerAlimento(Long registroId, Long alimentoId) {
    RegistroDiario registro = buscarRegistro(registroId);
    if (!alimentoRepository.existsById(alimentoId)) {
      throw new BusinessException("Alimento consumido nao encontrado");
    }
    alimentoRepository.deleteById(alimentoId);
    return toResponse(registro);
  }

  private RegistroDiario criarRegistro(Long usuarioId, LocalDate data) {
    var usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new BusinessException("Usuario nao encontrado"));
    var plano =
        planoRepository
            .findFirstByUsuarioIdAndAtivoTrueOrderByCriadoEmDesc(usuarioId)
            .orElseThrow(() -> new BusinessException("Usuario nao possui plano ativo"));

    RegistroDiario registro = new RegistroDiario();
    registro.setUsuario(usuario);
    registro.setPlano(plano);
    registro.setDataRegistro(data);
    registro = registroRepository.save(registro);

    for (var refeicao : refeicaoRepository.findByPlanoIdOrderByOrdemAsc(plano.getId())) {
      RefeicaoRegistrada registrada = new RefeicaoRegistrada();
      registrada.setRegistroDiario(registro);
      registrada.setRefeicao(refeicao);
      refeicaoRegistradaRepository.save(registrada);
    }
    return registro;
  }

  private RegistroDiario buscarRegistro(Long id) {
    return registroRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException("Registro diario nao encontrado"));
  }

  private RegistroResponse toResponse(RegistroDiario registro) {
    var refeicoes =
        refeicaoRegistradaRepository.findByRegistroDiarioId(registro.getId()).stream()
            .map(
                refeicao -> {
                  var alimentos =
                      alimentoRepository.findByRefeicaoRegistradaId(refeicao.getId()).stream()
                          .map(AlimentoConsumidoResponse::from)
                          .toList();
                  return RefeicaoRegistradaResponse.from(refeicao, alimentos);
                })
            .toList();
    return RegistroResponse.from(registro, refeicoes);
  }
}
