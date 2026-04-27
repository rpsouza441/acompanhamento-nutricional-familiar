package com.nutritracker.service;

import com.nutritracker.dto.UsuarioRequest;
import com.nutritracker.exception.BusinessException;
import com.nutritracker.model.Role;
import com.nutritracker.model.Usuario;
import com.nutritracker.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;

  public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<Usuario> listar() {
    return usuarioRepository.findAll();
  }

  @Transactional
  public Usuario criar(UsuarioRequest request) {
    if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
      throw new BusinessException("E-mail ja cadastrado");
    }
    if (request.senha() == null || request.senha().isBlank()) {
      throw new BusinessException("Senha e obrigatoria para novo usuario");
    }
    Usuario usuario = new Usuario();
    aplicar(usuario, request);
    usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
    return usuarioRepository.save(usuario);
  }

  @Transactional
  public Usuario atualizar(Long id, UsuarioRequest request) {
    Usuario usuario = buscar(id);
    aplicar(usuario, request);
    if (request.senha() != null && !request.senha().isBlank()) {
      usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
    }
    return usuarioRepository.save(usuario);
  }

  @Transactional
  public Usuario alterarAtivo(Long id, boolean ativo) {
    Usuario usuario = buscar(id);
    usuario.setAtivo(ativo);
    return usuarioRepository.save(usuario);
  }

  public Usuario buscar(Long id) {
    return usuarioRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException("Usuario nao encontrado"));
  }

  private void aplicar(Usuario usuario, UsuarioRequest request) {
    usuario.setNome(request.nome());
    usuario.setEmail(request.email());
    usuario.setRole(request.role() == null ? Role.USER : request.role());
    usuario.setAtivo(request.ativo() == null || request.ativo());
  }
}
