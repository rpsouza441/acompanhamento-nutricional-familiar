package com.nutritracker.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void erroGenericoNaoExpoeMensagemInterna() {
    var response = handler.generic(new RuntimeException("SQL syntax near usuarios.senha_hash"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().message())
        .isEqualTo("Erro interno inesperado. Tente novamente mais tarde.");
    assertThat(response.getBody().message()).doesNotContain("senha_hash");
  }

  @Test
  void businessExceptionPreservaMensagemAmigavel() {
    var response = handler.business(new BusinessException("Usuario nao encontrado"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().message()).isEqualTo("Usuario nao encontrado");
  }
}
