---
name: datapulse-design-system
description: Implementa e revisa UI/UX do front-end DataPulse usando MUI, Tailwind CSS v4, Lucide React e tokens versionados. Use em mudanças de tema, componentes, páginas, responsividade, gráficos ou acessibilidade; não use para lógica de back-end.
---

# DataPulse Design System

## Preparação

Leia `AGENTS.md`, `TASK.md`, `PLAN.md`, `front-end/AGENTS.md` e `front-end/DESIGN_SYSTEM.md`. Inspecione componentes e contratos existentes antes de editar.

## Regras

- MUI controla tema, tokens, componentes e estados; Tailwind v4 controla layout e responsividade.
- Reutilize `front-end/src/components/ui` e tokens de `front-end/src/app/designTokens.ts`.
- Use Lucide com imports nomeados, labels acessíveis e `aria-hidden` em ícones decorativos.
- Preserve contratos, polling, filtros, paginação, feedback e estados de erro. Não introduza `any`.
- Consulte documentação oficial via Context7 para APIs ou versões incertas.

## Fluxo

1. Classifique mudança por componente, feature, tema ou acessibilidade.
2. Atualize tokens/componentes compartilhados antes de duplicar estilo em páginas.
3. Verifique desktop, 320px, teclado, foco, contraste e estados assíncronos.
4. Execute `npm run lint`, `npm run build` e `git diff --check` quando autorizado.
