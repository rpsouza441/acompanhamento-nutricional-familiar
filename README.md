# NutriTracker

Sistema web para acompanhamento nutricional familiar. A familia registra agua, refeicoes, alimentos consumidos, observacoes, conquistas e relatorios a partir de planos nutricionais importados por JSON ou cadastrados manualmente.

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

## Estrutura

```text
.
|-- backend/
|   |-- src/main/java/com/nutritracker/
|   |   |-- config/
|   |   |-- controller/
|   |   |-- dto/
|   |   |-- exception/
|   |   |-- model/
|   |   |-- repository/
|   |   `-- service/
|   |-- src/main/resources/application.yml
|   |-- src/test/java/com/nutritracker/
|   |-- pom.xml
|   |-- mvnw
|   |-- mvnw.cmd
|   `-- Dockerfile
|-- frontend/
|   |-- src/components/
|   |-- src/context/
|   |-- src/hooks/
|   |-- src/pages/
|   |-- src/services/
|   |-- package.json
|   `-- Dockerfile
|-- database/
|   |-- 01-schema.sql
|   `-- 02-sample-data.sql
|-- docker-compose.yml
|-- nginx.conf
|-- .env.example
`-- README.md
```

## Setup com Docker

Pre-requisitos:

- Docker 24+
- Docker Compose v2

Setup em 3 comandos:

```bash
cp .env.example .env
# edite .env e troque senhas/JWT_SECRET antes de publicar o ambiente
docker compose up -d --build
```

Variaveis usadas pelo Compose:

```env
DB_ROOT_PASSWORD=nutritracker_root_2024
DB_USER=nutritracker
DB_PASSWORD=nutritracker_pass_2024
JWT_SECRET=change-this-secret-to-a-strong-256-bit-value-before-production
APP_CORS_ALLOWED_ORIGINS=http://localhost:18880,http://localhost:13017
```

Acessos padrao do Compose:

```text
Frontend: http://localhost:18880
API via Nginx: http://localhost:18880/api
Swagger via Nginx: http://localhost:18880/api/swagger-ui.html
Backend direto: http://localhost:18080
Swagger direto: http://localhost:18080/swagger-ui.html
```

## Credenciais de Exemplo

O seed `database/02-sample-data.sql` cria um usuario administrador:

```text
email: admin@nutritracker.local
senha: password
```

## Desenvolvimento Local

Backend local:

Pre-requisitos:

- Java 21
- `JAVA_HOME` apontando para a instalacao do Java 21
- MariaDB acessivel em `localhost:3306` com banco `nutritracker`
- Schema e seed aplicados a partir de `database/01-schema.sql` e `database/02-sample-data.sql`

```bash
cd backend
./mvnw spring-boot:run
```

No Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

O backend sobe em `http://localhost:18080`.

Variaveis opcionais do backend local, quando quiser sobrescrever os valores de `application.yml`:

```env
SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/nutritracker
SPRING_DATASOURCE_USERNAME=nutritracker
SPRING_DATASOURCE_PASSWORD=nutritracker_pass_2024
JWT_SECRET=change-this-secret-to-a-strong-256-bit-value-before-production
APP_CORS_ALLOWED_ORIGINS=http://localhost:13017,http://localhost:18880
```

Frontend local:

Pre-requisitos:

- Node.js 22 LTS ou versao compativel com Vite 5
- npm

```bash
cd frontend
npm install
npm run dev
```

O frontend de desenvolvimento sobe em `http://localhost:13017`.

Variaveis do frontend:

```env
VITE_API_BASE_URL=/api
VITE_API_PROXY_TARGET=http://localhost:18080
```

Para apontar para um backend remoto ou self-hosted, ajuste `VITE_API_PROXY_TARGET`, por exemplo:

```env
VITE_API_PROXY_TARGET=http://192.168.22.245:8880
```

## Testes e Build

Backend:

```bash
cd backend
./mvnw test
```

Windows:

```powershell
cd backend
.\mvnw.cmd test
```

Testes de integracao com MariaDB via Testcontainers:

```bash
cd backend
./mvnw verify -Pintegration
```

Esse fluxo requer Docker disponivel.

Frontend:

```bash
cd frontend
npm install
npm run build
```

CI:

- O workflow `.github/workflows/ci.yml` executa `./mvnw verify -Pintegration` no backend com Java 21.
- O mesmo workflow executa `npm ci` e `npm run build` no frontend com Node.js 22.

## Funcionalidades Implementadas

Backend:

- Autenticacao JWT com login, refresh e logout.
- CRUD administrativo de usuarios com soft-delete por campo `ativo`.
- Importacao de plano nutricional por JSON com validacao detalhada.
- Cadastro e edicao manual de planos nutricionais.
- Ativacao de plano por usuario.
- Registro diario com agua, refeicoes, alimentos e observacoes.
- Conquistas calculadas sob demanda e por scheduler diario.
- Relatorios JSON por periodo.
- Relatorio PDF server-side com grafico de agua.
- Swagger UI.

Frontend:

- Login com tratamento de erro.
- AuthContext global.
- Axios com Bearer token e refresh automatico em `401`.
- Dashboard com agua, progresso, refeicoes do dia e conquistas recentes.
- Registro de refeicao com selecao visual, entrada manual, horario e observacoes.
- Historico com indicadores de adesao.
- Conquistas com progresso e data de desbloqueio.
- Relatorios com filtros, tabelas, grafico e botao de PDF.
- Admin de usuarios.
- Admin de importacao de plano JSON.
- Admin de plano manual.

## Endpoints Principais

Autenticacao:

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

Usuarios:

- `GET /api/usuarios`
- `POST /api/usuarios`
- `PUT /api/usuarios/{id}`
- `PATCH /api/usuarios/{id}/ativo`

Planos:

- `POST /api/planos/importar`
- `POST /api/planos/manual`
- `GET /api/planos?usuarioId=`
- `GET /api/planos/{id}`
- `PUT /api/planos/{id}/manual`
- `PATCH /api/planos/{id}/ativar`

Registros:

- `GET /api/registros?usuarioId=&data=`
- `PUT /api/registros/{id}`
- `POST /api/registros/{id}/refeicoes/{refeicaoId}/concluir`
- `POST /api/registros/{id}/refeicoes/{refeicaoId}/alimentos`
- `DELETE /api/registros/{registroId}/alimentos/{alimentoId}`

Conquistas e relatorios:

- `GET /api/conquistas/usuario/{id}`
- `POST /api/conquistas/calcular/{usuarioId}`
- `GET /api/relatorios?usuarioId=&inicio=&fim=`
- `GET /api/relatorios/pdf?usuarioId=&inicio=&fim=`

Observacao de autorizacao:

- `ADMIN` pode consultar e alterar recursos de qualquer usuario.
- `USER` so pode acessar os proprios dados. Quando o endpoint aceitar `usuarioId`, o backend valida esse valor contra o usuario autenticado; em consultas do proprio usuario, o frontend pode omitir `usuarioId` nos endpoints que aceitam esse parametro como opcional.

## Importacao de Plano

Endpoint:

```text
POST /api/planos/importar
Content-Type: multipart/form-data
Authorization: Bearer <access-token>
Campo do arquivo: file
```

O plano importado e vinculado ao usuario autenticado pelo JWT. O endpoint nao usa `usuarioId` na query.

Exemplo de estrutura:

```json
{
  "configuracoes": {
    "meta_agua_diaria_ml": 3000,
    "objetivo": "Reeducacao alimentar",
    "profissional": "Profissional responsavel",
    "data_prescricao": "2025-10-29"
  },
  "refeicoes": [
    {
      "id": "desjejum",
      "nome": "Desjejum",
      "horario_sugerido": "06:20",
      "ordem": 1,
      "categorias": [
        {
          "nome": "Proteina",
          "tipo_selecao": "escolha_uma",
          "obrigatorio": true,
          "opcoes": [
            {
              "alimento": "Iogurte natural desnatado",
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
```

Validacoes:

- `configuracoes.meta_agua_diaria_ml` deve ser inteiro positivo.
- `refeicoes` deve conter pelo menos uma refeicao.
- Cada refeicao deve conter `id`, `nome` e `ordem`.
- Cada refeicao deve conter pelo menos uma categoria.
- `tipo_selecao` deve ser `escolha_uma`, `escolha_multipla` ou `livre`.
- Cada categoria deve conter pelo menos uma opcao.

## Relatorio PDF

O PDF e gerado pelo backend em:

```text
GET /api/relatorios/pdf?usuarioId=1&inicio=2026-04-01&fim=2026-04-30
```

Ele inclui resumo executivo, adesao por refeicao, tabela detalhada, grafico de agua com JFreeChart, conquistas e rodape com periodo.

## Portas

| Servico | Porta |
| --- | --- |
| Frontend dev Vite | `13017` |
| Backend Spring Boot | `18080` |
| Nginx / app Docker | `18880` |
| MariaDB container | `3306` interno |

## Status

Concluido:

- Backend principal.
- Frontend principal.
- Docker Compose com MariaDB, backend, frontend e Nginx.
- Portas nao convencionais configuradas.
- Testes unitarios de servicos do backend.
- Testes de integracao do backend com MariaDB via Testcontainers.
- Build de producao do frontend.

Pendencias conhecidas:

- Validacao final do Docker Compose em ambiente com Docker instalado.
- Ajustes finos de UX apos validar as telas com dados reais.
