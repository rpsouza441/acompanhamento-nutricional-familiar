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
├── frontend/
│   ├── src/
│   ├── package.json
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
- `POST /api/planos/manual`
- `GET /api/planos?usuarioId=`
- `GET /api/planos/{id}`
- `PUT /api/planos/{id}/manual`
- `PATCH /api/planos/{id}/ativar`
- `GET /api/registros?usuarioId=&data=`
- `PUT /api/registros/{id}`
- `POST /api/registros/{id}/refeicoes/{refeicaoId}/concluir`
- `POST /api/registros/{id}/refeicoes/{refeicaoId}/alimentos`
- `DELETE /api/registros/{registroId}/alimentos/{alimentoId}`
- `GET /api/conquistas/usuario/{id}`
- `POST /api/conquistas/calcular/{usuarioId}`
- `GET /api/relatorios?usuarioId=&inicio=&fim=`
- `GET /api/relatorios/pdf?usuarioId=&inicio=&fim=`

## Requisitos Locais

- Java 21
- Maven 3.9+
- Docker 24+
- Docker Compose v2

## Configuracao

Crie um arquivo `.env` na raiz a partir do exemplo:

```bash
cp .env.example .env
```

Edite o `JWT_SECRET` antes de usar fora do ambiente local.

## Rodando com Docker Compose

O Compose sobe MariaDB, backend, frontend e Nginx.

```bash
docker compose up -d --build
```

Acessos:

```text
Frontend: http://localhost:18880
API via Nginx: http://localhost:18880/api
Swagger via Nginx: http://localhost:18880/api/swagger-ui.html
Backend direto: http://localhost:18080
Swagger direto: http://localhost:18080/swagger-ui.html
```

O backend tambem aceita variaveis de ambiente Spring quando executado fora do Compose:

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

Ou usando o Maven Wrapper:

```bash
cd backend
./mvnw test
```

## Rodando o Frontend

Para desenvolvimento local:

```bash
cd frontend
npm install
npm run dev
```

Variavel de API para o frontend:

```env
VITE_API_BASE_URL=/api
VITE_API_PROXY_TARGET=http://localhost:18080
```

Para usar o backend em outro host durante desenvolvimento, ajuste `VITE_API_PROXY_TARGET`, por exemplo `http://192.168.22.245:8880`.

Build de producao:

```bash
cd frontend
npm run build
```

## Rodando o Backend

Com MariaDB disponivel e o schema aplicado:

```bash
cd backend
mvn spring-boot:run
```

A API sobe em:

```text
http://localhost:18080
```

Swagger UI:

```text
http://localhost:18080/swagger-ui.html
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
- Cadastro e edicao manual de planos nutricionais.
- Registro diario basico.
- Conquistas com calculo agendado e sob demanda.
- Relatorios JSON.
- Relatorio PDF server-side com grafico de agua.
- Schema MariaDB inicial.
- Testes unitarios iniciais.
- Docker Compose para MariaDB, backend e Nginx.
- Maven Wrapper no backend.
- Frontend React com Vite e TailwindCSS.

Pendencias principais:

- Testes de integracao com banco.
