# Execução local do back-end

Este documento descreve execução local do back-end implementado. Todo build ocorre via Docker; comandos não exigem Java ou Maven no host.

## Pré-requisitos

- Docker Engine/Desktop com Docker Compose v2.
- Portas livres: `8080` para API, `5432` para PostgreSQL e `15672` para painel RabbitMQ.
- Java e Maven locais não são necessários. Build ocorre em container multi-stage.

## Configuração

Na raiz do repositório, copie `.env.example` para `.env` e ajuste somente quando necessário. Valores esperados:

```dotenv
POSTGRES_DB=large_data
POSTGRES_USER=app
POSTGRES_PASSWORD=change-me-local
RABBITMQ_DEFAULT_USER=app
RABBITMQ_DEFAULT_PASS=change-me-local
BACKEND_PORT=8080
APP_BUSINESS_TIME_ZONE=America/Sao_Paulo
```

`.env` contém credenciais locais e não deve ser versionado. `.env.example` terá valores seguros de exemplo.

## Subir somente dependências e back-end

Execute na raiz:

```bash
docker compose up --build -d postgres rabbitmq back-end
```

Verifique containers:

```bash
docker compose ps
```

Veja logs recentes sem carregar saída inteira:

```bash
docker compose logs --tail=150 -f back-end
```

API ficará disponível em `http://localhost:8080`. Swagger UI ficará em `http://localhost:8080/swagger-ui.html`; especificação OpenAPI, em `http://localhost:8080/v3/api-docs`.

Health check:

```bash
curl http://localhost:8080/actuator/health
```

## Fluxo mínimo

Enviar CSV:

```bash
curl -i -F "file=@./data/transactions-1m.csv" http://localhost:8080/api/v1/ingestions
```

Resposta esperada: `202 Accepted`, `jobId`, status inicial e URL de acompanhamento. Recebimento do arquivo já terminou; processamento continua em background.

Consultar progresso:

```bash
curl http://localhost:8080/api/v1/ingestions/SEU_JOB_ID
```

Listar transações com cursor:

```bash
curl "http://localhost:8080/api/v1/transactions?size=50"
```

Consultar agregação mensal:

```bash
curl "http://localhost:8080/api/v1/analytics/monthly-by-category"
```

## IntelliJ IDEA: formatação e lint

O repositório já versiona a regra em `back-end/config/checkstyle/checkstyle.xml`. IntelliJ fornece feedback local; Maven e Docker continuam sendo a fonte de verdade para a validação.

### 1. Abrir e configurar Java 21

1. Abra a raiz `Desafio_Full_Stack` no IntelliJ e marque o projeto como confiável.
2. Acesse `File > Project Structure > Project` e selecione Java 21 como `Project SDK`.
3. Acesse `Settings > Build, Execution, Deployment > Build Tools > Maven` e selecione Java 21 para o Maven importer.
4. Recarregue o projeto Maven pelo painel `Maven`.

### 2. Instalar plugins

Em `Settings > Plugins > Marketplace`, instale e reinicie o IntelliJ:

- `CheckStyle-IDEA`.
- `google-java-format`, compatível com formatter `1.35.0`.

### 3. Apontar CheckStyle-IDEA para regra versionada

1. Acesse `Settings > Tools > Checkstyle`.
2. Clique em `+` e escolha `Use a local Checkstyle file`.
3. Preencha a descrição com `DataPulse Checkstyle`.
4. Selecione o caminho `$PROJECT_DIR$/back-end/config/checkstyle/checkstyle.xml`.
5. Se o plugin solicitar a versão do engine, selecione `14.1.0`, igual à dependência do Maven.
6. Selecione escopo de fontes Java incluindo testes.
7. Marque a configuração como ativa e confirme em `Apply`/`OK`.
8. Abra a janela `Checkstyle` e execute `Scan` para verificar o projeto.

Se `back-end` for aberto como projeto separado, use `$PROJECT_DIR$/config/checkstyle/checkstyle.xml`. Não escolha uma regra Google/Sun bundled: ela não substitui a configuração do repositório.

### 4. Ativar Google Java Format

1. Acesse `Settings > Other Settings > google-java-format Settings`.
2. Marque `Enable google-java-format`.
3. Se o plugin solicitar acesso aos módulos internos do compilador, abra `Help > Edit Custom VM Options`, adicione as linhas abaixo e reinicie:

```text
--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
```

O plugin substitui as ações `Code > Reformat Code` e `Code > Optimize Imports`. Se desejar formatação ao salvar, habilite essas ações em `Actions on Save`. Não misture com outro formatter Java nativo.

### 5. Validar localmente

No terminal, a partir da raiz do repositório:

```bash
cd back-end
mvn spotless:apply
mvn spotless:check
mvn checkstyle:check
mvn -DskipTests verify
```

Sem Maven local, use container Java 21 para aplicar formato:

```powershell
docker run --rm -v "${PWD}\back-end:/workspace" -w /workspace maven:3.9.11-eclipse-temurin-21 mvn -B spotless:apply
```

Contrato CSV:

```csv
occurred_at,category,amount,description
2026-01-15T10:30:00Z,food,42.90,Lunch
```

## Gerar massa de teste

Gerador planejado será executado em container, sem Node local:

```bash
docker compose --profile tools run --rm csv-generator 1000000 /data/transactions-1m.csv
```

Arquivo ficará em `./data`. Não versione CSV grande.

## Parar ambiente

Preservar volume do PostgreSQL:

```bash
docker compose stop back-end rabbitmq postgres
```

Remover containers, mantendo volumes:

```bash
docker compose down
```

`docker compose down -v` apaga banco local e dados de forma irreversível; use somente quando reset total for intencional.

## Versões do back-end

- Java 21 LTS (Eclipse Temurin 21 no build e no runtime Docker).
- Spring Boot 4.1.0, definido no parent de `back-end/pom.xml`.
- A propriedade Maven `<java.version>` está fixada em `21`.

## Estado atual

Back-end está implementado e executa em container Spring Boot; host precisa somente de Docker Compose.

## Problemas comuns

- Porta ocupada: altere mapeamento via variável suportada ou encerre processo conflitante.
- API não sobe: confira health de PostgreSQL/RabbitMQ e últimos 150 logs do back-end.
- Job parado em `QUEUED`: confira consumer RabbitMQ e volume de uploads montado no back-end.
- Upload rejeitado: confira limite configurado, cabeçalho CSV e encoding UTF-8.
- Swagger incompatível: confirme versão de `springdoc-openapi` suportada pela versão fixada do Spring Boot.
# Observabilidade e CSV

## Dashboard por jobs e datas

- `GET /api/v1/ingestions?size=10&cursor=` lista jobs com cursor keyset.
- `GET /api/v1/transactions/categories?jobId=&search=&size=5&cursor=` lista categorias do job.
- Analytics aceita `from` e `to` inclusivos em `YYYY-MM-DD`; padrao usa `America/Sao_Paulo`.
- Limite de `to` equivale ao proximo dia `00:00` exclusivo, evitando perda do ultimo dia.

API valida cabeçalho `occurred_at,category,amount,description` antes de criar job. CSV incompatível retorna `422 CSV_HEADER_INVALID`.

Progresso é persistido por chunk Spring Batch e consultado por `GET /api/v1/ingestions/{jobId}`. Dashboard usa `GET /api/v1/ingestions/active` para acompanhar jobs em andamento.

Erros são exibidos no console via SLF4J com `jobId` e `traceId`. Respostas usam Problem Details. Containers possuem nomes `datapulse-postgres`, `datapulse-rabbitmq` e `datapulse-api`.
