package com.nutritracker.service;

import com.nutritracker.repository.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ConquistaService {
  private final UsuarioRepository usuarioRepository;

  public ConquistaService(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void calcularTodas() {
    usuarioRepository.findAll().stream()
        .filter(usuario -> usuario.isAtivo())
        .forEach(usuario -> calcular(usuario.getId()));
  }

  public void calcular(Long usuarioId) {
    // A regra completa entra na proxima fatia: adesao >=80%, agua diaria e sequencias.
  }
}
