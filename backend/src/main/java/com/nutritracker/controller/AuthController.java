package com.nutritracker.controller;

import com.nutritracker.dto.LoginRequest;
import com.nutritracker.dto.LoginResponse;
import com.nutritracker.dto.RefreshRequest;
import com.nutritracker.dto.RefreshResponse;
import com.nutritracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/refresh")
  public RefreshResponse refresh(@Valid @RequestBody RefreshRequest request) {
    return authService.refresh(request.refreshToken());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }
}
