package com.nutritracker.service;

import com.nutritracker.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {
  private final UsuarioRepository usuarioRepository;

  public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return usuarioRepository
        .findByEmailIgnoreCase(username)
        .filter(usuario -> usuario.isAtivo())
        .map(
            usuario ->
                new User(
                    usuario.getEmail(),
                    usuario.getSenhaHash(),
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()))))
        .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
  }
}
