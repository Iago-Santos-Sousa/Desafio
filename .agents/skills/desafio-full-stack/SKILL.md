---
name: desafio-full-stack
description: Implementa e mantém o desafio de ingestão massiva deste repositório, cobrindo Spring Boot, Spring Batch, RabbitMQ, PostgreSQL, React/Vite e execução Docker. Use em tarefas de arquitetura, código, testes ou documentação dentro deste projeto.
---

# Desafio Full Stack

## Preparação

Leia, nesta ordem:

1. `/AGENTS.md` para regras globais.
2. `/TASK.md` para requisitos de produto.
3. `/PLAN.md` para decisões e fases.
4. Arquivos existentes ligados à tarefa.

Leia `/README_BACK_END.md` quando tocar execução local, containers ou configuração do back-end.

## Decisões que devem permanecer coerentes

- Stack: React + Vite + TypeScript no front-end; Java + Spring Boot no back-end; PostgreSQL e RabbitMQ.
- JPA/Hibernate atende requisito de ORM; Flyway controla migrations. TypeORM não se aplica a Java.
- Upload é gravado em stream. Resposta `202` ocorre depois do recebimento do corpo e antes da ingestão.
- RabbitMQ envia somente ID do job. Spring Batch lê arquivo incrementalmente e grava via JDBC batch em chunks.
- JPA atende metadados e consultas convencionais; não force JPA no hot path se aumentar memória ou reduzir throughput.
- Listagem usa paginação por cursor. Dashboard usa agregados pré-calculados. Progresso usa polling TanStack Query.
- Aplicação inteira deve iniciar com `docker compose up --build`, sem toolchain local.
- Versões implementadas do back-end: Java 21 LTS (Eclipse Temurin 21 no Docker) e Spring Boot 4.1.0. Alterações de versão exigem revisão da matriz de compatibilidade e atualização de `PLAN.md`.
- Qualidade Java: Spotless Maven + Google Java Format `1.35.0` e Checkstyle `14.1.0` versionados no back-end. `spotless:check` e `checkstyle:check` devem passar antes da entrega.

Mude decisão arquitetural somente com evidência concreta e atualização simultânea de `PLAN.md`, contratos e documentação afetada.

## Fluxo de trabalho

## Correções obrigatórias de feedback

## Dashboard por jobs e filtros temporais

- Endpoint de jobs usa cursor keyset e tamanho padrao 10.
- Endpoint de categorias e paginado em cinco itens e sempre recebe `jobId`.
- Analytics aceita intervalo inclusivo de datas locais; backend converte calendario `America/Sao_Paulo` e usa limite final exclusivo.
- Agregacao diaria por job garante filtros exatos sem varrer milhoes de transacoes por request.
- `/ingestions/:jobId` e pagina de detalhe; modal de transacoes usa paginacao de 25 e isolamento por job.

- Progresso de ingestão deve ser persistido após cada chunk Spring Batch; polling frontend deve consumir valores intermediários.
- Dashboard deve consultar jobs ativos e atualizar dados durante processamento.
- Validar cabeçalho CSV antes de RabbitMQ; arquivos fora do contrato devem retornar erro `422` seguro e removível.
- Encapsular todas chamadas TanStack Query em hooks customizados; UI renderiza loading, vazio, erro e refetch.
- Usar MUI `Snackbar` com `Alert` para sucesso/erro, duração configurável e fila sem mensagens duplicadas.
- Axios deve normalizar Problem Details e registrar responses sem dados sensíveis.
- Logs backend devem aparecer no console com `jobId` e `traceId`, limitando amostras de erro.

1. Classifique mudança como front-end, back-end, infraestrutura ou transversal.
2. Confirme contrato/API e invariantes afetados.
3. Consulte documentação oficial atual via Context7 quando sintaxe, compatibilidade ou versão de biblioteca importar.
4. Faça menor mudança coerente com padrões existentes.
5. Preserve limites de memória, idempotência e observabilidade da ingestão.
6. Valide somente conforme autorização e reporte comandos executados, não executados e riscos reais.

## Critérios de revisão

- Nenhuma leitura integral do CSV, `getBytes()` do multipart ou coleção acumulando todas as linhas.
- Nenhum insert individual por registro no caminho principal.
- Nenhuma paginação profunda com `OFFSET`.
- Nenhuma agregação completa da tabela grande a cada refresh do dashboard.
- Nenhum segredo em código, Compose ou arquivos versionados.
- Nenhuma entidade JPA exposta diretamente.
- Nenhum `any` novo no TypeScript sem motivo documentado.
- Polling encerra em estado terminal; componentes cobrem loading, vazio e erro.
- Migração, índice e query permanecem alinhados e verificáveis com dados de 1M+ linhas.

## Critérios adicionais de arquitetura

- React Router usa `createBrowserRouter` + `RouterProvider`; refresh de rota deve funcionar no Nginx.
- Front-end separa `pages`, `features`, `components`, `integrations/api`, `types` e `utils` por responsabilidade.
- Controllers Java não acessam `JdbcTemplate`; SQL fica em repositories e DTOs ficam fora de controllers.
- Criar `utils` somente para função pura compartilhada; não usar pacote como depósito genérico.

## Entrega

Resuma solução, arquivos alterados, validações executadas e pendências. Não declare teste, performance ou compatibilidade que não tenha sido verificada.
