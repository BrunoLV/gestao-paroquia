# Tasks: Observacoes de Evento com Controle de Tipo e Autoria

**Input**: Design documents from /specs/008-observacoes-evento-crud/
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: A feature exige cobertura de contrato, integração e evidências mensuráveis, então tarefas de testes estão incluídas.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar o baseline técnico e a matriz de contrato/erros da feature

- [X] T001 Consolidar o contrato público da feature em specs/008-observacoes-evento-crud/contracts/calendar-api-observacoes-evento.openapi.yaml
- [X] T002 Alinhar os códigos de erro determinísticos da feature em app/src/main/java/br/com/nsfatima/calendario/api/error/ErrorCodes.java
- [X] T003 Atualizar o mapeamento HTTP dos novos erros de observação em app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestrutura comum que bloqueia todas as histórias

**CRITICAL**: Nenhuma história começa antes desta fase

- [X] T004 Criar migration para lifecycle de `NOTA`, soft delete e revisões em app/src/main/resources/db/migration/V016__extend_observacoes_evento_for_note_lifecycle.sql
- [X] T005 Atualizar o mapeamento principal de observações com campos de criação e remoção lógica em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/ObservacaoEventoEntity.java
- [X] T006 [P] Criar entidade de histórico de revisão de nota em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/ObservacaoNotaRevisaoEntity.java
- [X] T007 [P] Criar repositório JPA para revisões de nota em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/ObservacaoNotaRevisaoJpaRepository.java
- [X] T008 Atualizar consultas de observação para modos `todas`, `minhas` e exclusão de removidas em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/ObservacaoEventoJpaRepository.java
- [X] T009 [P] Criar DTO de atualização e enriquecer o DTO de resposta com `criadoEmUtc` em app/src/main/java/br/com/nsfatima/calendario/api/dto/observacao/ObservacaoUpdateRequest.java e app/src/main/java/br/com/nsfatima/calendario/api/dto/observacao/ObservacaoResponse.java
- [X] T010 Implementar políticas de tipo permitido, autoria e mutabilidade em app/src/main/java/br/com/nsfatima/calendario/domain/service/ObservacaoMutationPolicyService.java
- [X] T011 [P] Criar exceções específicas do fluxo de observações em app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/ObservacaoNaoEncontradaException.java, app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/ObservacaoAutorInvalidoException.java e app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/ObservacaoTipoImutavelException.java
- [X] T012 [P] Expandir a auditoria estruturada de observações para create/update/delete/list/system em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/ObservacaoAuditPublisher.java

**Checkpoint**: Base pronta para implementar histórias em sequência de prioridade

---

## Phase 3: User Story 1 - Registrar e consultar notas de evento (Priority: P1) MVP

**Goal**: Permitir criar `NOTA` manual e consultar observações do evento nos modos `minhas` e `todas`

**Independent Test**: Criar uma `NOTA` válida e validar retorno consistente em `GET /eventos/{eventoId}/observacoes/minhas` e `GET /eventos/{eventoId}/observacoes`

### Tests for User Story 1

- [X] T013 [P] [US1] Criar teste de contrato para POST e GETs de observações em app/src/test/java/br/com/nsfatima/calendario/contract/ObservacoesContractTest.java
- [X] T014 [P] [US1] Criar teste de integração para criação persistente e listagem completa de nota em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/CreateListObservacaoIntegrationTest.java
- [X] T015 [P] [US1] Criar teste de integração para filtragem de `minhas observacoes` em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/ListMyObservacoesIntegrationTest.java

### Implementation for User Story 1

- [X] T016 [US1] Implementar caso de uso de criação manual restrita a `NOTA` em app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/CreateNotaObservacaoUseCase.java
- [X] T017 [US1] Implementar caso de uso de listagem completa do evento em app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/ListObservacoesUseCase.java
- [X] T018 [US1] Implementar caso de uso de listagem `minhas observacoes` em app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/ListMinhasObservacoesUseCase.java
- [X] T019 [US1] Integrar POST, GET `todas` e GET `minhas` no controller em app/src/main/java/br/com/nsfatima/calendario/api/controller/ObservacaoController.java
- [X] T020 [US1] Atualizar o request manual para aceitar apenas `NOTA` sem `usuarioId` no payload e derivar autoria/criacao do contexto autenticado em app/src/main/java/br/com/nsfatima/calendario/api/dto/observacao/ObservacaoCreateRequest.java e app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/CreateNotaObservacaoUseCase.java

**Checkpoint**: US1 funcional e testável isoladamente

---

## Phase 4: User Story 2 - Editar e excluir somente notas próprias (Priority: P2)

**Goal**: Permitir editar `NOTA` própria com revisão auditável e excluir `NOTA` própria por soft delete

**Independent Test**: Criar uma `NOTA`, editá-la preservando revisão e removê-la logicamente, garantindo ocultação nas listagens funcionais e rejeição para não autor

### Tests for User Story 2

- [X] T021 [P] [US2] Criar teste de contrato para PATCH e DELETE de nota em app/src/test/java/br/com/nsfatima/calendario/contract/ObservacoesMutationContractTest.java
- [X] T022 [P] [US2] Criar teste de integração para edição de nota com histórico de revisão em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/UpdateObservacaoIntegrationTest.java
- [X] T023 [P] [US2] Criar teste de integração para exclusão lógica e ocultação funcional em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/DeleteObservacaoIntegrationTest.java
- [X] T024 [P] [US2] Criar teste de integração para rejeição de mutação por não autor ou tipo sistêmico em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/ObservacaoMutationAuthorizationIntegrationTest.java

### Implementation for User Story 2

- [X] T025 [US2] Implementar caso de uso de edição de `NOTA` com persistência de revisão em app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/UpdateObservacaoUseCase.java
- [X] T026 [US2] Implementar caso de uso de exclusão lógica de `NOTA` em app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/DeleteObservacaoUseCase.java
- [X] T027 [US2] Expor endpoints PATCH e DELETE no controller de observações em app/src/main/java/br/com/nsfatima/calendario/api/controller/ObservacaoController.java
- [X] T028 [US2] Persistir trilha de revisão de conteúdo em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/ObservacaoNotaRevisaoEntity.java e app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/UpdateObservacaoUseCase.java
- [X] T029 [US2] Aplicar ocultação de notas removidas nas projeções funcionais em app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/ObservacaoEventoJpaRepository.java e app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/ListObservacoesUseCase.java

**Checkpoint**: US2 funcional e testável isoladamente

---

## Phase 5: User Story 3 - Registrar observações sistêmicas por fluxo de ação (Priority: P3)

**Goal**: Gerar observações sistêmicas automaticamente com texto e autoria coerentes ao fluxo de origem

**Independent Test**: Executar um fluxo sistêmico de cancelamento e validar criação automática de `CANCELAMENTO` com conteúdo derivado da justificativa e autoria humana ou técnica correta

### Tests for User Story 3

- [X] T030 [P] [US3] Criar teste de integração para observação sistêmica de cancelamento com autoria humana em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoObservacaoIntegrationTest.java
- [X] T031 [P] [US3] Criar teste de integração para fallback técnico de autoria sistêmica em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/SystemObservacaoFallbackAuthorIntegrationTest.java
- [X] T032 [P] [US3] Criar teste de integração para rejeitar criação manual de tipo sistêmico em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/RejectSystemObservationManualCreationIntegrationTest.java

### Implementation for User Story 3

- [X] T033 [US3] Implementar serviço dedicado de registro sistêmico de observações em app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/RegisterSystemObservacaoUseCase.java
- [X] T034 [US3] Implementar política de autoria humana/fallback técnico em app/src/main/java/br/com/nsfatima/calendario/domain/service/ObservacaoSystemAuthorPolicyService.java
- [X] T035 [US3] Delegar o fluxo de cancelamento para o serviço sistêmico de observações em app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CancelEventoUseCase.java
- [X] T036 [US3] Publicar auditoria estruturada dos registros sistêmicos em app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/ObservacaoAuditPublisher.java e app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/RegisterSystemObservacaoUseCase.java

**Checkpoint**: US3 funcional e testável isoladamente

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Consolidação final e validação transversal

- [X] T037 [P] Atualizar exemplos finais e checklist de validação em specs/008-observacoes-evento-crud/quickstart.md
- [X] T038 [P] Revisar o contrato OpenAPI final com exemplos de create/list/update/delete em specs/008-observacoes-evento-crud/contracts/calendar-api-observacoes-evento.openapi.yaml
- [X] T039 [P] Criar teste de performance Tier 1 para endpoints de observação em app/src/test/java/br/com/nsfatima/calendario/performance/ObservacaoTier1PerformanceTest.java
- [X] T040 Executar bateria final focada de contratos e integrações de observações em app/src/test/java/br/com/nsfatima/calendario
- [X] T041 Verificar evidências de SC-001..SC-012 no plano em specs/008-observacoes-evento-crud/plan.md
- [X] T042 Implementar consulta interna de auditoria para recuperar `NOTA` removida logicamente fora das listagens funcionais em app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/ListRemovedObservacoesForAuditUseCase.java e app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/ObservacaoEventoJpaRepository.java
- [X] T043 Definir e executar baseline operacional semanal de p95 (create/edit/list) e registrar evidências da comparação pré e pós implementação em specs/008-observacoes-evento-crud/quickstart.md e specs/008-observacoes-evento-crud/plan.md

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 (Setup): sem dependências
- Phase 2 (Foundational): depende da Phase 1 e bloqueia histórias
- Phase 3 (US1): depende da Phase 2
- Phase 4 (US2): depende da Phase 2 e reutiliza a persistência/listagem de US1
- Phase 5 (US3): depende da Phase 2 e integra o modelo persistente compartilhado de observações
- Phase 6 (Polish): depende das histórias que entrarem no release

### User Story Dependencies

- US1 (P1): início imediato após foundation; base do MVP
- US2 (P2): depende da base de observações persistidas e das regras manuais de US1
- US3 (P3): depende da foundation e integra com o modelo persistente compartilhado; valida melhor quando US1 já tiver consolidado o contrato manual

### Within Each User Story

- Testes primeiro (devem falhar antes da implementação)
- Persistência/políticas antes de controller
- Caso de uso antes de integração com fluxos produtores
- Story só conclui quando passa no teste independente definido na fase

### Parallel Opportunities

- T006, T007, T009, T011 e T012 podem rodar em paralelo na foundation
- Em US1, T013-T015 podem rodar em paralelo
- Em US2, T021-T024 podem rodar em paralelo
- Em US3, T030-T032 podem rodar em paralelo
- T037, T038 e T039 podem rodar em paralelo na fase final

---

## Parallel Example: User Story 1

- Task T013 [P] [US1]: teste de contrato de POST e GETs em app/src/test/java/br/com/nsfatima/calendario/contract/ObservacoesContractTest.java
- Task T014 [P] [US1]: integração de criação e listagem completa em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/CreateListObservacaoIntegrationTest.java
- Task T015 [P] [US1]: integração da listagem `minhas` em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/ListMyObservacoesIntegrationTest.java

## Parallel Example: User Story 2

- Task T021 [P] [US2]: contrato de PATCH/DELETE em app/src/test/java/br/com/nsfatima/calendario/contract/ObservacoesMutationContractTest.java
- Task T022 [P] [US2]: integração de edição com revisões em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/UpdateObservacaoIntegrationTest.java
- Task T023 [P] [US2]: integração de soft delete em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/DeleteObservacaoIntegrationTest.java
- Task T024 [P] [US2]: integração de rejeição por não autor/tipo imutável em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/ObservacaoMutationAuthorizationIntegrationTest.java

## Parallel Example: User Story 3

- Task T030 [P] [US3]: integração de `CANCELAMENTO` com autoria humana em app/src/test/java/br/com/nsfatima/calendario/integration/eventos/CancelEventoObservacaoIntegrationTest.java
- Task T031 [P] [US3]: integração de fallback técnico em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/SystemObservacaoFallbackAuthorIntegrationTest.java
- Task T032 [P] [US3]: integração de rejeição de tipo sistêmico manual em app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/RejectSystemObservationManualCreationIntegrationTest.java

---

## Implementation Strategy

### MVP First (US1)

1. Concluir Setup
2. Concluir Foundation
3. Entregar US1 completa
4. Validar independent test da US1
5. Demonstrar criação e listagem funcional de `NOTA`

### Incremental Delivery

1. Setup + Foundation
2. US1 (criação e listagem manual)
3. US2 (edição com revisão + soft delete)
4. US3 (geração sistêmica automática)
5. Polish final

### Parallel Team Strategy

1. Time fecha Phase 1 e 2 em conjunto
2. Com foundation pronta:
   - Dev A: US1
   - Dev B: US2
   - Dev C: US3
3. Fase final consolida performance, evidências e documentação

---

## Notes

- [P] indica tarefas sem dependência direta entre arquivos
- [USx] liga cada tarefa à história correspondente
- Cada história foi estruturada para ser validada independentemente
- Commits devem seguir checkpoints por fase/história para facilitar rollback e revisão
