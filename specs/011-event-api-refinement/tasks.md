# Tarefas: Refinamento da API de Eventos

**Entrada**: Documentos de design de `/specs/011-event-api-refinement/`
**Pré-requisitos**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`

**Organização**: As tarefas são agrupadas por História de Usuário (US) para permitir implementação e testes independentes de cada jornada.

## Formato: `[ID] [P?] [Story] Descrição`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependências)
- **[Story]**: A qual história de usuário esta tarefa pertence (ex: US1, US2, US3)

---

## Fase 1: Fundação (Pré-requisitos Bloqueantes)

**Propósito**: Infraestrutura central que DEVE estar completa antes que QUALQUER história de usuário possa ser implementada.

- [x] T001 [P] Atualizar `AuditLogService.java` para incluir `READ` e `LIST` na lista de ações auditáveis (`isAuditableAction`).
- [x] T002 [P] Garantir que o `CadastroEventoMetricsPublisher.java` suporte a métrica `publishCalendarQueryLatency` para eventos individuais.
- [x] T003 Criar `EventoFiltroRequest` em `app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/` para capturar parâmetros de consulta (start_date, end_date, organizacao_id).

**Checkpoint**: Fundação pronta - a implementação das histórias de usuário pode começar.

---

## Fase 2: História de Usuário 1 - Visualização Detalhada do Evento (Prioridade: P1) 🎯 MVP

**Objetivo**: Permitir que o usuário veja todos os detalhes de um evento específico com auditoria e métricas.

**Teste Independente**: Solicitar um ID de evento e verificar o DTO completo, o log de auditoria `READ` e a métrica de latência.

### Testes para US1

- [x] T004 [P] [US1] Criar teste de contrato para `GET /api/v1/eventos/{id}` em `app/src/test/java/br/com/nsfatima/calendario/api/v1/controller/EventoControllerGetTest.java`.
- [x] T005 [P] [US1] Criar teste de integração para fluxo de permissão RBAC no `GET` em `app/src/test/java/br/com/nsfatima/calendario/api/v1/controller/EventoControllerSecurityTest.java`.

### Implementação para US1

- [x] T006 [US1] Implementar método `findById` no `EventoService.java` com verificação de RBAC.
- [x] T007 [US1] Adicionar endpoint `GET /api/v1/eventos/{id}` no `EventoController.java`.
- [x] T008 [US1] Integrar medição de latência (`System.nanoTime()`) e publicação no `CadastroEventoMetricsPublisher` no novo endpoint.
- [x] T009 [US1] Integrar disparo de auditoria `READ` via `EventoAuditPublisher` no novo endpoint.

**Checkpoint**: US1 funcional e testável independentemente.

---

## Fase 3: História de Usuário 2 - Descoberta de Eventos por Período (Prioridade: P1)

**Objetivo**: Otimizar a listagem de eventos com paginação e filtros temporais.

**Teste Independente**: Filtrar eventos por um mês específico e verificar se o retorno é paginado e contém apenas eventos do período.

### Testes para US2

- [x] T010 [P] [US2] Criar teste de integração para listagem paginada em `app/src/test/java/br/com/nsfatima/calendario/api/v1/controller/EventoControllerListTest.java`.

### Implementação para US2

- [x] T011 [US2] Atualizar `EventoRepository.java` para suportar `Pageable` e queries filtradas por `inicioUtc` e `fimUtc`.
- [x] T012 [US2] Atualizar método `list` no `EventoService.java` para aceitar `EventoFiltroRequest` e retornar `Page<Evento>`.
- [x] T013 [US2] Refatorar endpoint `GET /api/v1/eventos` no `EventoController.java` para suportar os novos parâmetros e paginação.

**Checkpoint**: US2 funcional e integrada com a fundação.

---

## Fase 4: História de Usuário 3 - Cancelamento de Evento Resiliente (Prioridade: P2)

**Objetivo**: Fornecer um endpoint `POST` para cancelamento, evitando problemas com middlewares de rede.

**Teste Independente**: Cancelar um evento via `POST /api/v1/eventos/{id}/cancel` e verificar se o status e o motivo foram salvos.

### Testes para US3

- [x] T014 [P] [US3] Criar teste de integração para o novo endpoint de cancelamento em `app/src/test/java/br/com/nsfatima/calendario/api/v1/controller/EventoControllerCancelTest.java`.

### Implementação para US3

- [x] T015 [US3] Criar `CancelarEventoRequest.java` em `app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/`.
- [x] T016 [US3] Implementar endpoint `POST /api/v1/eventos/{id}/cancel` no `EventoController.java`.
- [x] T017 [US3] Reutilizar lógica de cancelamento existente no `EventoService.java` para o novo endpoint.

**Checkpoint**: Novo mecanismo de cancelamento operacional.

---

## Fase 5: História de Usuário 4 - Transição Transparente (Prioridade: P3)

**Objetivo**: Manter compatibilidade com o método `DELETE` enquanto sinaliza obsolescência.

### Implementação para US4

- [x] T018 [US4] Adicionar anotação `@Deprecated` ao endpoint `DELETE /api/v1/eventos/{id}` no `EventoController.java`.
- [x] T019 [US4] Adicionar cabeçalho de resposta `Warning: 299 - "This endpoint is deprecated"` no endpoint `DELETE`.

---

## Fase Final: Polimento e Validação

- [x] T020 [P] Atualizar documentação OpenAPI/Swagger com os novos endpoints e parâmetros.
- [x] T021 Validar fluxo completo usando o guia `quickstart.md`.
- [x] T022 [P] Limpeza de código e verificação de padrões de nomes.

---

## Dependências e Ordem de Execução

1. **Fase 1 (Fundação)**: Deve ser a primeira. Bloqueia todas as outras histórias.
2. **US1 e US2**: Podem ser executadas em paralelo após a Fundação.
3. **US3**: Pode ser executada em paralelo com US1/US2.
4. **US4**: Deve ser executada após US3 estar validada.
5. **Polimento**: Após todas as histórias estarem completas.
