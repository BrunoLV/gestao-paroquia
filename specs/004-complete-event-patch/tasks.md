# Tasks: PATCH Completo de Evento

**Input**: Design documents from `/specs/004-complete-event-patch/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Alinhar contrato, erros e estrutura base para implementacao do PATCH real

- [X] T001 Atualizar contrato de entrada parcial em app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/UpdateEventoRequest.java
- [X] T002 Definir excecoes de dominio para patch (not found/forbidden/approval required/conflict) em app/src/main/java/br/com/nsfatima/calendario/domain/exception/
- [X] T003 [P] Atualizar codigos deterministas de erro em app/src/main/java/br/com/nsfatima/calendario/api/error/ErrorCodes.java
- [X] T004 [P] Mapear novas excecoes para HTTP no app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java
- [X] T005 Definir matriz de contrato PATCH e exemplos finais em specs/004-complete-event-patch/contracts/calendar-api-complete-event-patch.openapi.yaml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Completar base tecnica obrigatoria antes das historias

**⚠️ CRITICAL**: Nenhuma historia deve iniciar antes da conclusao desta fase

- [X] T006 Ajustar acesso a persistencia de eventos para update transacional em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/EventoJpaRepository.java
- [X] T007 [P] Criar repositorio JPA de aprovacao em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/AprovacaoJpaRepository.java
- [X] T008 [P] Criar migration de persistencia de aprovacoes em app/src/main/resources/db/migration/V008__create_aprovacoes.sql
- [X] T009 [P] Implementar merge de entidade para PATCH no mapper em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/mapper/EventoMapper.java
- [X] T010 Criar resolvedor de ator/escopo para autorizacao de patch em app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/EventoActorContextResolver.java
- [X] T011 Implementar validador de permissao de PATCH com base em papel/escopo em app/src/main/java/br/com/nsfatima/calendario/domain/service/EventoPatchAuthorizationService.java
- [X] T012 Implementar validador de aprovacao para mudancas sensiveis exigindo explicitamente coordenador do conselho, vice-coordenador do conselho ou parroco e negando papeis nao autorizados em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/ValidateAprovacaoUseCase.java
- [X] T013 Preparar publisher de auditoria para resultado de patch com metadados de aprovacao em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/EventoAuditPublisher.java

**Checkpoint**: Fundacao pronta; historias podem ser executadas

---

## Phase 3: User Story 1 - Atualizar Parcialmente Evento Persistido (Priority: P1) 🎯 MVP

**Goal**: PATCH atualiza dados reais no banco e retorna estado persistido

**Independent Test**: Executar PATCH valido em evento existente e confirmar persistencia por leitura subsequente

### Tests for User Story 1

- [X] T014 [P] [US1] Criar teste de contrato de PATCH bem-sucedido em app/src/test/java/br/com/nsfatima/calendario/contract/EventosPatchContractTest.java
- [X] T015 [P] [US1] Criar teste de integracao de persistencia parcial em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoPersistenciaIntegrationTest.java
- [X] T016 [P] [US1] Criar teste de validacao para payload vazio/campos invalidos em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoValidationIntegrationTest.java
- [X] T041 [P] [US1] Criar teste de integracao para PATCH com evento inexistente e validacao de errorCode EVENT_NOT_FOUND em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoNotFoundIntegrationTest.java

### Implementation for User Story 1

- [X] T017 [US1] Refatorar fluxo principal de update para carregar entidade existente em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java
- [X] T018 [US1] Aplicar merge parcial e validacoes de dominio no use case em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java
- [X] T019 [US1] Persistir evento atualizado e mapear resposta real em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java
- [X] T020 [US1] Tratar evento inexistente com erro deterministico no fluxo de update em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java
- [X] T021 [US1] Publicar auditoria de sucesso/falha com ator real no endpoint PATCH em app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoController.java

**Checkpoint**: US1 funcional e testavel isoladamente

---

## Phase 4: User Story 2 - Bloquear Atualizacoes Invalidas por Regras de Negocio e Permissao (Priority: P2)

**Goal**: Regras de permissionamento e organizacao sao aplicadas de forma deterministica

**Independent Test**: Validar que usuarios sem permissao recebem FORBIDDEN e nao alteram estado persistido

### Tests for User Story 2

- [X] T022 [P] [US2] Criar testes de autorizacao por papel/escopo em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoAuthorizationIntegrationTest.java
- [X] T023 [P] [US2] Criar testes para regra de organizacao responsavel em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoOrganizacaoRulesIntegrationTest.java
- [X] T024 [P] [US2] Criar testes para participantes permitidos/proibidos em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoParticipantesIntegrationTest.java

### Implementation for User Story 2

- [X] T025 [US2] Integrar verificacao de permissao no update usando AuthorizationPolicy em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java
- [X] T026 [US2] Implementar persistencia de participantes no patch em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoParticipantesUseCase.java
- [X] T027 [US2] Implementar limpeza de participantes sem mock em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/ClearEventoParticipantesUseCase.java
- [X] T028 [US2] Enforcar regra de troca da organizacao responsavel por papel autorizado em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java
- [X] T029 [US2] Mapear negacoes de permissao para FORBIDDEN no handler global em app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java

**Checkpoint**: US2 funcional e testavel isoladamente

---

## Phase 5: User Story 3 - Resposta e Auditoria Confiaveis com Aprovacao Sensivel (Priority: P3)

**Goal**: Atualizacoes sensiveis exigem aprovacao e toda tentativa fica auditavel

**Independent Test**: Alteracao de data/cancelamento sem aprovacao deve falhar sem persistencia; com aprovacao valida deve persistir e registrar trilha

### Tests for User Story 3

- [X] T030 [P] [US3] Criar testes de aprovacao obrigatoria para data/cancelamento cobrindo coordenador do conselho, vice-coordenador do conselho, parroco e um perfil invalido em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoApprovalIntegrationTest.java
- [X] T031 [P] [US3] Criar testes de auditoria de patch sucesso/falha em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoAuditIntegrationTest.java
- [X] T032 [P] [US3] Criar testes de concorrencia otimista no patch em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoConcurrencyIntegrationTest.java
- [X] T042 [P] [US3] Criar teste de transicoes invalidas de lifecycle no PATCH com rejeicao deterministica em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoLifecycleTransitionsIntegrationTest.java

### Implementation for User Story 3

- [X] T033 [US3] Persistir solicitacao de aprovacao no use case de aprovacao em app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/CreateSolicitacaoAprovacaoUseCase.java
- [X] T034 [US3] Exigir aprovacao valida para mudancas sensiveis no fluxo de patch em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java
- [X] T035 [US3] Registrar metadados de aprovacao e resultados no servico de auditoria em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/AuditLogService.java
- [X] T036 [US3] Mapear conflito de concorrencia para erro CONFLICT em app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java

**Checkpoint**: US3 funcional e testavel isoladamente

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Fechamento de contrato, evidencia e conformidade

- [X] T037 [P] Atualizar guia de validacao final em specs/004-complete-event-patch/quickstart.md
- [X] T038 [P] Consolidar evidencias de execucao e metricas em specs/004-complete-event-patch/implementation-evidence.md
- [X] T039 [P] Revisar e alinhar exemplos e codigos de erro no contrato em specs/004-complete-event-patch/contracts/calendar-api-complete-event-patch.openapi.yaml
- [X] T040 Executar suites focadas e registrar resultado final em specs/004-complete-event-patch/checklists/requirements.md
- [X] T043 [P] Executar e registrar cobertura explicita de visibilidade publica por status em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/PublicStatusVisibilityIntegrationTest.java

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 (Setup): sem dependencia previa
- Phase 2 (Foundational): depende da Phase 1 e bloqueia historias
- Phase 3 (US1): depende da Phase 2
- Phase 4 (US2): depende da Phase 2; pode ocorrer em paralelo com US1 apos fundacao, mas prioridade recomendada e sequencial
- Phase 5 (US3): depende da Phase 2 e das bases de update/autorizacao prontas
- Phase 6 (Polish): depende das historias que entrarem no release

### User Story Dependencies

- US1 (P1): independente apos fundacao
- US2 (P2): independente apos fundacao, integra com fluxo de update da US1
- US3 (P3): depende do fluxo de update funcional e da infraestrutura de aprovacao

### Within Each User Story

- Testes da historia devem ser implementados e falhar antes dos ajustes finais de implementacao
- Implementacao base vem antes de ajustes de mapeamento de erro
- Historia so fecha apos teste independente passar

### Parallel Opportunities

- Setup: T003 e T004 podem executar em paralelo
- Foundational: T007, T008 e T009 podem executar em paralelo
- US1: T014, T015 e T016 podem executar em paralelo
- US2: T022, T023 e T024 podem executar em paralelo
- US3: T030, T031 e T032 podem executar em paralelo
- Polish: T037, T038 e T039 podem executar em paralelo

---

## Parallel Example: User Story 1

```bash
# Testes em paralelo (US1)
Task: T014 [US1] app/src/test/java/br/com/nsfatima/calendario/contract/EventosPatchContractTest.java
Task: T015 [US1] app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoPersistenciaIntegrationTest.java
Task: T016 [US1] app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoValidationIntegrationTest.java

# Implementacao coordenada apos testes
Task: T017 [US1] app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java
Task: T021 [US1] app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoController.java
```

## Parallel Example: User Story 2

```bash
# Testes em paralelo (US2)
Task: T022 [US2] app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoAuthorizationIntegrationTest.java
Task: T023 [US2] app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoOrganizacaoRulesIntegrationTest.java
Task: T024 [US2] app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoParticipantesIntegrationTest.java

# Implementacao paralelizavel por arquivos distintos
Task: T026 [US2] app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoParticipantesUseCase.java
Task: T027 [US2] app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/ClearEventoParticipantesUseCase.java
```

## Parallel Example: User Story 3

```bash
# Testes em paralelo (US3)
Task: T030 [US3] app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoApprovalIntegrationTest.java
Task: T031 [US3] app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoAuditIntegrationTest.java
Task: T032 [US3] app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UpdateEventoConcurrencyIntegrationTest.java

# Implementacao em paralelo parcial
Task: T033 [US3] app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/CreateSolicitacaoAprovacaoUseCase.java
Task: T035 [US3] app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/AuditLogService.java
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Concluir Phase 1 + Phase 2
2. Entregar Phase 3 (US1)
3. Validar teste independente da US1
4. Demonstrar PATCH com persistencia real

### Incremental Delivery

1. Fundacao completa
2. Entregar US1 e validar
3. Entregar US2 e validar
4. Entregar US3 e validar
5. Fechar polish e evidencias

### Parallel Team Strategy

1. Time A: fundacao de erro/contrato/repositorios (Phase 1-2)
2. Time B: fluxo principal US1
3. Time C: regras de permissao/participantes US2
4. Time D: aprovacao/auditoria/conflito US3

---

## Notes

- Tarefas com [P] sao paralelizaveis por arquivo e dependencia
- Labels [US1]/[US2]/[US3] garantem rastreabilidade por historia
- Cada historia deve permanecer validavel isoladamente
- Evitar acoplamento cruzado que quebre independencia dos checkpoints
