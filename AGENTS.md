# Contexto global do projeto

Estas instruções valem para todo o repositório, incluindo `front-end` e `back-end`.

## Fonte de verdade e leitura inicial

## Correção de progresso e observabilidade

## Dashboard por jobs e datas

- Listar jobs com keyset, dez itens por pagina; nunca usar `OFFSET` para navegacao profunda.
- Detalhe deve expor contadores/timestamps em DTO e abrir transacoes filtradas pelo proprio job.
- Categorias usam endpoint server-side, autocomplete e paginas de cinco itens.
- Analytics recebe datas inclusivas `YYYY-MM-DD`; resolver limites em `America/Sao_Paulo` e persistir instantes UTC.
- Cards e grafico consultam agregacao diaria por job; nao agrupar tabela bruta em cada refresh.
- Navbar usa `NavLink`/classe `active`; manter acessibilidade e query string compartilhavel.

- Polling é transporte padrão. Persistir progresso por chunk com `readCount`, `writeCount` e `skipCount`; nunca atualizar por linha.
- Dashboard consulta `/api/v1/ingestions/active`; polling termina em estado terminal.
- Validar cabeçalho CSV antes de enfileirar. Schema aceito: `occurred_at,category,amount,description`. Rejeitar incompatíveis com `422` e limpar arquivo.
- TanStack Query só aparece em hooks customizados dentro de `src/hooks/api`; componentes não chamam `useQuery`/`useMutation` diretamente.
- Toast global usa MUI `Snackbar` + `Alert`, com `autoHideDuration` customizável e sem duplicação durante polling.
- Axios registra response sanitizada; Problem Details deve preservar status, código, detalhe e trace ID.
- Back-end registra erros no console via SLF4J, com `jobId`/`traceId`; não imprimir conteúdo completo de CSV.
- Compose usa nomes fixos `datapulse-postgres`, `datapulse-rabbitmq`, `datapulse-api`, `datapulse-web` e `datapulse-csv-generator`.

## Atualização de arquitetura front-end

- Usar `react-router` estável atual com `createBrowserRouter` e `RouterProvider`.
- Rotas oficiais: `/dashboard`, `/ingestions`, `/ingestions/new`, `/ingestions/:jobId`, raiz com redirect e fallback 404.
- Manter estado remoto em TanStack Query. Query string representa filtros compartilháveis; Context API somente para estado global de UI.
- Responsabilidades: `pages` compõe rotas; `features` concentra domínios; `components` é compartilhado; `integrations/api` encapsula Axios; `utils` contém funções puras tipadas.
- Tailwind v4 cuida de layout/utilitários; MUI cuida de componentes e tema. Não duplicar estilos.

## Atualização de arquitetura back-end

- Todo endpoint deve seguir `controller -> service -> repository`.
- Controllers não podem acessar `JdbcTemplate`, SQL ou entidades diretamente.
- DTOs públicos ficam fora de controllers, preferencialmente como records. Entidades JPA nunca são retornadas.
- Repositories isolam JPA/JDBC e mapeiam resultados. Services orquestram casos de uso.
- Não criar pacote genérico `utils` sem função pura reutilizada; manter parser/mapper/validator junto da feature.

Antes de alterar código:

1. Leia `TASK.md` e `PLAN.md`.
2. Leia arquivos existentes diretamente relacionados à mudança.
3. Reutilize padrões, componentes, serviços, utilitários e contratos existentes.
4. Consulte `README_BACK_END.md` quando mudar execução local do back-end.

Se requisito, plano e código divergirem, preserve comportamento existente quando seguro e registre divergência. Não invente requisito silenciosamente.

## Versões e documentação

- Use versões estáveis mais recentes e mutuamente compatíveis no momento da instalação inicial.
- Fixe versões resolvidas nos arquivos de build/lock. Não use tags flutuantes de imagens Docker em entrega reproduzível.
- Antes de adicionar, atualizar ou configurar framework/biblioteca, consulte documentação oficial atual. Prefira Context7 para APIs de bibliotecas; use site oficial quando Context7 não cobrir versão necessária.
- Não presuma sintaxe antiga de Spring Boot, Spring Batch, Spring AMQP, React, Vite, TypeScript, Tailwind CSS, MUI, TanStack Query, Axios ou Chart.js.
- Não atualize dependências existentes fora do escopo. Dependência de produção nova exige autorização.
- Baseline fixado: Java 21 LTS, Spring Boot 4.1.0, React 19, Vite 8 e TypeScript 6. Java/Spring Boot estão definidos em `back-end/pom.xml` e `back-end/Dockerfile`; confirme compatibilidade de `springdoc-openapi` antes de atualizar.
- Back-end usa Spotless + Google Java Format para formatação e Checkstyle para lint. Após alterar Java, executar `spotless:check` e `checkstyle:check`; `mvn verify` é gate do build Docker.

## Arquitetura obrigatória

- Execução completa via Docker Compose, sem exigir Java, Maven, Node ou PostgreSQL instalados na máquina host.
- Um container por serviço: front-end, back-end, PostgreSQL e RabbitMQ.
- Dockerfiles multi-stage. Imagens e dependências com versões fixadas.
- Credenciais somente por variáveis de ambiente. Versione `.env.example`, nunca `.env` real.
- API REST versionada em `/api/v1`; erros no padrão Problem Details quando suportado.
- Swagger/OpenAPI exposto pelo back-end.
- JPA/Hibernate é ORM Java. Não usar TypeORM, pois pertence ao ecossistema Node.js.
- Flyway controla schema e migrations. `ddl-auto` deve validar, nunca criar/atualizar schema em produção.
- RabbitMQ desacopla aceite do upload e ingestão. Mensagem carrega identificador do job, não conteúdo CSV.
- Spring Batch processa CSV em streaming e chunks limitados. Não carregar arquivo ou conjunto completo em RAM.
- Metadados/status podem usar JPA. Caminho crítico de milhões de inserts usa JDBC batch por `JdbcBatchItemWriter`, com transações por chunk e driver PostgreSQL configurado para reescrita de batches.
- Paginação de transações usa cursor/keyset estável; evitar `OFFSET` alto.
- Agregações consultam tabela-resumo mensal, atualizada de forma idempotente após ingestão, evitando `GROUP BY` integral por requisição.
- Polling é transporte padrão de progresso. TanStack Query interrompe polling em status terminal.

## Back-end — Java e Spring Boot

- Código em Java com tipos específicos, records para DTOs quando adequado e injeção por construtor.
- Separar controller, application/service, domain, infrastructure/repository e mapper sem criar camadas vazias.
- Controllers tratam HTTP; services orquestram casos de uso; repositories isolam persistência.
- Aplicar SOLID com foco em responsabilidade única e extensão por interfaces apenas onde houver variação real.
- Preferir composição. Decorator somente quando adicionar comportamento transversal real, como métricas ou validação do leitor.
- Entidades JPA não devem ser retornadas pela API. Mapear para DTOs.
- Valores monetários usam `BigDecimal`/`NUMERIC`, nunca `double`.
- Datas usam `Instant`, `OffsetDateTime` ou `LocalDate` conforme semântica; persistir timestamps em UTC.
- Upload deve validar extensão, tipo, tamanho e cabeçalho CSV. Gerar nome interno; nunca confiar no nome original como caminho.
- Gravar upload em volume temporário por streaming. Retornar `202 Accepted` depois que corpo foi recebido e job persistido/enfileirado.
- Consumer com concorrência limitada e prefetch coerente. Ack somente após disparo/processamento seguro conforme desenho final.
- Job deve ter estados explícitos: `RECEIVED`, `QUEUED`, `PROCESSING`, `COMPLETED`, `COMPLETED_WITH_ERRORS`, `FAILED`.
- Progresso atualizado por chunk ou intervalo limitado; não executar update por linha.
- Linhas inválidas devem ser contadas e amostradas com limite. Não persistir stack trace ou milhões de erros individuais.
- Operações de retry precisam ser idempotentes. Evitar duplicidade com chave do job e regras de reinício do Spring Batch.
- Índices devem corresponder aos filtros e ordenação reais. Evitar excesso de índices na tabela de ingestão.
- Testes principais: unidade para parser/validação/mapeamento; integração com PostgreSQL/RabbitMQ reais via Testcontainers; API para contratos e paginação.

## Front-end — React, Vite e TypeScript

- Manter TypeScript estrito. Não introduzir `any`; prefira DTOs e unions discriminadas.
- Usar componentes funcionais, hooks e `async/await`.
- TanStack Query controla estado remoto: upload, status, métricas e páginas. Context API guarda apenas estado global de UI que não pertença ao servidor.
- React Hook Form controla formulários de consulta e validação de campos; componentes MUI controlados usam `Controller`.
- Campos de data do MUI devem usar `slotProps.field.readOnly`; bloquear teclado/colagem sem desabilitar botão ou seleção do calendário.
- Filtros de data compartilhados entre rotas usam Context API com `sessionStorage` versionado; validar e descartar payloads inválidos.
- Axios fica em cliente HTTP central, com base URL, timeout e tratamento de erro coerente.
- Consultas paginadas incluem cursor e filtros no `queryKey`; usar `placeholderData` para transição estável.
- Estado de cursor, histórico, próxima/anterior e reset deve ficar em hooks personalizados reutilizáveis; componentes não duplicam essa lógica.
- Polling usa `refetchInterval` adaptativo e para em estado terminal ou componente desmontado.
- Nunca renderizar milhões de registros. Usar paginação server-side; virtualization só se página visível ainda for grande.
- MUI fornece componentes acessíveis. Filtros de calendário usam MUI X Date Pickers com `LocalizationProvider` e `AdapterDateFns`; `date-fns` formata valores de calendário sem `toISOString()`.
- Filtros de data aceitam somente calendário entre `1900-01-01` e hoje em `America/Sao_Paulo`; frontend e backend devem rejeitar datas inválidas, futuras e anos não suportados.
- Tailwind CSS v4 usa plugin oficial `@tailwindcss/vite`; evitar dois sistemas disputando reset, spacing e tema.
- Centralizar tokens de cores, tipografia, espaçamento e estados no tema. Visual claro e vivo, mantendo contraste WCAG AA.
- Chart.js recebe apenas dados agregados. Destruir/atualizar instâncias via wrapper React oficial.
- Composition pattern e componentes reutilizáveis, sem abstrair componente usado uma única vez sem ganho claro.
- Estados obrigatórios: inicial, upload em andamento, fila/processamento, sucesso, vazio, erro recuperável e erro terminal.
- Acessibilidade: labels, foco visível, navegação por teclado, `aria-live` para progresso e mensagens úteis.

## Banco e contratos planejados

- `ingestion_job`: status, arquivo, totais, processados, válidos, inválidos, timestamps e erro resumido.
- `transaction_record`: ID crescente, job, data/hora, categoria, valor e campos opcionais definidos pelo CSV.
- `monthly_category_aggregate`: mês, categoria, soma e quantidade; chave primária composta.
- Contrato CSV canônico deve ser documentado e validado: `occurred_at,category,amount,description`.
- Índice principal de navegação: `(id)` ou `(job_id, id)` conforme endpoint. Índices de filtro devem ser validados com `EXPLAIN (ANALYZE, BUFFERS)` usando volume representativo.

## Escopo e validação

- Não alterar arquivos fora do escopo sem explicar necessidade.
- Não executar formatação global, comandos Git ou builds de `front-end`/`back-end` sem solicitação explícita.
- Após mudança TypeScript, executar verificação de tipos direcionada quando aplicável e permitida.
- Testes e lint somente quando pedido explicitamente.
- Para desempenho, não afirmar ganho sem medição. Registrar volume, hardware/limites do container, chunk size, concorrência e tempos.
- Não ler logs enormes. Use filtros, limites e trechos relevantes.
