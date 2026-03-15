# Tasks: Criacao Completa de Evento

**Input**: Design documents from `/specs/003-complete-event-creation/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Testes de contrato/integracao estao incluidos porque a especificacao exige evidencias executaveis por user story.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar baseline tecnico e contrato da feature

- [X] T001 Revisar e alinhar escopo tecnico da feature em `specs/003-complete-event-creation/plan.md`
- [X] T002 Consolidar delta de contrato da feature em `specs/003-complete-event-creation/contracts/calendar-api-complete-event-create.openapi.yaml`
- [X] T003 [P] Atualizar cenarios de validacao operacional em `specs/003-complete-event-creation/quickstart.md`
- [X] T004 [P] Definir matriz de erros deterministas da feature em `app/src/main/java/br/com/nsfatima/calendario/api/error/ErrorCodes.java`
- [X] T005 Definir fronteiras de implementacao (api/application/domain/infrastructure) para o fluxo de create/list em `specs/003-complete-event-creation/plan.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Base tecnica obrigatoria para qualquer user story

**CRITICAL**: Nenhuma tarefa de user story deve iniciar antes desta fase

- [X] T006 Criar migration de persistencia de idempotencia em `app/src/main/resources/db/migration/V008__create_evento_idempotency.sql`
- [X] T007 [P] Criar entidade de idempotencia em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/EventoIdempotencyEntity.java`
- [X] T008 [P] Criar repositorio JPA de idempotencia em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/EventoIdempotencyJpaRepository.java`
- [X] T009 Criar servico de aplicacao para idempotencia de create em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/EventoIdempotencyService.java`
- [X] T010 Ajustar autenticacao obrigatoria de `GET /api/v1/eventos` em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/SecurityConfig.java`
- [X] T011 Definir mapeamento de erro para conflito de idempotencia em `app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java`
- [X] T012 Ajustar repositorio de eventos para ordenacao deterministica de listagem em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/EventoJpaRepository.java`
- [X] T013 Garantir fronteira transacional para create atomico (evento + idempotencia) em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CreateEventoUseCase.java`

**Checkpoint**: Foundation ready - implementacao por user story pode iniciar

---

## Phase 3: User Story 1 - Cadastrar Evento Completo (Priority: P1) MVP

**Goal**: Criar evento completo com persistencia real, listagem sem mock e idempotencia

**Independent Test**: Enviar `POST /api/v1/eventos` com payload completo + `Idempotency-Key`, validar `201`, replay idempotente e presenca em `GET /api/v1/eventos` autenticado

### Tests for User Story 1

- [X] T014 [P] [US1] Criar teste de contrato para create completo em `app/src/test/java/br/com/nsfatima/calendario/contract/EventosContractTest.java`
- [X] T015 [P] [US1] Criar teste de integracao para persistencia e listagem real em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CreateEventoPersistenciaIntegrationTest.java`
- [X] T016 [P] [US1] Criar teste de integracao para replay idempotente em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/EventoIdempotencyIntegrationTest.java`
- [X] T046 [P] [US1] Criar teste de integracao para compatibilidade de leitura legada (FR-013) em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/LegacyEventoReadCompatibilityIntegrationTest.java`

### Implementation for User Story 1

- [X] T017 [US1] Expandir payload de create para evento completo em `app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/CreateEventoRequest.java`
- [X] T018 [P] [US1] Ajustar DTO de resposta para refletir dados persistidos de create/list em `app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/EventoResponse.java`
- [X] T019 [US1] Implementar persistencia real no create em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CreateEventoUseCase.java`
- [X] T020 [US1] Implementar fluxo de idempotencia no create em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/EventoIdempotencyService.java`
- [X] T021 [US1] Substituir listagem mockada por listagem persistida em `app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoController.java`
- [X] T022 [US1] Implementar caso de uso de listagem autenticada em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/ListEventosUseCase.java`
- [X] T023 [US1] Implementar mapeamento entity->DTO para create/list em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/mapper/EventoMapper.java`
- [X] T024 [US1] Ajustar entidade de evento para campos completos do create em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/EventoEntity.java`
- [X] T047 [US1] Garantir compatibilidade de leitura para registros legados no caso de uso/listagem em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/ListEventosUseCase.java`

**Checkpoint**: US1 funcional e testavel de forma independente

---

## Phase 4: User Story 2 - Aplicar Regras de Dominio na Criacao (Priority: P2)

**Goal**: Aplicar validacoes de negocio no create com conflito nao bloqueante e erros deterministas

**Independent Test**: Validar rejeicao de intervalos invalidos, rejeicao de `ADICIONADO_EXTRA` sem justificativa e persistencia com `CONFLICT_PENDING` em sobreposicao

### Tests for User Story 2

- [X] T025 [P] [US2] Criar teste de integracao para validacoes de create em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CreateEventoValidationIntegrationTest.java`
- [X] T026 [P] [US2] Criar teste de integracao para conflito nao bloqueante no create em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CreateEventoConflitoPendingIntegrationTest.java`
- [X] T027 [P] [US2] Atualizar teste de rejeicao de campos desconhecidos em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UnknownFieldRejectionIntegrationTest.java`
- [X] T048 [P] [US2] Criar teste de integracao para integridade entre organizacao responsavel e participantes (FR-015) em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/EventoOrganizacaoParticipantesIntegrityIntegrationTest.java`

### Implementation for User Story 2

- [X] T028 [US2] Evoluir validacoes de create no dominio em `app/src/main/java/br/com/nsfatima/calendario/domain/service/EventoDomainService.java`
- [X] T029 [US2] Implementar regra de conflito nao bloqueante no calendario em `app/src/main/java/br/com/nsfatima/calendario/domain/policy/CalendarIntegrityPolicy.java`
- [X] T030 [US2] Persistir marcador/razao de conflito pendente em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/EventoEntity.java`
- [X] T031 [US2] Criar migration para campos de conflito pendente em `app/src/main/resources/db/migration/V009__add_evento_conflict_pending_fields.sql`
- [X] T032 [US2] Reforcar rejeicao de propriedades desconhecidas no binding JSON em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/config/JacksonConfig.java`
- [X] T033 [US2] Garantir codigos de erro de validacao/regra de negocio no handler global em `app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java`
- [X] T049 [US2] Implementar validacao de integridade organizacao responsavel x participantes no dominio em `app/src/main/java/br/com/nsfatima/calendario/domain/service/EventoDomainService.java`

**Checkpoint**: US1 e US2 funcionais e testaveis independentemente

---

## Phase 5: User Story 3 - Rastrear Operacao de Criacao (Priority: P3)

**Goal**: Garantir trilha auditavel de sucesso/falha para create/list com correlacao operacional

**Independent Test**: Executar create valido e invalido e comprovar auditoria estruturada com resultado e contexto

### Tests for User Story 3

- [X] T034 [P] [US3] Criar teste de integracao para auditoria de sucesso/falha no create em `app/src/test/java/br/com/nsfatima/calendario/integration/observability/EventoCreateAuditIntegrationTest.java`
- [X] T035 [P] [US3] Estender teste de seguranca para auditoria de escrita negada em `app/src/test/java/br/com/nsfatima/calendario/integration/security/DeniedWriteAuditIntegrationTest.java`

### Implementation for User Story 3

- [X] T036 [US3] Publicar auditoria estruturada de create/list em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/EventoAuditPublisher.java`
- [X] T037 [US3] Incluir correlacao e categoria de erro no log operacional em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/AuditLogService.java`
- [X] T038 [US3] Propagar correlation-id e metadados de idempotencia no pipeline de request em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/CorrelationIdFilter.java`
- [X] T039 [US3] Instrumentar metricas de create (success/failure/conflict_pending/replay) em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/CadastroEventoMetricsPublisher.java`
- [X] T040 [US3] Atualizar snapshot semanal para incluir metricas do novo fluxo em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/WeeklyMetricsSnapshotJob.java`

**Checkpoint**: Todas as user stories funcionais e auditaveis

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalizacao de qualidade, documentacao e evidencia de entrega

- [X] T041 [P] Atualizar exemplos finais de contrato em `specs/003-complete-event-creation/contracts/calendar-api-complete-event-create.openapi.yaml`
- [X] T042 [P] Atualizar roteiro final de validacao em `specs/003-complete-event-creation/quickstart.md`
- [X] T043 [P] Documentar nota de compatibilidade do payload completo em `specs/003-complete-event-creation/compatibility-notice.md`
- [X] T044 Executar suites de regressao da feature e registrar evidencias em `specs/003-complete-event-creation/implementation-evidence.md`
- [X] T045 Validar aderencia constitucional e checklist de requisitos em `specs/003-complete-event-creation/checklists/requirements.md`
- [X] T050 [P] Garantir cobertura constitucional de visibilidade por status com testes dedicados em `app/src/test/java/br/com/nsfatima/calendario/contract/PublicVisibilityContractTest.java` e `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/PublicStatusVisibilityIntegrationTest.java`
- [X] T051 [P] Capturar baseline e pos-implementacao de SC-003 (tempo de cadastro) em `specs/003-complete-event-creation/implementation-evidence.md`
- [X] T052 [P] Capturar baseline e pos-implementacao de SC-004 (p95 de listagem <= 2s) em `specs/003-complete-event-creation/implementation-evidence.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: inicia imediatamente
- **Phase 2 (Foundational)**: depende da Phase 1 e bloqueia todas as user stories
- **Phase 3 (US1)**: depende da Phase 2
- **Phase 4 (US2)**: depende da Phase 2 e integra sobre fluxo base entregue em US1
- **Phase 5 (US3)**: depende da Phase 2 e da existencia do fluxo de create/list entregue em US1
- **Phase 6 (Polish)**: depende das user stories selecionadas completas

### User Story Dependencies

- **US1 (P1)**: sem dependencia de outras stories (MVP)
- **US2 (P2)**: depende do fluxo base de create/list da US1
- **US3 (P3)**: depende do create/list da US1; pode evoluir em paralelo com US2 apos foundation + baseline US1

### Within Each User Story

- Testes devem ser criados e falhar antes da implementacao
- DTO/modelo/mapeamento antes de orquestracao de use case
- Use case antes de controller
- Controller antes da validacao final de story

### Parallel Opportunities

- Setup paralelo: T003, T004
- Foundational paralelo: T007, T008
- US1 paralelo: T014, T015, T016, T018 e T046
- US2 paralelo: T025, T026, T027 e T048
- US3 paralelo: T034, T035
- Polish paralelo: T041, T042, T043, T050, T051 e T052

---

## Parallel Example: User Story 1

```bash
# Testes de US1 em paralelo:
Task: "T014 [US1] Contrato de create completo"
Task: "T015 [US1] Integracao de persistencia/listagem"
Task: "T016 [US1] Integracao de idempotencia"
Task: "T046 [US1] Compatibilidade de leitura legada"

# Modelagem inicial de US1 em paralelo:
Task: "T017 [US1] Expandir CreateEventoRequest"
Task: "T018 [US1] Ajustar EventoResponse"
```

## Parallel Example: User Story 2

```bash
# Testes de regras de dominio em paralelo:
Task: "T025 [US2] Validacoes de create"
Task: "T026 [US2] Conflito nao bloqueante"
Task: "T027 [US2] Rejeicao de campos desconhecidos"
Task: "T048 [US2] Integridade organizacao x participantes"
```

## Parallel Example: User Story 3

```bash
# Testes de auditoria em paralelo:
Task: "T034 [US3] Auditoria de create sucesso/falha"
Task: "T035 [US3] Auditoria de escrita negada"
```

---

## Implementation Strategy

### MVP First (US1)

1. Concluir Setup (Phase 1)
2. Concluir Foundational (Phase 2)
3. Entregar US1 (Phase 3)
4. Validar independentemente o fluxo completo de create/list

### Incremental Delivery

1. Setup + Foundational
2. US1 (MVP operacional)
3. US2 (regras de dominio no create)
4. US3 (observabilidade/auditoria completa)
5. Polish e evidencias finais

### Parallel Team Strategy

1. Time inteiro fecha Phase 1 e Phase 2
2. Apos foundation:
   - Dev A: US1 (create/list persistido + idempotencia)
   - Dev B: US2 (regras e conflitos)
   - Dev C: US3 (observabilidade)
3. Consolidar evidencias na Phase 6

---

## Notes

- [P] indica tarefas sem conflito de arquivo/dependencia direta
- Labels [US1]/[US2]/[US3] garantem rastreabilidade por historia
- Cada story deve permanecer validavel isoladamente
- Commits recomendados por bloco logico de tarefas
- Evitar acoplamento cruzado entre stories fora das dependencias declaradas
