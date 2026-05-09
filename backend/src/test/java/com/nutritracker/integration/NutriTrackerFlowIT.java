package com.nutritracker.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class NutriTrackerFlowIT {
  private static final String ADMIN_EMAIL = "admin@nutritracker.local";
  private static final String PASSWORD = "password";

  @Container
  static final MariaDBContainer<?> MARIADB =
      new MariaDBContainer<>("mariadb:10.11")
          .withDatabaseName("nutritracker")
          .withUsername("nutritracker")
          .withPassword("nutritracker_pass_2024")
          .withCopyFileToContainer(
              MountableFile.forHostPath(Path.of("..", "database", "01-schema.sql").toAbsolutePath()),
              "/docker-entrypoint-initdb.d/01-schema.sql")
          .withCopyFileToContainer(
              MountableFile.forHostPath(Path.of("..", "database", "02-sample-data.sql").toAbsolutePath()),
              "/docker-entrypoint-initdb.d/02-sample-data.sql");

  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MARIADB::getJdbcUrl);
    registry.add("spring.datasource.username", MARIADB::getUsername);
    registry.add("spring.datasource.password", MARIADB::getPassword);
    registry.add("spring.datasource.driver-class-name", MARIADB::getDriverClassName);
    registry.add("nutritracker.jwt.secret", () -> "integration-test-secret-with-at-least-256-bits");
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void fluxoPrincipalComBancoRealEPermissoes() throws Exception {
    JsonNode adminLogin = login(ADMIN_EMAIL, PASSWORD);
    String adminToken = adminLogin.path("accessToken").asText();
    Long adminId = adminLogin.path("usuario").path("id").asLong();

    JsonNode novoUsuario =
        postJson(
            "/api/usuarios",
            adminToken,
            """
            {
              "nome": "Paciente Teste",
              "email": "paciente.integration@nutritracker.local",
              "senha": "password",
              "role": "USER",
              "ativo": true
            }
            """);
    Long usuarioId = novoUsuario.path("id").asLong();

    String userToken = login("paciente.integration@nutritracker.local", PASSWORD).path("accessToken").asText();

    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "plano.json",
            MediaType.APPLICATION_JSON_VALUE,
            planoJson().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    mockMvc
        .perform(multipart("/api/planos/importar").file(file).header("Authorization", bearer(userToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.plano.usuarioId").value(usuarioId))
        .andExpect(jsonPath("$.refeicoes").value(1));

    JsonNode registro =
        getJson("/api/registros?data=2026-05-09", userToken)
            .andExpect(jsonPath("$.usuarioId").value(usuarioId))
            .andExpect(jsonPath("$.refeicoes[0].nome").value("Desjejum"))
            .andReturnJson();
    Long registroId = registro.path("id").asLong();
    Long refeicaoId = registro.path("refeicoes").get(0).path("refeicaoId").asLong();

    mockMvc
        .perform(
            put("/api/registros/{id}", registroId)
                .header("Authorization", bearer(userToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"aguaConsumidaMl\":3000,\"observacoesGerais\":\"Tudo certo\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.aguaConsumidaMl").value(3000));

    mockMvc
        .perform(
            post("/api/registros/{id}/refeicoes/{refeicaoId}/concluir", registroId, refeicaoId)
                .header("Authorization", bearer(userToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"concluida\":true,\"horarioRealizado\":\"07:00\",\"observacoes\":\"ok\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.refeicoes[0].concluida").value(true));

    mockMvc
        .perform(
            get("/api/relatorios?inicio=2026-05-09&fim=2026-05-09")
                .header("Authorization", bearer(userToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.usuarioId").value(usuarioId))
        .andExpect(jsonPath("$.diasRegistrados").value(1));

    mockMvc
        .perform(
            get("/api/relatorios/pdf?inicio=2026-05-09&fim=2026-05-09")
                .header("Authorization", bearer(userToken)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF));

    mockMvc
        .perform(
            get("/api/relatorios?usuarioId={adminId}&inicio=2026-05-09&fim=2026-05-09", adminId)
                .header("Authorization", bearer(userToken)))
        .andExpect(status().isForbidden());
  }

  private JsonNode login(String email, String senha) throws Exception {
    return postJson(
        "/api/auth/login",
        null,
        """
        {"email":"%s","senha":"%s"}
        """
            .formatted(email, senha));
  }

  private JsonNode postJson(String path, String token, String body) throws Exception {
    var request = post(path).contentType(MediaType.APPLICATION_JSON).content(body);
    if (token != null) {
      request.header("Authorization", bearer(token));
    }
    String response =
        mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(response);
  }

  private ResultJson getJson(String path, String token) throws Exception {
    var result =
        mockMvc
            .perform(get(path).header("Authorization", bearer(token)))
            .andExpect(status().isOk());
    return new ResultJson(result.andReturn().getResponse().getContentAsString(), result);
  }

  private String bearer(String token) {
    return "Bearer " + token;
  }

  private String planoJson() {
    return """
        {
          "configuracoes": {
            "meta_agua_diaria_ml": 3000,
            "objetivo": "Integracao",
            "profissional": "Nutricionista",
            "data_prescricao": "2026-05-09"
          },
          "refeicoes": [
            {
              "id": "desjejum",
              "nome": "Desjejum",
              "horario_sugerido": "07:00",
              "ordem": 1,
              "categorias": [
                {
                  "nome": "Proteina",
                  "tipo_selecao": "escolha_uma",
                  "obrigatorio": true,
                  "opcoes": [
                    {
                      "alimento": "Iogurte natural",
                      "porcao": "1 pote",
                      "peso_valor": 170,
                      "unidade": "g"
                    }
                  ]
                }
              ]
            }
          ]
        }
        """;
  }

  private class ResultJson {
    private final String body;
    private final org.springframework.test.web.servlet.ResultActions actions;

    private ResultJson(String body, org.springframework.test.web.servlet.ResultActions actions) {
      this.body = body;
      this.actions = actions;
    }

    private ResultJson andExpect(org.springframework.test.web.servlet.ResultMatcher matcher)
        throws Exception {
      actions.andExpect(matcher);
      return this;
    }

    private JsonNode andReturnJson() throws Exception {
      return objectMapper.readTree(body);
    }
  }
}
