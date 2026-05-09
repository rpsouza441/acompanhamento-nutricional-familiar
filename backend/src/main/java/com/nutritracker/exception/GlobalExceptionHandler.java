package com.nutritracker.exception;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiError> business(BusinessException exception) {
    return build(HttpStatus.BAD_REQUEST, exception.getMessage(), java.util.List.of());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> credentials() {
    return build(HttpStatus.UNAUTHORIZED, "Credenciais invalidas", java.util.List.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
    var errors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> new ApiError.FieldError(error.getField(), error.getDefaultMessage()))
            .toList();
    return build(HttpStatus.BAD_REQUEST, "Dados invalidos", errors);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ApiError> detailedValidation(ValidationException exception) {
    return build(HttpStatus.BAD_REQUEST, exception.getMessage(), exception.getErrors());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> accessDenied() {
    return build(HttpStatus.FORBIDDEN, "Acesso negado", java.util.List.of());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> generic(Exception exception) {
    LOGGER.error("Erro interno inesperado", exception);
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Erro interno inesperado. Tente novamente mais tarde.",
        java.util.List.of());
  }

  private ResponseEntity<ApiError> build(
      HttpStatus status, String message, java.util.List<ApiError.FieldError> errors) {
    return ResponseEntity.status(status)
        .body(new ApiError(Instant.now(), status.value(), message, errors));
  }
}
