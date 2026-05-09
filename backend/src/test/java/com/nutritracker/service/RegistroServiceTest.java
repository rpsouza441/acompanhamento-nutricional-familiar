package com.nutritracker.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.nutritracker.dto.RegistroUpdateRequest;
import com.nutritracker.model.PlanoNutricional;
import com.nutritracker.model.RegistroDiario;
import com.nutritracker.model.Role;
import com.nutritracker.model.Usuario;
import com.nutritracker.repository.AlimentoConsumidoRepository;
import com.nutritracker.repository.CategoriaRefeicaoRepository;
import com.nutritracker.repository.OpcaoAlimentoRepository;
import com.nutritracker.repository.PlanoNutricionalRepository;
import com.nutritracker.repository.RefeicaoRegistradaRepository;
import com.nutritracker.repository.RefeicaoRepository;
import com.nutritracker.repository.RegistroDiarioRepository;
import com.nutritracker.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class RegistroServiceTest {
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private PlanoNutricionalRepository planoRepository;
  @Mock private RefeicaoRepository refeicaoRepository;
  @Mock private RegistroDiarioRepository registroRepository;
  @Mock private RefeicaoRegistradaRepository refeicaoRegistradaRepository;
  @Mock private CategoriaRefeicaoRepository categoriaRepository;
  @Mock private OpcaoAlimentoRepository opcaoRepository;
  @Mock private AlimentoConsumidoRepository alimentoRepository;
  @Mock private ConquistaService conquistaService;

  private RegistroService service;

  @BeforeEach
  void setUp() {
    service =
        new RegistroService(
            usuarioRepository,
            planoRepository,
            refeicaoRepository,
            registroRepository,
            refeicaoRegistradaRepository,
            categoriaRepository,
            opcaoRepository,
            alimentoRepository,
            conquistaService,
            new AuthorizationService());
  }

  @Test
  void atualizarNaoPermiteUsuarioAlterarRegistroDeOutroUsuario() {
    when(registroRepository.findById(10L)).thenReturn(Optional.of(registroDoUsuario(2L)));

    assertThatThrownBy(
            () -> service.atualizar(usuario(1L, Role.USER), 10L, new RegistroUpdateRequest(2000, null)))
        .isInstanceOf(AccessDeniedException.class);
  }

  private RegistroDiario registroDoUsuario(Long usuarioId) {
    Usuario usuario = usuario(usuarioId, Role.USER);
    PlanoNutricional plano = new PlanoNutricional();
    plano.setId(20L);
    plano.setUsuario(usuario);
    plano.setJsonOriginal("{}");

    RegistroDiario registro = new RegistroDiario();
    registro.setId(10L);
    registro.setUsuario(usuario);
    registro.setPlano(plano);
    return registro;
  }

  private Usuario usuario(Long id, Role role) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setRole(role);
    return usuario;
  }
}
