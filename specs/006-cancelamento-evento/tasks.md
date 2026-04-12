# Tasks: Cancelamento de Evento

**Input**: Design documents from /specs/006-cancelamento-evento/
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: A feature exige cobertura de integração/contrato nas histórias e evidências mensuráveis, então tarefas de testes estão incluídas.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar o baseline técnico e o contrato da feature

- [X] T001 Consolidar o contrato de cancelamento e aprovação automática em specs/006-cancelamento-evento/contracts/calendar-api-event-cancellation.openapi.yaml
- [X] T002 Alinhar matriz de erros determinísticos da feature em app/src/main/java/br/com/nsfatima/calendario/api/error/ErrorCodes.java
- [X] T003 Atualizar mapeamentos HTTP de erro para novos códigos da feature em app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestrutura comum que bloqueia todas as histórias

**CRITICAL**: Nenhuma história começa antes desta fase

- [X] T004 Criar migration de suporte a solicitação pendente com snapshot da ação em app/src/main/resources/db/migration/V012__extend_aprovacoes_for_pending_actions.sql
- [X] T005 Atualizar o mapeamento de aprovação com campos de snapshot e decisão em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/AprovacaoEntity.java
- [X] T006 [P] Adicionar consultas para pendência/decisão de aprovação em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/AprovacaoJpaRepository.java
- [X] T007 Criar DTO de requisição de cancelamento com motivo obrigatório em app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/CancelEventoRequest.java
- [X] T008 [P] Criar DTO de resposta para cancelamento pendente em app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/CancelamentoPendenteResponse.java
- [X] T009 [P] Criar DTOs de decisão de aprovação com resultado de execução em app/src/main/java/br/com/nsfatima/calendario/api/dto/aprovacao/AprovacaoDecisionRequest.java
- [X] T010 [P] Criar DTO de resposta da decisão com outcome da ação em app/src/main/java/br/com/nsfatima/calendario/api/dto/aprovacao/AprovacaoDecisionResponse.java
- [X] T011 Implementar política de autorização de cancelamento por papel e escopo em app/src/main/java/br/com/nsfatima/calendario/domain/service/EventoCancelamentoAuthorizationService.java
- [X] T012 [P] Criar exceções específicas do fluxo pendente/execução automática em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/ApprovalExecutionFailedException.java

**Checkpoint**: Base pronta para implementar histórias em sequência de prioridade

---

## Phase 3: User Story 1 - Cancelamento Direto por Plenos Poderes (Priority: P1) MVP

**Goal**: Efetivar cancelamento imediato para pároco e liderança do conselho

**Independent Test**: DELETE com motivo em evento CONFIRMADO por pároco/conselho retorna 200 e persiste CANCELADO

### Tests for User Story 1

- [X] T013 [P] [US1] Criar teste de contrato para DELETE com efetivação imediata em app/src/test/java/br/com/nsfatima/calendario/contract/EventoMutacaoContractTest.java
- [X] T014 [P] [US1] Criar teste de integração para cancelamento direto por pároco/conselho em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoImmediateIntegrationTest.java
- [X] T015 [P] [US1] Criar teste de integração para rejeitar status diferente de CONFIRMADO em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoStatusEligibilityIntegrationTest.java

### Implementation for User Story 1

- [X] T016 [US1] Implementar caso de uso de cancelamento imediato com soft delete em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CancelEventoUseCase.java
- [X] T017 [US1] Integrar endpoint DELETE ao novo caso de uso em app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoController.java
- [X] T018 [US1] Aplicar validação do motivo obrigatório na borda HTTP em app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoController.java
- [X] T019 [US1] Persistir observação append-only de cancelamento na efetivação em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CancelEventoUseCase.java

**Checkpoint**: US1 funcional e testável isoladamente

---

## Phase 4: User Story 2 - Fluxo Pendente com Aprovação e Execução Automática (Priority: P2)

**Goal**: Criar solicitação pendente para papéis intermediários e efetivar automaticamente após aprovação

**Independent Test**: DELETE por pastoral/laicato/vigário retorna 202, PATCH da aprovação aprovada executa cancelamento sem novo DELETE

### Tests for User Story 2

- [X] T020 [P] [US2] Criar teste de integração para criação de solicitação pendente via DELETE em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoApprovalRequestIntegrationTest.java
- [X] T021 [P] [US2] Criar teste de integração para aprovação com execução automática em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ApproveCancelEventoIntegrationTest.java
- [X] T022 [P] [US2] Criar teste de integração para reprovação sem mutação do evento em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/RejectCancelEventoIntegrationTest.java
- [X] T023 [P] [US2] Criar teste de contrato para PATCH de decisão de aprovação com outcome em app/src/test/java/br/com/nsfatima/calendario/contract/AprovacoesContractTest.java

### Implementation for User Story 2

- [X] T024 [US2] Estender CancelEventoUseCase para retornar 202 e persistir snapshot pendente por papéis intermediários em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CancelEventoUseCase.java
- [X] T025 [US2] Implementar caso de uso de decisão de aprovação com execução automática em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/DecideSolicitacaoAprovacaoUseCase.java
- [X] T026 [US2] Expor endpoint PATCH de decisão no controller de aprovações em app/src/main/java/br/com/nsfatima/calendario/api/controller/AprovacaoController.java
- [X] T027 [US2] Implementar resposta discriminada do DELETE (200 `EventoCanceladoResponse` e 202 `CancelamentoPendenteResponse`) em app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/EventoCanceladoResponse.java, app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/CancelamentoPendenteResponse.java e app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoController.java
- [X] T028 [US2] Implementar proteção contra decisão duplicada/reuso de solicitação em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/DecideSolicitacaoAprovacaoUseCase.java

**Checkpoint**: US2 funcional e testável isoladamente

---

## Phase 5: User Story 3 - Rejeição por Papel e Escopo (Priority: P3)

**Goal**: Garantir FORBIDDEN para papéis não autorizados e escopo incorreto

**Independent Test**: Papéis proibidos e pastoral/laicato fora da organização responsável recebem FORBIDDEN sem mutação

### Tests for User Story 3

- [X] T029 [P] [US3] Criar teste de integração de matriz de autorização para cancelamento em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoAuthorizationIntegrationTest.java
- [X] T030 [P] [US3] Criar teste de integração para escopo organizacional de pastoral/laicato em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoOrganizationScopeIntegrationTest.java
- [X] T031 [P] [US3] Criar teste de regressão de transição inválida de lifecycle no cancelamento em app/src/test/java/br/com/nsfatima/calendario/contract/LifecycleTransitionRegressionTest.java

### Implementation for User Story 3

- [X] T032 [US3] Aplicar política de autorização no fluxo de cancelamento imediato e pendente em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CancelEventoUseCase.java
- [X] T033 [US3] Ajustar regra de aprovadores autorizados para decisão no fluxo automático em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/ValidateAprovacaoUseCase.java
- [X] T034 [US3] Normalizar respostas FORBIDDEN e INVALID_STATUS_TRANSITION no handler global em app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java

**Checkpoint**: US3 funcional e testável isoladamente

---

## Phase 6: User Story 4 - Auditoria e Rastreabilidade (Priority: P4)

**Goal**: Garantir trilha completa para solicitação, decisão, efetivação e falhas

**Independent Test**: Cada caminho do fluxo gera audit trail e observação CANCELAMENTO conforme esperado

### Tests for User Story 4

- [X] T035 [P] [US4] Criar teste de integração para auditoria de solicitação pendente e decisão em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoAuditTrailIntegrationTest.java
- [X] T036 [P] [US4] Criar teste de integração para observação CANCELAMENTO após efetivação automática em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoObservacaoIntegrationTest.java
- [X] T037 [P] [US4] Criar teste de integração para falha segura de execução pós-aprovação em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoApprovalExecutionFailureIntegrationTest.java
- [X] T038 [P] [US4] Criar teste de contrato/integrado de visibilidade por status para CANCELADO (fora do calendário público e presente no histórico interno autorizado) em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelledEventoVisibilityIntegrationTest.java
- [X] T039 [P] [US4] Criar teste de integração para garantir preservação de vínculos históricos (organizações, projetos e observações pré-existentes) após cancelamento em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoHistoricalLinksPreservationIntegrationTest.java

### Implementation for User Story 4

- [X] T040 [US4] Publicar eventos de auditoria estruturada para pending/approved/rejected/executed em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/EventoAuditPublisher.java
- [X] T041 [US4] Enriquecer métricas operacionais para cancelamento direto e pós-aprovação em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/CadastroEventoMetricsPublisher.java
- [X] T042 [US4] Garantir correlação de logs com solicitação de aprovação no fluxo de cancelamento em app/src/main/java/br/com/nsfatima/calendario/api/controller/AprovacaoController.java

**Checkpoint**: US4 funcional e testável isoladamente

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Consolidação final e validação transversal

- [X] T043 [P] Atualizar documentação de uso e exemplos da feature em specs/006-cancelamento-evento/quickstart.md
- [X] T044 [P] Revisar contrato OpenAPI final com exemplos de 200 e 202 em specs/006-cancelamento-evento/contracts/calendar-api-event-cancellation.openapi.yaml
- [X] T045 Executar bateria final focada de contratos e integrações de cancelamento em app/src/test/java/br/com/nsfatima/calendario
- [X] T046 Verificar evidências de critérios SC-001..SC-007 no plano em specs/006-cancelamento-evento/plan.md
- [X] T047 Executar medição de desempenho do cancelamento direto (Tier 1) e registrar evidência de SC-006 (<= 2s) em specs/006-cancelamento-evento/quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 (Setup): sem dependências
- Phase 2 (Foundational): depende da Phase 1 e bloqueia histórias
- Phase 3 (US1): depende da Phase 2
- Phase 4 (US2): depende da Phase 2 e integra com estrutura de US1
- Phase 5 (US3): depende da Phase 2
- Phase 6 (US4): depende de US1 e US2 para validar trilha completa
- Phase 7 (Polish): depende das histórias que entrarem no release

### User Story Dependencies

- US1 (P1): início imediato após foundation; base do MVP
- US2 (P2): depende da infraestrutura de aprovação pendente e da base de cancelamento
- US3 (P3): depende da base de autorização e fluxos de US1/US2
- US4 (P4): depende dos caminhos de execução de US1/US2 para auditar o ciclo inteiro

### Within Each User Story

- Testes primeiro (devem falhar antes da implementação)
- Caso de uso antes de controller
- Regras de domínio antes de mapeamento de erro
- Story só conclui quando passa teste independente definido na fase

### Parallel Opportunities

- T006, T008, T009, T010 e T012 podem rodar em paralelo na foundation
- Em US1, T013-T015 podem rodar em paralelo
- Em US2, T020-T023 podem rodar em paralelo
- Em US3, T029-T031 podem rodar em paralelo
- Em US4, T035-T039 podem rodar em paralelo
- T043 e T044 podem rodar em paralelo na fase final

---

## Parallel Example: User Story 1

- Task T013 [P] [US1]: teste de contrato DELETE imediato em app/src/test/java/br/com/nsfatima/calendario/contract/EventoMutacaoContractTest.java
- Task T014 [P] [US1]: integração cancelamento direto em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoImmediateIntegrationTest.java
- Task T015 [P] [US1]: integração de elegibilidade de status em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoStatusEligibilityIntegrationTest.java

## Parallel Example: User Story 2

- Task T020 [P] [US2]: integração de criação pendente via DELETE em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoApprovalRequestIntegrationTest.java
- Task T021 [P] [US2]: integração de aprovação com execução automática em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ApproveCancelEventoIntegrationTest.java
- Task T022 [P] [US2]: integração de reprovação sem mutação em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/RejectCancelEventoIntegrationTest.java
- Task T023 [P] [US2]: contrato de decisão de aprovação em app/src/test/java/br/com/nsfatima/calendario/contract/AprovacoesContractTest.java

## Parallel Example: User Story 3

- Task T029 [P] [US3]: integração da matriz de autorização em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoAuthorizationIntegrationTest.java
- Task T030 [P] [US3]: integração de escopo organizacional em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoOrganizationScopeIntegrationTest.java
- Task T031 [P] [US3]: regressão de lifecycle inválido em app/src/test/java/br/com/nsfatima/calendario/contract/LifecycleTransitionRegressionTest.java

## Parallel Example: User Story 4

- Task T035 [P] [US4]: integração de auditoria da solicitação e decisão em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoAuditTrailIntegrationTest.java
- Task T036 [P] [US4]: integração da observação CANCELAMENTO em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoObservacaoIntegrationTest.java
- Task T037 [P] [US4]: integração de falha segura pós-aprovação em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoApprovalExecutionFailureIntegrationTest.java
- Task T038 [P] [US4]: contrato/integrado de visibilidade CANCELADO em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelledEventoVisibilityIntegrationTest.java
- Task T039 [P] [US4]: integração de preservação de vínculos históricos após cancelamento em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoHistoricalLinksPreservationIntegrationTest.java

---

## Implementation Strategy

### MVP First (US1)

1. Concluir Setup
2. Concluir Foundation
3. Entregar US1 completa
4. Validar independent test da US1
5. Demonstrar cancelamento direto como MVP

### Incremental Delivery

1. Setup + Foundation
2. US1 (cancelamento direto)
3. US2 (pendência + aprovação executa automaticamente)
4. US3 (matriz completa de permissão e escopo)
5. US4 (auditoria e rastreabilidade ponta-a-ponta)
6. Polish final

### Parallel Team Strategy

1. Time fecha Phase 1 e 2 em conjunto
2. Com foundation pronta:
   - Dev A: US1
   - Dev B: US2
   - Dev C: US3
3. US4 entra quando US1 e US2 tiverem caminhos executáveis completos

---

## Notes

- [P] indica tarefas sem dependência direta entre arquivos
- [USx] liga cada tarefa à história correspondente
- Cada história foi estruturada para ser validada independentemente
- Commits devem seguir checkpoints por fase/história para facilitar rollback e revisão
