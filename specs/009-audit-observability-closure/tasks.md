# Tasks: Fechamento de Auditoria e Retrabalho

**Input**: Design documents from `/specs/009-audit-observability-closure/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: A feature exige cobertura de contrato, integração, segurança, falha determinística e evidências de performance, então as tarefas de teste estão incluídas.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Consolidar contrato, matriz de erros e documentação-base da feature

- [X] T001 Consolidar o contrato OpenAPI inicial da feature em specs/009-audit-observability-closure/contracts/calendar-api-audit-observability.openapi.yaml
- [X] T002 Alinhar a matriz de erros determinísticos para trilha auditável e retrabalho em app/src/main/java/br/com/nsfatima/calendario/api/error/ErrorCodes.java
- [X] T003 Atualizar o mapeamento HTTP dos novos erros da feature em app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java
- [X] T004 Documentar os comandos e critérios de validação final da feature em specs/009-audit-observability-closure/quickstart.md

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Criar a base comum de persistência, consulta e métricas que bloqueia todas as histórias

**CRITICAL**: Nenhuma história começa antes desta fase

- [X] T005 Criar migration para persistência imutável da trilha auditável em app/src/main/resources/db/migration/V017__create_auditoria_operacao.sql
- [X] T006 [P] Criar entidade JPA da trilha auditável em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/AuditoriaOperacaoEntity.java
- [X] T007 [P] Criar repositório JPA da trilha auditável em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/AuditoriaOperacaoJpaRepository.java
- [X] T008 [P] Criar DTO de resposta da trilha auditável em app/src/main/java/br/com/nsfatima/calendario/api/dto/metrics/AuditoriaOperacaoResponse.java
- [X] T009 [P] Criar DTO de resposta da taxa de retrabalho com numerador e denominador em app/src/main/java/br/com/nsfatima/calendario/api/dto/metrics/IndicadorRetrabalhoResponse.java
- [X] T010 Implementar validador de período operacional com granularidade ou `inicio/fim` em app/src/main/java/br/com/nsfatima/calendario/domain/policy/PeriodoOperacionalPolicy.java
- [X] T011 [P] Criar exceções específicas de consulta operacional em app/src/main/java/br/com/nsfatima/calendario/application/usecase/metrics/PeriodoOperacionalInvalidoException.java e app/src/main/java/br/com/nsfatima/calendario/application/usecase/metrics/PersistenciaAuditoriaObrigatoriaException.java
- [X] T012 Adaptar o serviço de auditoria para persistência estruturada com `correlationId` em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/AuditLogService.java
- [X] T013 Definir gravação auditável transacional fail-closed para mutações cobertas em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/EventoAuditPublisher.java e app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/ObservacaoAuditPublisher.java
- [X] T014 [P] Preparar consultas base para numerador de retrabalho e eventos afetados em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/AuditoriaOperacaoJpaRepository.java e app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/EventoJpaRepository.java
- [X] T015 [P] Criar teste de infraestrutura da trilha auditável persistida em app/src/test/java/br/com/nsfatima/calendario/integration/foundation/AuditoriaInfrastructureIntegrationTest.java
- [X] T016 [P] Criar teste de falha determinística para período ambíguo em app/src/test/java/br/com/nsfatima/calendario/integration/foundation/PeriodoOperacionalValidationIntegrationTest.java

**Checkpoint**: Foundation pronta para implementar histórias em ordem de prioridade

---

## Phase 3: User Story 1 - Consultar trilha auditável (Priority: P1) 🎯 MVP

**Goal**: Expor consulta auditável por período e organização com ordenação determinística e escopo autorizado

**Independent Test**: Executar mutações reais de evento e observação, consultar `GET /api/v1/auditoria/eventos/trilha` com `organizacaoId` e período válidos e validar registros persistidos, ordenados e sem vazamento entre organizações

### Tests for User Story 1

- [X] T017 [P] [US1] Criar teste de contrato para `GET /api/v1/auditoria/eventos/trilha` em app/src/test/java/br/com/nsfatima/calendario/contract/AuditoriaEventosContractTest.java
- [X] T018 [P] [US1] Criar teste de integração para consulta da trilha auditável por organização e período em app/src/test/java/br/com/nsfatima/calendario/integration/auditoria/AuditTrailQueryIntegrationTest.java
- [X] T019 [P] [US1] Criar teste de integração para ordenação determinística da trilha com timestamps iguais em app/src/test/java/br/com/nsfatima/calendario/integration/auditoria/AuditTrailOrderingIntegrationTest.java
- [X] T020 [P] [US1] Criar teste de integração para escopo organizacional negado na trilha em app/src/test/java/br/com/nsfatima/calendario/integration/auditoria/AuditTrailAuthorizationIntegrationTest.java

### Implementation for User Story 1

- [X] T021 [US1] Criar caso de uso de consulta da trilha auditável em app/src/main/java/br/com/nsfatima/calendario/application/usecase/metrics/ListAuditTrailUseCase.java
- [X] T022 [US1] Implementar projeção de leitura da trilha auditável em app/src/main/java/br/com/nsfatima/calendario/application/usecase/metrics/ListAuditTrailUseCase.java
- [X] T023 [US1] Expor `GET /api/v1/auditoria/eventos/trilha` no controller em app/src/main/java/br/com/nsfatima/calendario/api/controller/AuditoriaEventoController.java
- [X] T024 [US1] Garantir validação de `organizacaoId` e período na borda HTTP em app/src/main/java/br/com/nsfatima/calendario/api/controller/AuditoriaEventoController.java
- [X] T025 [US1] Persistir registros auditáveis para mutações de evento, aprovação e observação com campos mínimos da spec em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/EventoAuditPublisher.java, app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/ObservacaoAuditPublisher.java e app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/AuditLogService.java

**Checkpoint**: US1 funcional e testável isoladamente

---

## Phase 4: User Story 2 - Consultar taxa de retrabalho (Priority: P2)

**Goal**: Expor taxa de retrabalho por período e organização com numerador e denominador explícitos

**Independent Test**: Preparar ocorrências elegíveis de cancelamento, reagendamento e troca de organização responsável, consultar `GET /api/v1/auditoria/eventos/retrabalho` e validar taxa, numerador e denominador para a organização informada

### Tests for User Story 2

- [X] T026 [P] [US2] Criar teste de contrato para `GET /api/v1/auditoria/eventos/retrabalho` em app/src/test/java/br/com/nsfatima/calendario/contract/IndicadorRetrabalhoContractTest.java
- [X] T027 [P] [US2] Criar teste de integração para cálculo da taxa de retrabalho em app/src/test/java/br/com/nsfatima/calendario/integration/metrics/IndicadorRetrabalhoIntegrationTest.java
- [X] T028 [P] [US2] Criar teste de integração para período sem ocorrências de retrabalho em app/src/test/java/br/com/nsfatima/calendario/integration/metrics/IndicadorRetrabalhoZeroIntegrationTest.java
- [X] T029 [P] [US2] Criar teste de integração para rejeição de consulta sem `organizacaoId` em app/src/test/java/br/com/nsfatima/calendario/integration/metrics/IndicadorRetrabalhoValidationIntegrationTest.java

### Implementation for User Story 2

- [X] T030 [US2] Implementar cálculo do numerador de ocorrências elegíveis de retrabalho em app/src/main/java/br/com/nsfatima/calendario/application/usecase/metrics/GetIndicadorRetrabalhoUseCase.java
- [X] T031 [US2] Implementar cálculo do denominador de eventos afetados no período em app/src/main/java/br/com/nsfatima/calendario/application/usecase/metrics/GetIndicadorRetrabalhoUseCase.java
- [X] T032 [US2] Substituir o placeholder atual pelo cálculo real da taxa de retrabalho em app/src/main/java/br/com/nsfatima/calendario/application/usecase/metrics/GetIndicadorRetrabalhoUseCase.java
- [X] T033 [US2] Expor `GET /api/v1/auditoria/eventos/retrabalho` no controller em app/src/main/java/br/com/nsfatima/calendario/api/controller/AuditoriaEventoController.java
- [X] T034 [US2] Publicar projeção consistente da taxa de retrabalho com numerador e denominador em app/src/main/java/br/com/nsfatima/calendario/api/dto/metrics/IndicadorRetrabalhoResponse.java

**Checkpoint**: US2 funcional e testável isoladamente

---

## Phase 5: User Story 3 - Preservar baseline e evidências operacionais (Priority: P3)

**Goal**: Garantir snapshot semanal consistente, fail-closed e evidências verificáveis para revisão operacional

**Independent Test**: Induzir falha na persistência auditável obrigatória, validar rollback integral da mutação e confirmar que o snapshot semanal continua refletindo a métrica operacional após a mudança

### Tests for User Story 3

- [X] T035 [P] [US3] Criar teste de integração para fail-closed quando a persistência auditável obrigatória falha em app/src/test/java/br/com/nsfatima/calendario/integration/auditoria/AuditPersistenceFailureIntegrationTest.java
- [X] T036 [P] [US3] Criar teste de integração para snapshot semanal com taxa de retrabalho operacional em app/src/test/java/br/com/nsfatima/calendario/integration/metrics/WeeklyMetricsSnapshotIntegrationTest.java
- [X] T037 [P] [US3] Criar teste de integração para ausência de confirmação parcial da mutação em falha auditável em app/src/test/java/br/com/nsfatima/calendario/integration/auditoria/AuditRollbackConsistencyIntegrationTest.java

### Implementation for User Story 3

- [X] T038 [US3] Garantir rollback integral das mutações cobertas quando a auditoria persistida falhar em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/AuditLogService.java, app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CancelEventoUseCase.java, app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java e app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/DeleteObservacaoUseCase.java
- [X] T039 [US3] Evoluir o snapshot semanal para incluir payload consistente de retrabalho operacional em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/WeeklyMetricsSnapshotJob.java e app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/CadastroEventoMetricsPublisher.java
- [X] T040 [US3] Enriquecer as evidências de impacto operacional e baseline semanal em specs/009-audit-observability-closure/plan.md e specs/009-audit-observability-closure/quickstart.md

**Checkpoint**: US3 funcional e testável isoladamente

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Consolidação final de contrato, regressões constitucionais, performance e evidências constitucionais

- [X] T041 [P] Revisar o contrato OpenAPI final com exemplos e matriz de erro fechada em specs/009-audit-observability-closure/contracts/calendar-api-audit-observability.openapi.yaml
- [X] T042 [P] Criar teste de performance Tier 1 para os endpoints `trilha` e `retrabalho` em app/src/test/java/br/com/nsfatima/calendario/performance/AuditoriaTier1PerformanceTest.java
- [X] T043 Executar bateria final focada de contrato, integração, segurança e performance da feature em app/src/test/java/br/com/nsfatima/calendario
- [X] T044 [P] Verificar e registrar evidências de SC-001..SC-005 em specs/009-audit-observability-closure/plan.md
- [X] T045 [P] Atualizar o quickstart com comandos finais executados e smoke operacional em specs/009-audit-observability-closure/quickstart.md
- [X] T046 [P] Criar regressão contratual para preservação dos contratos existentes impactados, incluindo visibilidade pública por status, em app/src/test/java/br/com/nsfatima/calendario/contract/EventoStatusVisibilityRegressionContractTest.java
- [X] T047 [P] Criar regressão de integração para rejeição de transições inválidas de lifecycle nos fluxos existentes impactados em app/src/test/java/br/com/nsfatima/calendario/integration/evento/EventoLifecycleTransitionRegressionIntegrationTest.java
- [X] T048 [P] Criar validação de retenção histórica da baseline semanal com comparação entre snapshots consecutivos em app/src/test/java/br/com/nsfatima/calendario/integration/metrics/WeeklyMetricsSnapshotHistoryIntegrationTest.java

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: sem dependências, pode começar imediatamente.
- **Phase 2 (Foundational)**: depende da Phase 1 e bloqueia todas as histórias.
- **Phase 3 (US1)**: depende da conclusão da Phase 2.
- **Phase 4 (US2)**: depende da conclusão da Phase 2 e reutiliza a base da trilha auditável persistida.
- **Phase 5 (US3)**: depende da conclusão da Phase 2 e valida melhor após US1 e US2 fornecerem persistência e cálculo estáveis.
- **Phase 6 (Polish)**: depende das histórias que entrarem no release.

### User Story Dependencies

- **US1 (P1)**: pode começar imediatamente após Foundation; é o MVP da feature.
- **US2 (P2)**: depende da infraestrutura de auditoria persistida criada na Foundation; permanece independentemente testável depois disso.
- **US3 (P3)**: depende da Foundation e integra com a persistência e o cálculo criados nas histórias anteriores, mas mantém teste independente próprio.

### Within Each User Story

- Testes primeiro e devem falhar antes da implementação.
- DTOs e contratos antes dos controllers.
- Casos de uso antes da integração final na borda HTTP.
- A história só conclui quando passar no teste independente definido na fase.

### Parallel Opportunities

- T006, T007, T008, T009, T011, T014, T015 e T016 podem rodar em paralelo na foundation.
- Em US1, T017-T020 podem rodar em paralelo.
- Em US2, T026-T029 podem rodar em paralelo.
- Em US3, T035-T037 podem rodar em paralelo.
- T041, T042, T044, T045, T046, T047 e T048 podem rodar em paralelo na fase final.

---

## Parallel Example: User Story 1

- Task T017 [P] [US1]: contrato de `GET /api/v1/auditoria/eventos/trilha` em app/src/test/java/br/com/nsfatima/calendario/contract/AuditoriaEventosContractTest.java
- Task T018 [P] [US1]: integração de consulta por período e organização em app/src/test/java/br/com/nsfatima/calendario/integration/auditoria/AuditTrailQueryIntegrationTest.java
- Task T019 [P] [US1]: integração de ordenação determinística em app/src/test/java/br/com/nsfatima/calendario/integration/auditoria/AuditTrailOrderingIntegrationTest.java
- Task T020 [P] [US1]: integração de escopo negado em app/src/test/java/br/com/nsfatima/calendario/integration/auditoria/AuditTrailAuthorizationIntegrationTest.java

## Parallel Example: User Story 2

- Task T026 [P] [US2]: contrato do retrabalho em app/src/test/java/br/com/nsfatima/calendario/contract/IndicadorRetrabalhoContractTest.java
- Task T027 [P] [US2]: integração do cálculo da taxa em app/src/test/java/br/com/nsfatima/calendario/integration/metrics/IndicadorRetrabalhoIntegrationTest.java
- Task T028 [P] [US2]: integração de taxa zero em app/src/test/java/br/com/nsfatima/calendario/integration/metrics/IndicadorRetrabalhoZeroIntegrationTest.java
- Task T029 [P] [US2]: integração de validação sem `organizacaoId` em app/src/test/java/br/com/nsfatima/calendario/integration/metrics/IndicadorRetrabalhoValidationIntegrationTest.java

## Parallel Example: User Story 3

- Task T035 [P] [US3]: fail-closed em app/src/test/java/br/com/nsfatima/calendario/integration/auditoria/AuditPersistenceFailureIntegrationTest.java
- Task T036 [P] [US3]: snapshot semanal em app/src/test/java/br/com/nsfatima/calendario/integration/metrics/WeeklyMetricsSnapshotIntegrationTest.java
- Task T037 [P] [US3]: rollback consistente em app/src/test/java/br/com/nsfatima/calendario/integration/auditoria/AuditRollbackConsistencyIntegrationTest.java

---

## Implementation Strategy

### MVP First (US1)

1. Concluir Setup.
2. Concluir Foundation.
3. Entregar US1 completa.
4. Validar o teste independente da US1.
5. Demonstrar consulta auditável por período e organização.

### Incremental Delivery

1. Setup + Foundation.
2. US1: trilha auditável consultável.
3. US2: taxa de retrabalho operacional.
4. US3: fail-closed, baseline semanal e evidências.
5. Polish final com regressões constitucionais, performance e documentação.

### Parallel Team Strategy

1. O time fecha Phase 1 e 2 em conjunto.
2. Com foundation pronta:
   - Dev A: US1
   - Dev B: US2
   - Dev C: US3
3. A fase final consolida contrato, regressões constitucionais, performance e evidências constitucionais.

---

## Notes

- `[P]` indica tarefas com arquivos independentes e sem dependência direta entre si.
- `[USx]` liga cada tarefa à respectiva história para preservar rastreabilidade.
- Cada história foi estruturada para poder ser validada de forma independente.
- As tarefas preservam o módulo único `app` e evitam dependências novas fora do stack existente.
