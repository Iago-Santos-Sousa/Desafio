# DataPulse — ingestão em larga escala

Sistema React + Vite + TypeScript com API Java + Spring Boot, PostgreSQL e RabbitMQ. Upload CSV segue para volume por streaming; RabbitMQ dispara Spring Batch; inserts usam JDBC batch em chunks. Browser consulta progresso por polling e lista dados por cursor.

Back-end usa Java 21 LTS (Eclipse Temurin 21 em Docker) com Spring Boot 4.1.0. Versões ficam fixadas em `back-end/pom.xml` e `back-end/Dockerfile`.

Qualidade Java usa Spotless + Google Java Format para formatação e Checkstyle para lint. Maven e Docker validam regras; IntelliJ pode aplicar mesmas regras durante edição.

## Executar

Requisito único: Docker Desktop com Compose v2.

```bash
copy .env.example .env
docker compose up --build
```

- Front-end: http://localhost:5173
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- RabbitMQ: http://localhost:15672 (`app` / `change-me-local`)

Fluxo back-end detalhado está em [README_BACK_END.md](README_BACK_END.md). O contrato CSV usa `occurred_at,category,amount,description`.

Rotas front-end:

- `/dashboard`: cards, gráfico e tabela paginada.
- `/ingestions/new`: upload CSV.
- `/ingestions/:jobId`: status do processamento com polling.

Front-end usa React Router, TanStack Query, MUI e Tailwind CSS v4. Tailwind organiza layout/responsividade; MUI fornece componentes e tema. Back-end segue camadas controller, service, repository, DTO e entity.

## Desempenho

## Dashboard e filtros

Dashboard lista jobs em paginas de 10 com cursor, abre detalhe por ID e consulta transacoes do job em modal. Filtros `from`/`to` afetam cards e grafico; datas sao inclusivas no calendario `America/Sao_Paulo`.

## Feedback e diagnóstico

- Dashboard exibe jobs ativos por polling em `/api/v1/ingestions/active`.
- Status intermediário é gravado após cada chunk Spring Batch.
- CSV precisa usar `occurred_at,category,amount,description`; arquivos de `csv_tests` são rejeitados com `422` por incompatibilidade de domínio.
- Respostas HTTP têm Problem Details e `traceId`; front-end registra resposta sanitizada e mostra toast MUI.
- Containers nomeados: `datapulse-postgres`, `datapulse-rabbitmq`, `datapulse-api`, `datapulse-web`.

- Multipart é copiado para volume, sem materializar arquivo na RAM.
- Spring Batch lê linha a linha e confirma chunks de 2.000 registros.
- `reWriteBatchedInserts=true` reduz round-trips PostgreSQL.
- Paginação usa keyset por `id`; agregados são mantidos em `monthly_category_aggregate`.
- RabbitMQ fornece fila durável, backpressure e retry do worker.

Gerar 1 milhão de linhas sem Node local:

```bash
docker compose --profile tools run --rm csv-generator 1000000 /data/transactions-1m.csv
```

## Desenvolvimento

Dependências e toolchains locais são opcionais. Dockerfiles usam build multi-stage. Versões ficam fixadas em `pom.xml`, `package-lock.json`, Dockerfiles e Compose. Consulte documentação oficial/Context7 antes de alterar APIs ou versões.

## Limitações conhecidas

Volume local atende execução single-host. Escala distribuída exigiria object storage compartilhado e workers separados. Chunk size, heap e índices devem ser ajustados por benchmark com `EXPLAIN (ANALYZE, BUFFERS)` e carga representativa.
