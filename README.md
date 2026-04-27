# NutriTracker

Sistema de acompanhamento nutricional familiar para registrar consumo alimentar diario, consumo de agua, adesao ao plano e conquistas. O projeto usa prescricoes nutricionais importadas por JSON e tambem preve cadastro manual de planos.

## Stack

| Camada | Tecnologia |
| --- | --- |
| Backend | Java 21, Spring Boot 3.3.x, REST API |
| Frontend | React 18, Vite, TailwindCSS |
| Banco de dados | MariaDB 10.11 |
| Containerizacao | Docker e Docker Compose v2 |
| Autenticacao | JWT com access token e refresh token |
| PDF | iText 7 e JFreeChart |
| Documentacao API | SpringDoc OpenAPI, Swagger UI |

## Estrutura Atual

```text
.
├── backend/
│   ├── src/main/java/com/nutritracker/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   ├── src/main/resources/application.yml
│   ├── src/test/java/com/nutritracker/
│   ├── pom.xml
│   └── Dockerfile
├── database/
│   ├── 01-schema.sql
│   └── 02-sample-data.sql
└── README.md
```

## Backend Implementado

Endpoints iniciais:

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/usuarios`
- `POST /api/usuarios`
- `PUT /api/usuarios/{id}`
- `PATCH /api/usuarios/{id}/ativo`
- `POST /api/planos/importar`
- `GET /api/planos?usuarioId=`
- `GET /api/planos/{id}`
- `PATCH /api/planos/{id}/ativar`
- `GET /api/registros?usuarioId=&data=`
- `PUT /api/registros/{id}`
- `POST /api/registros/{id}/refeicoes/{refeicaoId}/concluir`
- `POST /api/registros/{id}/refeicoes/{refeicaoId}/alimentos`
- `DELETE /api/registros/{registroId}/alimentos/{alimentoId}`

## Requisitos Locais

- Java 21
- Maven 3.9+
- Docker 24+
- Docker Compose v2

## Configuracao

Crie um arquivo `.env` na raiz quando o Docker Compose estiver disponivel:

```env
DB_ROOT_PASSWORD=nutritracker_root_2024
DB_USER=nutritracker
DB_PASSWORD=nutritracker_pass_2024
JWT_SECRET=change-this-secret-to-a-strong-256-bit-value
```

O backend tambem aceita variaveis de ambiente Spring:

```env
SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/nutritracker
SPRING_DATASOURCE_USERNAME=nutritracker
SPRING_DATASOURCE_PASSWORD=nutritracker_pass_2024
JWT_SECRET=change-this-secret-to-a-strong-256-bit-value
```

## Rodando Testes

Com Maven instalado:

```bash
cd backend
mvn test
```

## Rodando o Backend

Com MariaDB disponivel e o schema aplicado:

```bash
cd backend
mvn spring-boot:run
```

A API sobe em:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Quando o Nginx do Docker Compose for adicionado, o acesso esperado sera:

```text
http://localhost/api
http://localhost/api/swagger-ui.html
```

## Credencial Inicial

O seed em `database/02-sample-data.sql` cria:

```text
email: admin@nutritracker.local
senha: password
```

## Importacao de Plano

Endpoint:

```text
POST /api/planos/importar?usuarioId=1
Content-Type: multipart/form-data
Campo do arquivo: file
```

Validacoes atuais:

- `configuracoes.meta_agua_diaria_ml` deve ser inteiro positivo.
- `refeicoes` deve conter pelo menos uma refeicao.
- Cada refeicao deve conter `id`, `nome` e `ordem`.
- Cada refeicao deve conter pelo menos uma categoria.
- `tipo_selecao` deve ser `escolha_uma`, `escolha_multipla` ou `livre`.
- Cada categoria deve conter pelo menos uma opcao.

## Status

Concluido nesta etapa:

- Estrutura inicial do backend.
- Autenticacao JWT.
- CRUD administrativo inicial de usuarios com soft-delete.
- Importacao de planos por JSON.
- Registro diario basico.
- Schema MariaDB inicial.
- Testes unitarios iniciais.

Pendencias principais:

- Docker Compose completo.
- Frontend React.
- Cadastro manual de plano no backend.
- Logica completa de conquistas.
- Relatorios JSON e PDF.
- Testes de integracao com banco.
