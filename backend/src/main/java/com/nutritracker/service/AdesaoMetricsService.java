package com.nutritracker.service;

import com.nutritracker.model.RegistroDiario;
import com.nutritracker.repository.RefeicaoRegistradaRepository;
import com.nutritracker.repository.RegistroDiarioRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdesaoMetricsService {
  private final RegistroDiarioRepository registroRepository;
  private final RefeicaoRegistradaRepository refeicaoRegistradaRepository;

  public AdesaoMetricsService(
      RegistroDiarioRepository registroRepository,
      RefeicaoRegistradaRepository refeicaoRegistradaRepository) {
    this.registroRepository = registroRepository;
    this.refeicaoRegistradaRepository = refeicaoRegistradaRepository;
  }

  public List<RegistroDiario> registrosDoUsuario(Long usuarioId) {
    return registroRepository.findByUsuarioIdOrderByDataRegistroAsc(usuarioId);
  }

  public List<RegistroDiario> registrosNoPeriodo(Long usuarioId, LocalDate inicio, LocalDate fim) {
    return registroRepository.findByUsuarioIdAndDataRegistroBetweenOrderByDataRegistroAsc(
        usuarioId, inicio, fim);
  }

  public double adesaoPercentual(RegistroDiario registro) {
    var refeicoes = refeicaoRegistradaRepository.findByRegistroDiarioId(registro.getId());
    if (refeicoes.isEmpty()) {
      return 0.0;
    }
    long concluidas = refeicoes.stream().filter(refeicao -> refeicao.isConcluida()).count();
    return (concluidas * 100.0) / refeicoes.size();
  }

  public boolean temAltaAdesao(RegistroDiario registro) {
    return adesaoPercentual(registro) >= 80.0;
  }

  public int diasComAltaAdesao(List<RegistroDiario> registros) {
    return (int) registros.stream().filter(this::temAltaAdesao).count();
  }

  public int diasMetaAgua(List<RegistroDiario> registros) {
    return (int)
        registros.stream()
            .filter(
                registro ->
                    registro.getAguaConsumidaMl() != null
                        && registro.getPlano() != null
                        && registro.getPlano().getMetaAguaDiariaMl() != null
                        && registro.getAguaConsumidaMl() >= registro.getPlano().getMetaAguaDiariaMl())
            .count();
  }

  public int maiorSequenciaAltaAdesao(List<RegistroDiario> registros) {
    int maior = 0;
    int atual = 0;
    LocalDate dataAnterior = null;

    for (RegistroDiario registro :
        registros.stream().sorted(Comparator.comparing(RegistroDiario::getDataRegistro)).toList()) {
      if (!temAltaAdesao(registro)) {
        atual = 0;
        dataAnterior = registro.getDataRegistro();
        continue;
      }

      if (dataAnterior != null && registro.getDataRegistro().equals(dataAnterior.plusDays(1))) {
        atual++;
      } else {
        atual = 1;
      }
      maior = Math.max(maior, atual);
      dataAnterior = registro.getDataRegistro();
    }
    return maior;
  }
}
