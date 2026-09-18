# Front-end

React 19 + Vite + TypeScript 6. React Router gerencia navegação; TanStack Query gerencia estado remoto e polling; Axios encapsula API; MUI fornece componentes e tema; Tailwind CSS v4 fornece utilitários de layout.

## Rotas

- `/dashboard`: métricas, agregados e transações com paginação por cursor.
- `/ingestions/new`: upload assíncrono de CSV.
- `/ingestions/:jobId`: acompanhamento de job e estados terminais.

## Estrutura

## Dados, loading e erros

- Dashboard lista jobs paginados; detalhe abre transacoes isoladas por job.
- Datas ficam na URL como `from` e `to`, com padrao primeiro dia do mes ate hoje no fuso `America/Sao_Paulo`.

- Requisições TanStack Query ficam em `src/hooks/api`.
- Dashboard mostra jobs ativos e atualiza summary/transações durante processamento.
- Skeletons representam carregamento inicial; indicador discreto representa refetch.
- `Snackbar` + `Alert` exibem sucesso/erro com duração configurável.
- Axios normaliza `422`, `404`, `5xx`, timeout e falha de rede; logs não incluem payload sensível.

## Estrutura de diretÃ³rios

- `app`: router, providers e tema.
- `layouts`: shell compartilhado.
- `pages`: composição das rotas.
- `features`: componentes/hooks por domínio.
- `components`: componentes visuais reutilizáveis.
- `integrations/api`: cliente Axios e endpoints.
- `types`: contratos TypeScript.
- `utils`: formatação e helpers puros.

## Desenvolvimento

```bash
npm install
npm run dev
```

Para stack completa, use `docker compose up --build` na raiz. Configure `VITE_API_URL` quando API não estiver em `http://localhost:8080`.
