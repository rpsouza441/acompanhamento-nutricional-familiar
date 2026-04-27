package com.nutritracker.config;

import com.nutritracker.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UsuarioRepository usuarioRepository;

  public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
    this.jwtService = jwtService;
    this.usuarioRepository = usuarioRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authorization = request.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String token = authorization.substring("Bearer ".length());
      String email = jwtService.getSubject(token);
      usuarioRepository
          .findByEmailIgnoreCase(email)
          .filter(usuario -> usuario.isAtivo())
          .ifPresent(
              usuario -> {
                var authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name());
                var authentication =
                    new UsernamePasswordAuthenticationToken(usuario, null, java.util.List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(authentication);
              });
    } catch (RuntimeException ignored) {
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
