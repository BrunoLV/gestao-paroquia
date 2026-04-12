# Tasks: Execucao Automatica Pos-Aprovacao para Criacao e Edicao de Evento

**Input**: Design documents from `/specs/007-auto-approval-event-flow/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Esta feature exige cobertura de testes de contrato e integracao para comprovar execucao automatica pos-aprovacao sem reenvio manual.

**Organization**: Tarefas agrupadas por user story para implementacao e validacao independente.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar baseline de contrato, validacao operacional e estrutura de testes da feature.

- [X] T001 Consolidar baseline inicial minima do contrato (paths, responses 201/202/200 e codigos de erro obrigatorios) em specs/007-auto-approval-event-flow/contracts/calendar-api-approval-execution.openapi.yaml
- [X] T002 Definir checklist de validacao operacional (pending, approved, rejected, executed, failed) em specs/007-auto-approval-event-flow/quickstart.md
- [X] T003 [P] Criar classe base de suporte para suites de integracao da feature em app/src/test/java/br/com/nsfatima/calendario/support/ApprovalFlowTestFixtures.java
- [X] T004 [P] Criar classe base de suporte para contratos de comportamento pendente em app/src/test/java/br/com/nsfatima/calendario/contract/ApprovalPendingContractSupport.java
- [X] T005 Documentar limites arquiteturais da implementacao em specs/007-auto-approval-event-flow/plan.md

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestrutura comum que bloqueia US1, US2 e US3 ate ficar pronta.

- [X] T006 Evoluir enum de tipo de solicitacao com `CRIACAO_EVENTO` e `EDICAO_EVENTO` em app/src/main/java/br/com/nsfatima/calendario/domain/type/TipoSolicitacaoInput.java
- [X] T007 [P] Evoluir enum de resposta de tipo de solicitacao em app/src/main/java/br/com/nsfatima/calendario/domain/type/TipoSolicitacaoResponse.java
- [X] T008 Criar/atualizar migration para suportar snapshot de acao pendente e metadados de execucao em app/src/main/resources/db/migration/V015__approval_action_payload_for_create_update.sql
- [X] T009 Atualizar mapeamento da entidade de aprovacao para snapshots de criacao/edicao em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/AprovacaoEntity.java
- [X] T010 [P] Ajustar repositorio JPA para consultas de solicitacoes pendentes por tipo/estado em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/AprovacaoJpaRepository.java
- [X] T011 Implementar parser/assembler de payload imutavel de acao pendente em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/ApprovalActionPayloadMapper.java
- [X] T012 [P] Padronizar outcome operacional de decisao (`EXECUTED`, `REJECTED`, `FAILED`) em app/src/main/java/br/com/nsfatima/calendario/api/dto/aprovacao/AprovacaoDecisionResponse.java
- [X] T013 Atualizar mapeamento de erro deterministico de execucao automatica em app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java
- [X] T014 [P] Criar teste de integracao para conflito em redecisao de solicitacao (`APPROVAL_ALREADY_DECIDED`) em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ApprovalAlreadyDecidedIntegrationTest.java

**Checkpoint**: Fundacao pronta; user stories podem iniciar.

---

## Phase 3: User Story 1 - Criar Evento Com Aprovacao Assincrona (Priority: P1) 🎯 MVP

**Goal**: Permitir criacao de evento com modo pendente e execucao automatica apos aprovacao, sem novo POST do cliente.

**Independent Test**: Criar solicitacao pendente via POST, aprovar via PATCH de aprovacao e validar evento criado automaticamente.

### Tests for User Story 1

- [X] T015 [P] [US1] Criar teste de integracao para retorno `APPROVAL_PENDING` no create em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CreateEventoApprovalPendingIntegrationTest.java
- [X] T016 [P] [US1] Criar teste de integracao para execucao automatica de criacao apos `APROVADA` em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ApproveCreateEventoIntegrationTest.java
- [X] T017 [P] [US1] Criar teste de integracao para `REPROVADA` sem criacao de evento em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/RejectCreateEventoIntegrationTest.java
- [X] T018 [P] [US1] Criar teste de contrato para resposta de create pendente em app/src/test/java/br/com/nsfatima/calendario/contract/EventosCreatePendingContractTest.java
- [X] T019 [P] [US1] Criar teste de regressao para criacao imediata com perfil autorizado (compatibilidade FR-015) em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CreateEventoImmediateCompatibilityIntegrationTest.java
- [X] T020 [P] [US1] Criar teste de idempotencia da criacao automatica pos-aprovacao sem duplicidade em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ApproveCreateEventoIdempotencyIntegrationTest.java
- [X] T021 [P] [US1] Criar teste de imutabilidade do snapshot de criacao pendente (sem alteracao apos criacao) em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CreateEventoApprovalSnapshotImmutabilityIntegrationTest.java
- [X] T022 [P] [US1] Criar teste de auditoria no caminho `REPROVADA` para criacao pendente em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/RejectCreateEventoAuditTrailIntegrationTest.java

### Implementation for User Story 1

- [X] T023 [US1] Evoluir fluxo de criacao para decidir entre execucao imediata e solicitacao pendente em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CreateEventoUseCase.java
- [X] T024 [US1] Introduzir DTO de resposta pendente de aprovacao para create em app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/EventoApprovalPendingResponse.java
- [X] T025 [US1] Ajustar controller de evento para retornar `201` ou `202` no create conforme fluxo em app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoController.java
- [X] T026 [US1] Persistir snapshot imutavel de payload de criacao pendente em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/CreateEventoApprovalRequestUseCase.java
- [X] T027 [US1] Estender decisao de aprovacao para executar automaticamente criacao pendente em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/DecideSolicitacaoAprovacaoUseCase.java
- [X] T028 [US1] Reutilizar idempotencia no caminho de execucao automatica de criacao em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/EventoIdempotencyService.java
- [X] T029 [US1] Publicar auditoria especifica do fluxo de criacao pendente (pending/execute/reject/fail) em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/EventoAuditPublisher.java
- [X] T030 [US1] Publicar metricas de fluxo de criacao pendente e execucao pos-aprovacao em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/CadastroEventoMetricsPublisher.java

**Checkpoint**: US1 funcional e testavel de forma independente.

---

## Phase 4: User Story 2 - Editar Evento Com Aplicacao Automatica (Priority: P1)

**Goal**: Permitir edicao sensivel com solicitacao unica e aplicacao automatica apos aprovacao, sem novo PATCH do cliente.

**Independent Test**: Enviar PATCH sensivel em modo pendente, aprovar e validar alteracao aplicada automaticamente.

### Tests for User Story 2

- [X] T031 [P] [US2] Criar teste de integracao para PATCH sensivel retornando `APPROVAL_PENDING` em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoApprovalPendingIntegrationTest.java
- [X] T032 [P] [US2] Criar teste de integracao para execucao automatica de edicao apos `APROVADA` em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ApproveUpdateEventoIntegrationTest.java
- [X] T033 [P] [US2] Criar teste de integracao para `REPROVADA` sem alterar evento em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/RejectUpdateEventoIntegrationTest.java
- [X] T034 [P] [US2] Criar teste de integracao para falha segura de execucao pos-aprovacao em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoApprovalExecutionFailureIntegrationTest.java
- [X] T035 [P] [US2] Criar teste de contrato para resposta de patch pendente em app/src/test/java/br/com/nsfatima/calendario/contract/EventosPatchPendingContractTest.java
- [X] T036 [P] [US2] Criar teste de regressao para patch imediato com perfil autorizado (compatibilidade FR-015) em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoImmediateCompatibilityIntegrationTest.java
- [X] T037 [P] [US2] Criar teste de negacao de decisao por papel/escopo invalido em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ApprovalDecisionAuthorizationIntegrationTest.java
- [X] T038 [P] [US2] Criar teste de imutabilidade do snapshot de edicao pendente (sem alteracao apos criacao) em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoApprovalSnapshotImmutabilityIntegrationTest.java
- [X] T039 [P] [US2] Criar teste de auditoria no caminho `REPROVADA` para edicao pendente em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/RejectUpdateEventoAuditTrailIntegrationTest.java

### Implementation for User Story 2

- [X] T040 [US2] Evoluir requisicao de patch para suportar caminho pendente sem `aprovacaoId` obrigatorio em app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/UpdateEventoRequest.java
- [X] T041 [US2] Evoluir use case de patch para criar solicitacao pendente quando campo sensivel exigir autorizacao em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java
- [X] T042 [US2] Persistir snapshot imutavel de payload de edicao pendente em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/UpdateEventoApprovalRequestUseCase.java
- [X] T043 [US2] Estender decisao de aprovacao para aplicar automaticamente edicao pendente em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/DecideSolicitacaoAprovacaoUseCase.java
- [X] T044 [US2] Reaplicar validacoes de dominio e autorizacao no caminho de execucao automatica de edicao em app/src/main/java/br/com/nsfatima/calendario/domain/service/EventoDomainService.java
- [X] T045 [US2] Ajustar controller para retorno `200` ou `202` no patch conforme resultado do fluxo em app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoController.java
- [X] T046 [US2] Garantir consistencia transacional e ausencia de mutacao parcial em falha de execucao automatica em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/DecideSolicitacaoAprovacaoUseCase.java
- [X] T047 [US2] Implementar verificacao explicita de autorizacao da decisao por papel e escopo organizacional no fluxo de aprovacao em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/DecideSolicitacaoAprovacaoUseCase.java

**Checkpoint**: US1 e US2 funcionais e independentes.

---

## Phase 5: User Story 3 - Operacao Segura e Rastreavel (Priority: P2)

**Goal**: Garantir auditoria, metricas e diagnostico deterministico para todo o workflow assincorno.

**Independent Test**: Simular caminhos de sucesso e falha para criacao/edicao e validar trilha operacional completa.

### Tests for User Story 3

- [X] T048 [P] [US3] Criar teste de integracao para trilha de auditoria ponta a ponta da criacao pendente em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CreateEventoApprovalAuditTrailIntegrationTest.java
- [X] T049 [P] [US3] Criar teste de integracao para trilha de auditoria ponta a ponta da edicao pendente em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoApprovalAuditTrailIntegrationTest.java
- [X] T050 [P] [US3] Criar teste de integracao para erro deterministico `APPROVAL_EXECUTION_FAILED` com estado consistente cobrindo criacao e edicao em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ApprovalAutoExecutionFailureConsistencyIntegrationTest.java
- [X] T051 [P] [US3] Criar teste de contrato para payload de resultado de decisao com `actionExecution` em app/src/test/java/br/com/nsfatima/calendario/contract/AprovacoesDecisionExecutionContractTest.java

### Implementation for User Story 3

- [X] T052 [US3] Enriquecer auditoria transversal com correlation id e metadados comuns para criacao/edicao pendente (sem duplicar eventos especificos da US1) em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/EventoAuditPublisher.java
- [X] T053 [US3] Instrumentar metricas de throughput, sucesso/falha e tempo entre decisao e execucao em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/CadastroEventoMetricsPublisher.java
- [X] T054 [US3] Ajustar mapeamento global de excecoes para erros deterministicos de workflow assincorno em app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java
- [X] T055 [US3] Consolidar contrato OpenAPI final apos implementacao/testes (schemas finais de `actionExecution`, matriz de erro fechada e regra de compatibilidade de `APPROVAL_REQUIRED`) em specs/007-auto-approval-event-flow/contracts/calendar-api-approval-execution.openapi.yaml
- [X] T056 [US3] Validar instrumentacao da baseline constitucional (`event_registration_lead_time_minutes`, `calendar_query_latency_ms`, `administrative_rework_indicator`) em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/CadastroEventoMetricsPublisher.java

**Checkpoint**: Todas as user stories funcionais e testaveis de forma independente.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Consolidar evidencias, regressao final e documentacao de operacao.

- [X] T057 [P] Atualizar quickstart com comandos finais executados e evidencias em specs/007-auto-approval-event-flow/quickstart.md
- [X] T058 [P] Atualizar plano com snapshot de evidencias SC-001..SC-005 em specs/007-auto-approval-event-flow/plan.md
- [X] T059 Executar bateria final de regressao da feature em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ApprovalFlowRegressionSuite.java
- [X] T060 [P] Revisar tasks/checklist de qualidade para fechamento da feature em specs/007-auto-approval-event-flow/checklists/requirements.md
- [X] T061 [P] Medir e registrar evidencia do SC-003 (p95 <= 60s entre decisao e execucao) em specs/007-auto-approval-event-flow/plan.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: sem dependencias.
- **Phase 2 (Foundational)**: depende de Phase 1; bloqueia inicio das user stories.
- **Phase 3 (US1)**: depende de Phase 2.
- **Phase 4 (US2)**: depende de Phase 2; pode ocorrer em paralelo com US1 se a equipe separar os arquivos.
- **Phase 5 (US3)**: depende da base de US1 e US2 para validar trilha completa.
- **Phase 6 (Polish)**: depende das user stories concluídas.

### User Story Dependencies

- **US1 (P1)**: inicia apos Fundacao; entrega MVP de criacao automatica pos-aprovacao.
- **US2 (P1)**: inicia apos Fundacao; depende de utilitarios compartilhados de snapshot/execucao definidos na Fundacao.
- **US3 (P2)**: depende de US1 e US2 para validar observabilidade e falha segura ponta a ponta.

### Within Each User Story

- Testes devem ser implementados primeiro e falhar antes da implementacao.
- Ajustes de DTO/contrato precedem orquestracao de use case.
- Orquestracao de use case precede auditoria/metrica final.

### Parallel Opportunities

- Tarefas [P] na Setup e Fundacao podem rodar em paralelo.
- Testes [P] dentro de cada US podem rodar em paralelo.
- US1 e US2 podem ser desenvolvidas em paralelo apos Fundacao.
- Ajustes de contrato/documentacao [P] podem ocorrer em paralelo com implementacao de codigo quando nao houver conflito de arquivo.

---

## Parallel Example: User Story 1

```bash
# Testes paralelos US1
T015 CreateEventoApprovalPendingIntegrationTest
T016 ApproveCreateEventoIntegrationTest
T017 RejectCreateEventoIntegrationTest
T018 EventosCreatePendingContractTest

# Implementacoes paralelas US1 sem conflito direto
T024 DTO de resposta pendente
T029 Auditoria de fluxo de criacao pendente
T030 Metricas de criacao pendente
```

---

## Parallel Example: User Story 2

```bash
# Testes paralelos US2
T031 UpdateEventoApprovalPendingIntegrationTest
T032 ApproveUpdateEventoIntegrationTest
T033 RejectUpdateEventoIntegrationTest
T035 EventosPatchPendingContractTest

# Implementacoes paralelas US2 sem conflito direto
T040 DTO/Request de patch pendente
T044 Validacoes de dominio
T045 Ajuste de controller
```

---

## Implementation Strategy

### MVP First (US1)

1. Concluir Phase 1 e Phase 2.
2. Implementar integralmente US1.
3. Validar criterios independentes da US1.
4. Demonstrar criacao pendente + aprovacao com execucao automatica sem reenvio.

### Incremental Delivery

1. Base (Setup + Fundacao).
2. Entrega US1 (criacao).
3. Entrega US2 (edicao).
4. Entrega US3 (observabilidade e robustez operacional).
5. Polish final com regressao completa.

### Parallel Team Strategy

1. Equipe A: fluxo de criacao (US1).
2. Equipe B: fluxo de edicao (US2).
3. Equipe C: auditoria/metrica e contratos finais (US3 + polish).

---

## Notes

- Todas as tarefas seguem formato checklist obrigatorio com ID sequencial e caminho de arquivo.
- [P] indica paralelizacao segura quando nao houver dependencia de tarefa incompleta no mesmo arquivo.
- [US#] identifica rastreabilidade direta por user story.
- Evitar introduzir dependencias externas sem justificativa constitucional.
