# Plano de implementação

## 1. Objetivo e estado atual

## Implementacao — dashboard por jobs e filtros temporais

- Rota `/ingestions` lista todos os jobs em paginas de 10 usando cursor keyset por `created_at` e `id`; dashboard concentra metricas e detalhe permanece em `/ingestions/{jobId}`.
- DTO de detalhe expoe totais, contadores, timestamps e erro; `stored_path` nunca sai da API.
- Detalhe abre modal de transacoes com filtro de categoria por job, autocomplete incremental de 5 opcoes e paginas de 25 transacoes.
- Analytics aceita `from`/`to` inclusivos em `YYYY-MM-DD`; fuso fixo `America/Sao_Paulo`; limite superior e proximo dia `00:00` exclusivo.
- Migration V2 cria agregacao diaria por job e indexes; V3 preenche `started_at` de jobs antigos. Cards e grafico usam agregados diarios filtrados.
- Navbar reutilizavel usa `NavLink` com classe ativa automatica e `aria-current`.

## Correção operacional — progresso, CSV e observabilidade

- Polling permanece transporte oficial. `ChunkListener` persiste `readCount`, `writeCount` e `skipCount` após cada chunk; estado terminal encerra polling.
- `GET /api/v1/ingestions/active?limit=10` alimenta painel de jobs ativos no dashboard. Summary e transações atualizam enquanto houver job ativo; agregados atualizam ao concluir.
- Cabeçalho canônico validado antes da fila: `occurred_at,category,amount,description`. CSV de clientes em `csv_tests` retorna `422 CSV_HEADER_INVALID`, sem job, mensagem RabbitMQ ou arquivo residual.
- Front-end concentra chamadas TanStack Query em `src/hooks/api`, usa Skeleton/LinearProgress, interceptor Axios sanitizado e `ToastProvider` com MUI `Snackbar` + `Alert` e duração configurável.
- Back-end retorna Problem Details com `status`, `code`, `title`, `detail`, `timestamp` e `traceId`; logs SLF4J no console incluem `jobId` sem conteúdo CSV.
- Containers Compose: `datapulse-postgres`, `datapulse-rabbitmq`, `datapulse-api`, `datapulse-web`, `datapulse-csv-generator`.

## Atualização arquitetural — rotas e camadas

Implementação aprovada após revisão do estado real:

### Front-end

- Instalar `react-router` em versão estável atual e fixar resolução no `package-lock.json`. Usar `createBrowserRouter` + `RouterProvider`, conforme documentação oficial atual consultada via Context7.
- Rotas: `/` redireciona para `/dashboard`; `/dashboard` exibe métricas, agregados e progresso ativo; `/ingestions` lista jobs; `/ingestions/new` recebe CSV; `/ingestions/:jobId` acompanha job; `*` exibe 404.
- TanStack Query continua dono de estado remoto e polling. React Router cuida de navegação, parâmetros e composição de páginas.
- Estrutura: `app`, `layouts`, `pages`, `features`, `components`, `integrations/api`, `types` e `utils`. `pages` compõe rotas; `features` guarda comportamento de domínio; `integrations` isola HTTP; `utils` contém apenas funções puras reutilizáveis.
- Filtro de categoria usa query string. Context API fica reservado a estado global real de UI; não manter provider para filtro local.
- Tailwind CSS v4 permanece ativo pelo plugin oficial Vite e será usado para layout/responsividade. MUI permanece fonte de componentes, acessibilidade e tema. Evitar duplicidade entre `sx` e classes.

### Back-end

- Controllers permanecem finos e sem `JdbcTemplate` ou SQL.
- `transaction` e `analytics` passam a ter `controller`, `dto`, `service` e `repository` separados. Queries keyset, resumo e agregados ficam nos repositories.
- DTOs viram records de topo. Entidades JPA nunca atravessam fronteira HTTP.
- Batch, messaging e ingestão continuam separados por responsabilidade; listeners orquestram e não escondem exceções.
- Criar `utils` apenas para função pura compartilhada. Parser, mapper e validação específicos ficam junto da feature.

### Critérios adicionais

- Refresh direto em qualquer rota funciona no Nginx existente (`try_files ... /index.html`).
- Contratos REST e schema PostgreSQL permanecem compatíveis.
- Verificar TypeScript, lint, build Docker do back-end e smoke de endpoints após refatoração.

## Implementacao — rota de jobs e filtros MUI

- Dashboard deixa de renderizar a tabela `Jobs processados`; mantém apenas painel de ingestões ativas com polling para preservar feedback em tempo real.
- Nova rota `/ingestions` renderiza listagem de jobs com cursor keyset, dez itens por página e navegação para `/ingestions/:jobId`.
- Navbar recebe link `Jobs processados` com `NavLink`, classe ativa e `aria-current`; `/dashboard`, `/ingestions` e `/ingestions/new` ficam acessíveis diretamente.
- Filtros de período usam `@mui/x-date-pickers` com `LocalizationProvider`, `AdapterDateFns` e locale `pt-BR`; `date-fns` converte `Date` para `yyyy-MM-dd` sem serialização UTC.
- Data padrão continua primeiro dia do mês corrente até data atual em `America/Sao_Paulo`; backend mantém interpretação inclusiva do fim do período.
- Dependências `@mui/x-date-pickers` `9.14.0` e `date-fns` `4.4.0` ficam resolvidas no `package.json`/`package-lock.json`; API, migrations e contratos backend permanecem inalterados.

## Implementacao — React Hook Form e validacao de datas

- `react-hook-form` controla formulario de periodo do dashboard; `Controller` integra DatePicker MUI e exibe `helperText` de erro abaixo de cada campo.
- Campos de data foram extraidos para `src/components/DateRangeFields.tsx`; regras validam formato, ano `1900..ano atual`, datas futuras, intervalo e calendario bissexto.
- Backend valida a mesma politica em `DateRangeResolver`; `LocalDate`/ISO rejeita datas inexistentes, como `2023-02-29`, e aceita `2024-02-29`. Ano `0236` retorna `400 INVALID_DATE_RANGE`.
- `useCursorPagination` concentra cursor, historico, navegacao e reset para jobs e transacoes; paginacao server-side e keyset permanecem inalteradas.
- Testes unitarios cobrem ano suportado, bissexto valido, ano antigo, futuro e intervalo invertido.

## Implementacao - bloqueio manual e persistencia dos filtros

- `DateRangeFields` usa `slotProps.field.readOnly`, bloqueando digitacao e colagem sem desabilitar botão, abertura ou selecao do calendario MUI.
- `DashboardDateFiltersProvider` e `useDashboardDateFilters` mantem `from`/`to` em Context API e persistem somente strings `yyyy-MM-dd` na chave versionada `datapulse.dashboard.date-range.v1` do `sessionStorage`.
- Payload ausente, corrompido, incompleto, antigo, futuro ou fora do intervalo suportado e descartado; o periodo padrao segue primeiro dia do mes ate hoje em `America/Sao_Paulo`.
- Precedencia definida: par valido na URL vence session storage; sem URL valida, usar sessao; sem sessao valida, usar padrao. Dashboard sincroniza Context, URL e storage sem criar historico extra na hidratacao.
- Provider envolve `RouterProvider`, portanto valores sobrevivem a navegacao entre `/dashboard`, `/ingestions`, `/ingestions/new` e `/ingestions/:jobId`; estado remoto continua no TanStack Query.
- `isValidDateRange` passa a aplicar tambem limites de ano/data para validar URL e storage antes das consultas.
- Validar com build, lint e fluxo manual de digitacao/colagem, selecao pelo calendario, navegacao, reload da aba, URL valida e storage corrompido.

Construir sistema containerizado capaz de receber CSV com mais de 1 milhão de registros, processar sem crescimento proporcional de RAM, consultar progresso, listar dados eficientemente e exibir dashboard React responsivo.

Estado inicial em 2026-09-16:

- `front-end`: scaffold React 19 + Vite 8 + TypeScript 6, ainda com tela inicial.
- `back-end`: diretório vazio; Java/Spring Boot serão criados e compilados via Docker.
- Infraestrutura, banco, contratos e testes ainda inexistentes.

Implementado nesta etapa:

- Back-end usa Java 21 LTS (Eclipse Temurin 21 no build/runtime Docker) com Spring Boot 4.1.0 + Spring Batch 6 + RabbitMQ + PostgreSQL + Flyway.
- Front-end React/Vite/TypeScript com Axios, TanStack Query, MUI, Tailwind e Chart.js.
- Docker Compose, Dockerfiles multi-stage, gerador CSV e README operacional.
- Smoke test executado com cinco registros; testes automatizados de carga ainda pendentes.

## Migração implementada — Java 21 LTS

- Java 25 foi substituído por Java 21 LTS no compilador Maven e no runtime Docker.
- Spring Boot permanece em 4.1.0; requisitos da linha aceitam Java 17 ou superior, portanto Java 21 mantém compatibilidade.
- Busca estática não encontrou APIs exclusivas do Java 25 no código da aplicação.
- APIs REST, migrations, contratos, processamento Spring Batch e dependências de infraestrutura não foram alterados.
- Validação executada: build limpo da imagem Java 21, health check, migrations e smoke test de status, analytics e transações.

## Plano implementado — lint e formatação Java 21

- Spotless Maven `3.10.2` + Google Java Format `1.35.0` padronizam fontes Java; `spotless:apply` corrige e `spotless:check` bloqueia divergências.
- Maven Checkstyle `3.6.0` com engine `14.1.0` aplica regras versionadas em `back-end/config/checkstyle/checkstyle.xml`.
- `.editorconfig` do back-end fixa UTF-8, LF, newline final e indentação compatível com Google Java Format.
- IntelliJ usa plugins `google-java-format` e CheckStyle-IDEA; Maven/Docker permanecem fonte de verdade fora da IDE.
- Todos os 40 arquivos Java existentes devem ser formatados no baseline inicial; lint não altera contratos REST, schema ou comportamento.
- Docker executa `mvn -DskipTests verify`, aplicando Spotless e Checkstyle antes da imagem final.

## Implementacao concluida — organizacao do modulo ingestion

- O modulo `back-end/src/main/java/com/desafio/ingestion/ingestion` foi reorganizado por responsabilidade:
  - `controller`: endpoints REST e mapeamento de entrada/saida HTTP.
  - `dto`: records publicos (`IngestionAcceptedResponse`, listagem e detalhe); entidades JPA nao atravessam a API.
  - `entity`: `IngestionJob` e `JobStatus`.
  - `repository`: repositorios Spring Data JPA e consulta keyset.
  - `service`: casos de uso de aceite, consulta e progresso.
  - `batch`: configuracao Spring Batch, listeners e `TransactionRow`.
  - `messaging`: contrato `JobMessage` e listener RabbitMQ.
  - `validation`: validacao de cabecalho e excecao de formato CSV.
  - `cursor`: codec e excecao de cursor opaco.
- `IngestionController` agora retorna `IngestionAcceptedResponse` tipado no `POST /api/v1/ingestions`; JSON permanece compativel (`jobId`, `status`, `statusUrl`).
- JPQL, handlers de excecao, conversor RabbitMQ e entidades de transacao foram atualizados para os novos pacotes.
- `JdbcBatchItemWriter` e fluxo Spring Batch permanecem preservados para o caminho de alto volume; nenhuma migration ou rota foi alterada.
- Front-end nao exigiu mudanca de codigo: contratos REST permanecem compativeis e estrutura existente continua valida.
- Validacao executada: `docker compose build back-end` passou com Maven `verify`, Spotless e Checkstyle.

## Plano implementado — configuração IntelliJ para Checkstyle

- `README_BACK_END.md` documenta configuração do projeto com Java 21, instalação dos plugins `CheckStyle-IDEA` e `google-java-format` e reload Maven.
- CheckStyle-IDEA deve usar `Use a local Checkstyle file` apontando para `$PROJECT_DIR$/back-end/config/checkstyle/checkstyle.xml`, com engine `14.1.0` e escopo Java incluindo testes.
- Quando `back-end` for aberto isoladamente, o caminho equivalente é `$PROJECT_DIR$/config/checkstyle/checkstyle.xml`.
- Google Java Format `1.35.0` deve ser ativado no projeto; os seis `--add-exports` do `jdk.compiler` ficam documentados para IntelliJ quando necessários.
- IntelliJ fornece feedback e formatação local; Maven e Docker permanecem gates oficiais. Nenhuma configuração `.idea` específica de máquina é obrigatória ou versionada.
- Validação documentada: `mvn spotless:check`, `mvn checkstyle:check` e `mvn -DskipTests verify`; sem Maven local, executar os comandos pelo container Java 21.

## Plano implementado — migração JdbcTemplate para Spring Data JPA

- `JdbcTemplate` foi removido dos repositories de consulta de jobs, transações e analytics; o código da aplicação não possui mais uso direto dessa API.
- Jobs usam repository Spring Data com JPQL, constructor projection e cursor keyset por `(createdAt, id)`.
- Transações usam entidade JPA, `JpaRepository` e fragmento customizado com Criteria API para filtros opcionais, projeções DTO e paginação por cursor sem `OFFSET`.
- Agregados diários usam entidade JPA e chave composta; resumo usa JPQL, série mensal usa projection nativa pontual e refresh usa `delete` + `INSERT ... SELECT` transacional com timezone configurável.
- `JdbcBatchItemWriter` permanece no Spring Batch para o caminho crítico de milhões de inserts, preservando chunks, memória limitada e throughput.
- Schema, migrations, contratos REST e respostas públicas permanecem compatíveis; entidades JPA não atravessam a camada HTTP.
- Validação executada: Spotless, Checkstyle, compilação Java 21, build Docker, inicialização com Hibernate `ddl-auto=validate`, endpoints de jobs/transações/categorias/analytics e upload CSV de smoke.

## 2. Decisões arquiteturais

### 2.1 Pipeline escolhido

```text
Browser -> REST upload -> arquivo em volume + ingestion_job -> RabbitMQ(jobId)
                                                        |
                                                        v
                         Spring Batch reader -> processor -> JDBC batch -> PostgreSQL
                                                        |
Browser <- polling status/metrics/listagem <- REST APIs -+
```

Fluxo:

1. API valida metadados e transmite multipart diretamente para arquivo temporário controlado.
2. Cria `ingestion_job` com status `RECEIVED`.
3. Publica mensagem persistente com `jobId`; muda status para `QUEUED`.
4. Consumer RabbitMQ inicia Spring Batch com parâmetros únicos.
5. `FlatFileItemReader` lê linha a linha; processor valida e converte; `JdbcBatchItemWriter` insere chunks.
6. Listener atualiza contadores com frequência limitada.
7. Etapa final consolida agregados mensais de forma idempotente e marca status terminal.
8. Front-end acompanha status por polling adaptativo e invalida métricas/listagens ao concluir.

### 2.2 Por que RabbitMQ + Spring Batch

- RabbitMQ dá fila durável, backpressure, ack/retry e desacopla requisição do worker.
- `@Async`/thread pool isolado perderia fila em restart e dificultaria backpressure.
- Spring Batch fornece chunks, transações, skip/retry controlado, metadados e reinício.
- Multithreading fica limitado inicialmente a um consumer/step. Aumentar concorrência só após benchmark, pois CSV único, disco e banco podem virar gargalo.
- Mensagem nunca transporta CSV; apenas ID e metadados mínimos.

### 2.3 Por que polling

- Status muda em segundos, não em milissegundos.
- Polling com TanStack Query simplifica reconexão, balanceamento e estados de cache.
- Intervalo inicial sugerido: 1 segundo durante upload/fila, 2 segundos durante processamento, desligado em estado terminal e com backoff em erro.
- SSE pode ser evolução futura se medição provar excesso de requests. WebSocket não agrega valor para fluxo unidirecional atual.

### 2.4 Persistência rápida e memória limitada

- Nunca usar `MultipartFile#getBytes`, `readAllLines`, `List` global ou persistência linha a linha.
- Arquivo recebido por stream para volume com quota/limpeza.
- Chunk inicial: 2.000 registros; faixa de benchmark: 1.000, 2.000 e 5.000.
- JDBC URL com `reWriteBatchedInserts=true`; prepared statements e transação por chunk.
- `BigDecimal` para valores; parser de timestamp explícito; encoding UTF-8.
- JPA/Hibernate para `ingestion_job` e operações convencionais. JDBC batch no hot path reduz persistence context e overhead de dirty checking.
- Heap do container limitada durante benchmark para demonstrar uso constante, por exemplo 512 MiB. Valor final depende de teste.

### 2.5 Paginação e agregação

- Listagem usa cursor opaco baseado em `(id)` ou `(occurred_at,id)` conforme ordenação final.
- Cursor evita custo crescente de `OFFSET` em páginas profundas.
- Filtros aceitos: job, categoria e intervalo de datas. Tamanho máximo de página limitado.
- Dashboard consulta `monthly_category_aggregate`, não executa agrupamento completo sobre milhões de linhas a cada refresh.
- Consolidação usa UPSERT idempotente por `(month, category)` e job concluído.

## 3. Stack implementada e alvo

Versões exatas estão confirmadas e fixadas nos arquivos de build/runtime. Não usar `latest` na entrega.

### Back-end

- Java 21 LTS, configurado em `<java.version>21</java.version>` no `back-end/pom.xml`.
- Spring Boot 4.1.0, configurado no parent Maven `spring-boot-starter-parent`.
- Docker usa Maven/Eclipse Temurin 21 no build e Eclipse Temurin 21 JRE no runtime.
- Maven Wrapper dentro do projeto e Maven em estágio de build Docker.
- Spring Web MVC, Validation, Data JPA, Batch, AMQP, Actuator.
- PostgreSQL driver, Flyway, Apache Commons CSV quando necessário pelo reader, MapStruct opcional somente se reduzir mapeamento manual real.
- `springdoc-openapi` compatível com Spring Boot fixado.
- JUnit 5, Spring Boot Test, Testcontainers PostgreSQL/RabbitMQ.

### Front-end

- React 19, Vite 8 e TypeScript 6 já instalados.
- Axios, TanStack Query v5, MUI, Tailwind CSS v4 com `@tailwindcss/vite`, Chart.js e wrapper React compatível.
- Context API apenas para preferências/estado global de UI; estado remoto fica no TanStack Query.

### Infraestrutura

- PostgreSQL estável suportado e RabbitMQ Management com tags fixadas.
- Nginx ou servidor estático enxuto no estágio runtime do front-end.
- Docker Compose v2 com healthchecks, redes internas, volumes nomeados e limites documentados.

## 4. Modelo de dados inicial

### `ingestion_job`

- `id UUID` PK.
- `original_filename`, `stored_path`, `file_size_bytes`, checksum opcional.
- `status` com enum persistido de forma estável.
- `total_rows`, `processed_rows`, `valid_rows`, `invalid_rows`.
- `error_summary` limitado.
- `created_at`, `queued_at`, `started_at`, `finished_at`, `updated_at`.
- Campo de versão para concorrência otimista.

### `transaction_record`

- `id BIGINT GENERATED ...` PK.
- `ingestion_job_id UUID` FK.
- `occurred_at TIMESTAMPTZ`.
- `category VARCHAR` normalizada.
- `amount NUMERIC(19,4)`.
- `description VARCHAR` nullable e limitada.
- `created_at TIMESTAMPTZ`.

### `monthly_category_aggregate`

- `month DATE` normalizado para primeiro dia.
- `category VARCHAR`.
- `total_amount NUMERIC(24,4)`.
- `transaction_count BIGINT`.
- PK `(month, category)`.

### Índices candidatos

- `transaction_record (ingestion_job_id, id)` para auditoria/paginação por job.
- `transaction_record (occurred_at, id)` para período + cursor.
- `transaction_record (category, occurred_at, id)` somente se filtro combinado justificar.
- Não criar índices redundantes. Confirmar todos com `EXPLAIN (ANALYZE, BUFFERS)` em 1M+ linhas.

## 5. Contrato REST planejado

- `POST /api/v1/ingestions` — multipart, retorna `202`, `jobId`, `statusUrl`.
- `GET /api/v1/ingestions/{jobId}` — status, contadores, percentual quando total conhecido, timestamps e erro resumido.
- `GET /api/v1/transactions?cursor=&size=&jobId=&category=&from=&to=` — página por cursor e `nextCursor`.
- `GET /api/v1/analytics/summary` — totais gerais/cards.
- `GET /api/v1/analytics/monthly-by-category?from=&to=&category=` — séries agregadas.
- `GET /actuator/health` — readiness/liveness para Compose.
- `/swagger-ui.html` e `/v3/api-docs` — documentação viva.

Erros usam status HTTP corretos e corpo consistente: código, título, detalhe seguro, campos inválidos, timestamp e trace ID. Limites retornam `413`; CSV inválido, `422`; job inexistente, `404`.

## 6. Estrutura planejada

```text
back-end/
  src/main/java/.../
    ingestion/       controller, application, domain, infrastructure
    transaction/     controller, service, repository, mapper
    analytics/       controller, service, repository
    shared/           errors, config, observability
  src/main/resources/
    db/migration/
    application.yml
  src/test/
  pom.xml
  mvnw
  Dockerfile
front-end/
  src/
    app/              providers, router, theme
    layouts/          shell compartilhado
    pages/            composiÃ§Ã£o das rotas
    features/         ingestion, transactions, dashboard
    components/       componentes visuais reutilizÃ¡veis
    integrations/api/ cliente Axios e endpoints
    types/             contratos TypeScript
    utils/             helpers puros
  Dockerfile
scripts/
  generate-csv.mjs
docker-compose.yml
.env.example
```

Estrutura pode ser ajustada para padrões reais encontrados durante implementação. Evitar arquivos/camadas vazias.

## 7. Fases de implementação

### Fase 1 — Fundação e Compose

- Gerar Spring Boot via container/documentação oficial, sem instalar Java host.
- Criar Maven Wrapper, configuração por ambiente e health endpoint.
- Criar Dockerfiles multi-stage para back e front.
- Criar Compose com PostgreSQL, RabbitMQ, back-end e front-end; healthchecks e `depends_on` por saúde.
- Criar `.env.example`, `.gitignore` para `.env`, uploads e CSVs grandes.
- Critério: `docker compose up --build` inicia stack saudável em máquina limpa.

### Fase 2 — Schema e contratos

- Criar migrations Flyway para jobs, transações, agregados, FKs e índices mínimos.
- Modelar entidades/DTOs/enums e Problem Details.
- Configurar OpenAPI/Swagger.
- Documentar contrato CSV e limites.
- Critério: migrations reproduzíveis; schema validado pelo Hibernate; OpenAPI acessível.

### Fase 3 — Upload e fila

- Implementar streaming seguro para arquivo, validação de cabeçalho/tamanho e limpeza em falha.
- Persistir job e publicar mensagem confirmável/persistente no RabbitMQ.
- Configurar exchange, queue, routing key, DLQ, retry limitado e observabilidade.
- Implementar status endpoint.
- Critério: request retorna `202`; reinício não perde job aceito; arquivo não entra em RAM integralmente.

### Fase 4 — Ingestão em chunks

- Configurar Spring Batch job/step, reader, validator/processor, writer JDBC e listeners.
- Atualizar contadores sem write por linha.
- Definir política para linha inválida, limite de amostras e status parcial/falha.
- Garantir restart/idempotência e limpeza do arquivo após estado terminal conforme retenção.
- Critério: CSV 1M+ processa com heap estável e sem duplicar em retry.

### Fase 5 — Consulta otimizada

- Implementar paginação keyset e cursor opaco.
- Criar consolidação de agregados e endpoints de cards/gráficos.
- Rodar planos SQL com volume representativo; ajustar índices com evidência.
- Critério: latência não cresce linearmente com profundidade da página; dashboard não varre tabela bruta por refresh.

### Fase 6 — Front-end

- Instalar versões atuais compatíveis das bibliotecas autorizadas.
- Criar providers, tema, cliente Axios e query keys.
- Criar tela de upload com progresso de envio separado do progresso de ingestão.
- Criar polling adaptativo, cards, gráfico e tabela paginada server-side.
- Cobrir estados, responsividade, acessibilidade e cancelamento.
- Critério: DOM permanece limitado e UI continua interativa durante job grande.

### Fase 7 — Massa, testes e documentação

- Criar gerador determinístico de CSV 1M+ executado via container/profile Compose.
- Adicionar testes unitários e integrações Testcontainers focados no pipeline.
- Medir memória, throughput, latência de páginas e agregações sob limites declarados.
- Finalizar README principal, decisões, trade-offs e troubleshooting.
- Critério: avaliador executa stack e cenário sem toolchain local.

## 8. Estratégia de testes e validação futura

- Unidade: parsing, normalização, validação, cursor, mappers e transições de status.
- Integração: migrations, JDBC batch, constraints, queries, RabbitMQ ack/retry/DLQ e endpoints.
- Front-end: hooks de polling, parada terminal, tabela, upload e estados de erro.
- Carga: 1M+ linhas, CSV inválido parcial, arquivo acima do limite, consumer reiniciado e banco temporariamente indisponível.
- SQL: `EXPLAIN (ANALYZE, BUFFERS)` para filtros, keyset e agregados.
- Memória: heap/container monitorados do início ao fim; comparar RSS e GC em chunks diferentes.
- Reprodutibilidade: rebuild sem cache e subida em ambiente limpo, somente quando autorizado.

## 9. Observabilidade e segurança

- Logs estruturados com `jobId` e trace ID; nunca logar conteúdo completo do CSV.
- Métricas: jobs por status, linhas/s, duração, skips, falhas, profundidade da fila e tempo de query.
- Actuator expõe somente endpoints necessários.
- Limitar tamanho de upload, page size, comprimentos e categorias.
- Sanitizar nome de arquivo; impedir path traversal; volume sem execução.
- Credenciais fora do repositório; usuário PostgreSQL sem privilégios administrativos.
- CORS restrito ao front-end local configurado.

## 10. Riscos e mitigação

- Contar total de linhas exige passe extra: percentual pode ficar indeterminado inicialmente; mostrar linhas processadas e fase. Contagem opcional por streaming se UX exigir.
- RabbitMQ confirma mensagem, mas transação DB + broker não é atômica: usar estado reconciliável/outbox se testes mostrarem janela problemática.
- Tabela-resumo pode divergir em retry: agregação deve ser reconstruível/idempotente por job ou calculada em etapa transacional controlada.
- Muitos índices reduzem insert throughput: começar mínimo e adicionar por plano medido.
- Integrações atuais são compiladas com Java 21 e Spring Boot 4.1.0; ao atualizar qualquer versão, revisar matriz de compatibilidade de `springdoc`, Batch e Testcontainers.
- Volume local não equivale a object storage: suficiente para Compose; produção distribuída exigiria S3/MinIO e workers separados.
- Polling em muitos clientes aumenta tráfego: intervalo adaptativo agora; SSE permanece alternativa futura.

## 11. Definição de pronto

- Um comando Docker Compose sobe todos os serviços sem dependências host além de Docker.
- Upload de 1M+ linhas retorna `202` e processa em background sem OOM.
- Status apresenta progresso e erros limitados.
- Inserts são batched; tamanho e resultado medidos.
- Listagem usa cursor; dashboard usa agregados otimizados.
- Índices possuem justificativa e plano SQL registrado.
- Front-end permanece responsivo, acessível e não cria DOM proporcional ao dataset.
- Swagger, variáveis, gerador de dados, execução e decisões arquiteturais documentados.
- Testes relacionados e verificações autorizadas passam; limitações reais ficam explícitas.
