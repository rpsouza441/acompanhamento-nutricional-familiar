package com.nutritracker.exception;

import java.util.List;

public class ValidationException extends RuntimeException {
  private final List<ApiError.FieldError> errors;

  public ValidationException(String message, List<ApiError.FieldError> errors) {
    super(message);
    this.errors = errors;
  }

  public List<ApiError.FieldError> getErrors() {
    return errors;
  }
}
