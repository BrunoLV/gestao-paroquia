# Tasks: Enum Mapping and Endpoint DTO Contracts

**Input**: Design documents from `/specs/002-enum-fields-dto-endpoints/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Automated tests are required for this feature because it changes API contracts and validation rules.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently where practical.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: User story label for traceability (`[US1]`, `[US2]`, `[US3]`)

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare shared contract-hardening artifacts and package-level support for enum/DTO migration.

- [X] T001 Update request/response schema matrix and migration notes in /home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/contracts/calendar-api-enum-dto.openapi.yaml
- [X] T002 Create field-level validation item payload in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/error/ValidationErrorItem.java
- [X] T003 Create validation error response payload in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/error/ValidationErrorResponse.java
- [X] T004 [P] Create enum request normalization support in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/support/EnumRequestNormalizer.java
- [X] T005 [P] Create legacy enum inconsistency publisher in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/LegacyEnumInconsistencyPublisher.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish shared validation, error, and contract behaviors required by all user stories.

**⚠️ CRITICAL**: No user story work should begin until this phase is complete.

- [X] T006 Update deterministic validation code catalog in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/error/ErrorCodes.java
- [X] T007 Update global exception mapping for invalid enums and unknown fields in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java
- [X] T008 [P] Add contract-validation integration coverage in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/integration/foundation/ContractValidationErrorIntegrationTest.java
- [X] T009 [P] Create implementation checklist for enum normalization and unknown-field rejection in /home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/quickstart.md
- [X] T010 Update feature contract examples and error scenarios in /home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/contracts/calendar-api-enum-dto.openapi.yaml
- [X] T074 [P] Implement strict unknown-property rejection for changed request DTO binding in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/infrastructure/config/JacksonConfig.java

**Checkpoint**: Shared validation and error infrastructure is ready; user story implementation can begin.

---

## Phase 3: User Story 1 - Enforce Valid Categorical Values (Priority: P1) 🎯 MVP

**Goal**: Replace free-form event categorical fields with explicit enums and enforce canonical validation behavior at the API boundary.

**Independent Test**: Send valid and invalid event create/update requests and verify trim + case-insensitive acceptance for canonical values, rejection for invalid values, and canonical enum output in responses.

### Tests for User Story 1

- [X] T011 [P] [US1] Update event list/create contract assertions for enum-backed status fields in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/EventosContractTest.java
- [X] T012 [P] [US1] Update event mutation contract assertions for enum validation and deterministic errors in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/EventoMutacaoContractTest.java
- [X] T013 [P] [US1] Create event enum normalization integration coverage in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/integration/eventos/EnumNormalizationIntegrationTest.java
- [X] T014 [P] [US1] Create legacy event status sentinel read coverage in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/integration/eventos/LegacyEventStatusReadIntegrationTest.java
- [X] T075 [P] [US1] Add failing contract coverage for atomic rejection of invalid partial update payloads in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/EventoMutacaoContractTest.java
- [X] T076 [P] [US1] Add integration coverage proving invalid partial updates do not mutate unrelated fields in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/integration/eventos/AtomicUpdateValidationIntegrationTest.java

### Implementation for User Story 1

- [X] T015 [P] [US1] Create event status input enum in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/domain/type/EventoStatusInput.java
- [X] T016 [P] [US1] Create event status response enum in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/domain/type/EventoStatusResponse.java
- [X] T017 [US1] Replace free-form request status typing in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/CreateEventoRequest.java
- [X] T018 [US1] Replace free-form request status typing in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/UpdateEventoRequest.java
- [X] T019 [US1] Replace free-form response status typing in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/EventoResponse.java
- [X] T020 [US1] Update event business validation for canonical enum transitions in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/domain/service/EventoDomainService.java
- [X] T021 [US1] Update event creation flow to consume canonical enum values in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CreateEventoUseCase.java
- [X] T022 [US1] Update event update flow to consume canonical enum values in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java
- [X] T023 [US1] Update event controller binding for enum-backed DTOs in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoController.java
- [X] T077 [US1] Enforce atomic rejection semantics for invalid partial updates in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java

**Checkpoint**: Event create/update/list flows enforce canonical categorical values and expose typed status responses.

---

## Phase 4: User Story 2 - Replace Generic Maps in API Contracts (Priority: P2)

**Goal**: Replace dynamic `Map` request/response payloads with explicit DTOs for project, observation, participants, recurrence, and approval endpoints.

**Independent Test**: Call each affected endpoint and verify explicit request/response structures, strict rejection of undocumented fields, and no remaining map-shaped domain payloads.

### Tests for User Story 2

- [X] T024 [P] [US2] Update project collection contract for typed request/response payloads in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/ProjetosContractTest.java
- [X] T025 [P] [US2] Update project mutation contract for typed patch payloads in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/ProjetoMutacaoContractTest.java
- [X] T026 [P] [US2] Create observation contract coverage for explicit DTO payloads in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/ObservacoesContractTest.java
- [X] T027 [P] [US2] Update participant replacement contract for explicit DTO payloads in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/EventoParticipantesContractTest.java
- [X] T028 [P] [US2] Update participant clear contract for explicit DTO responses in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/EventoParticipantesLimpezaContractTest.java
- [X] T029 [P] [US2] Update recurrence contract for typed enum request/response payloads in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/EventoRecorrenciaContractTest.java
- [X] T030 [P] [US2] Create approval contract coverage for typed DTO payloads in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/AprovacoesContractTest.java

### Implementation for User Story 2

- [X] T031 [P] [US2] Create project create request DTO in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/projeto/ProjetoCreateRequest.java
- [X] T032 [P] [US2] Create project patch request DTO in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/projeto/ProjetoPatchRequest.java
- [X] T033 [P] [US2] Create project response DTO in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/projeto/ProjetoResponse.java
- [X] T034 [P] [US2] Create observation create request DTO in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/observacao/ObservacaoCreateRequest.java
- [X] T035 [P] [US2] Create observation response DTO in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/observacao/ObservacaoResponse.java
- [X] T036 [P] [US2] Create event participants request DTO in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/EventoParticipantesRequest.java
- [X] T037 [P] [US2] Create event participants response DTO in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/EventoParticipantesResponse.java
- [X] T038 [P] [US2] Create event recurrence request DTO in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/EventoRecorrenciaRequest.java
- [X] T039 [P] [US2] Create event recurrence response DTO in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/EventoRecorrenciaResponse.java
- [X] T040 [P] [US2] Create approval request DTO in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/aprovacao/AprovacaoCreateRequest.java
- [X] T041 [P] [US2] Create approval response DTO in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/dto/aprovacao/AprovacaoResponse.java
- [X] T042 [US2] Update project use case to return typed contract data in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/projeto/CreateProjetoUseCase.java
- [X] T043 [US2] Update observation creation flow to return typed contract data in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/CreateObservacaoUseCase.java
- [X] T044 [US2] Update observation list flow to return typed contract data in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/ListObservacoesUseCase.java
- [X] T045 [US2] Update participant replacement flow to return typed contract data in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoParticipantesUseCase.java
- [X] T046 [US2] Update participant clear flow to return typed contract data in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/ClearEventoParticipantesUseCase.java
- [X] T047 [US2] Update recurrence flow to consume typed request data in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CreateEventoRecorrenciaUseCase.java
- [X] T048 [US2] Update approval flow to consume typed request data in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/CreateSolicitacaoAprovacaoUseCase.java
- [X] T049 [US2] Remove map-shaped payload handling from /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/controller/ProjetoController.java
- [X] T050 [US2] Remove map-shaped payload handling from /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/controller/ObservacaoController.java
- [X] T051 [US2] Remove map-shaped payload handling from /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoParticipacaoController.java
- [X] T052 [US2] Remove map-shaped payload handling from /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/controller/AprovacaoController.java

**Checkpoint**: All formerly dynamic-map endpoints expose explicit DTO contracts and reject undocumented request fields.

---

## Phase 5: User Story 3 - Preserve Calendar Domain Behavior During Contract Hardening (Priority: P3)

**Goal**: Preserve existing calendar behavior while adding legacy-safe response projection and regression coverage for contract hardening.

**Independent Test**: Re-run core calendar flows and verify unchanged behavior for valid operations, plus `UNKNOWN_LEGACY` exposure and audit signaling when reading invalid legacy categorical data.

### Tests for User Story 3

- [X] T053 [P] [US3] Create contract coverage for `UNKNOWN_LEGACY` response projection in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/LegacyEnumSentinelContractTest.java
- [X] T054 [P] [US3] Create unknown-field rejection integration coverage across migrated endpoints in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UnknownFieldRejectionIntegrationTest.java
- [X] T055 [P] [US3] Update project-event linkage regression coverage for typed contracts in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/integration/projetos/VinculoProjetoEventoIntegrationTest.java
- [X] T056 [P] [US3] Update end-to-end contract-hardening regression coverage in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/integration/SmokeCalendarFlowTest.java
- [X] T078 [P] [US3] Update public visibility regression coverage for hardened contracts in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/PublicVisibilityContractTest.java
- [X] T079 [P] [US3] Update RBAC scope regression coverage for hardened contracts in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/integration/security/RbacOrganizationIntegrationTest.java
- [X] T080 [P] [US3] Update invalid lifecycle/status regression contract coverage in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/contract/AddedExtraValidationContractTest.java
- [X] T081 [P] [US3] Update invalid lifecycle/status regression integration coverage in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/test/java/br/com/nsfatima/calendario/integration/eventos/AddedExtraValidationIntegrationTest.java

### Implementation for User Story 3

- [X] T057 [P] [US3] Create observation input enum in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/domain/type/TipoObservacaoInput.java
- [X] T058 [P] [US3] Create observation response enum with `UNKNOWN_LEGACY` in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/domain/type/TipoObservacaoResponse.java
- [X] T059 [P] [US3] Create recurrence input enum in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/domain/type/FrequenciaRecorrenciaInput.java
- [X] T060 [P] [US3] Create recurrence response enum with `UNKNOWN_LEGACY` in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/domain/type/FrequenciaRecorrenciaResponse.java
- [X] T061 [P] [US3] Create approval input enum in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/domain/type/TipoSolicitacaoInput.java
- [X] T062 [P] [US3] Create approval response enum with `UNKNOWN_LEGACY` in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/domain/type/TipoSolicitacaoResponse.java
- [X] T063 [US3] Extend observation response mapping and legacy inconsistency auditing in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/ListObservacoesUseCase.java
- [X] T064 [US3] Extend recurrence response mapping and legacy inconsistency auditing in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CreateEventoRecorrenciaUseCase.java
- [X] T065 [US3] Extend approval response mapping and legacy inconsistency auditing in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/CreateSolicitacaoAprovacaoUseCase.java
- [X] T066 [US3] Update observation controller response projection for legacy-aware enums in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/controller/ObservacaoController.java
- [X] T067 [US3] Update event participation controller response projection for legacy-aware enums in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoParticipacaoController.java
- [X] T068 [US3] Update approval controller response projection for legacy-aware enums in /home/bruno/DEV/WORKSPACES/calendario-paroquia/app/src/main/java/br/com/nsfatima/calendario/api/controller/AprovacaoController.java

**Checkpoint**: Contract hardening preserves operational behavior and surfaces invalid legacy values safely with `UNKNOWN_LEGACY` and audit signals.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize documentation, metrics evidence, and cross-story validation.

- [X] T069 [P] Update final contract examples and enum catalogs in /home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/contracts/calendar-api-enum-dto.openapi.yaml
- [X] T070 [P] Update implementation evidence and verification steps in /home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/quickstart.md
- [X] T071 [P] Add migration mapping examples for clients in /home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/research.md
- [X] T072 [P] Add response sentinel and enum catalog notes in /home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/data-model.md
- [X] T073 Run clarified feature validation checklist in /home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/spec.md
- [X] T082 [P] Publish consumer-facing compatibility notice in /home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/compatibility-notice.md
- [X] T083 [P] Reference the compatibility notice and PR evidence requirements in /home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; starts immediately.
- **Foundational (Phase 2)**: Depends on Setup; blocks all story work.
- **User Story 1 (Phase 3)**: Depends on Foundational only.
- **User Story 2 (Phase 4)**: Depends on Foundational; can proceed after shared validation/error behavior is in place.
- **User Story 3 (Phase 5)**: Depends on Foundational only; operates on existing endpoints and remains independently testable within its own scope.
- **Polish (Phase 6)**: Depends on completion of all desired user stories.

### User Story Dependencies

- **US1 (P1)**: No dependency on other stories; establishes MVP for canonical categorical validation on core event flows.
- **US2 (P2)**: No strict dependency on US1, but shares the same validation infrastructure from Phase 2.
- **US3 (P3)**: No dependency on other stories; hardens regression guarantees and legacy-safe projection on already-existing endpoints.

### Within Each User Story

- Contract and integration tests MUST be written and fail before implementation changes.
- Enum/type definitions before DTO binding changes.
- DTOs before controller/use-case migration.
- Shared response projection before regression validation.
- Partial-update mutation guards MUST be in place before closing US1.

### Parallel Opportunities

- Setup: T004-T005 can run in parallel.
- Foundational: T008-T009 and T074 can run in parallel.
- US1: T011-T014, T075 and T076 can run in parallel; T015-T016 can run in parallel.
- US2: T024-T030 can run in parallel; T031-T041 can run in parallel.
- US3: T053-T056 and T078-T081 can run in parallel; T057-T062 can run in parallel.
- Polish: T069-T072 and T082-T083 can run in parallel.

---

## Parallel Example: User Story 1

```bash
Task: "Update event list/create contract assertions in app/src/test/java/br/com/nsfatima/calendario/contract/EventosContractTest.java"
Task: "Update event mutation contract assertions in app/src/test/java/br/com/nsfatima/calendario/contract/EventoMutacaoContractTest.java"
Task: "Create event enum normalization integration coverage in app/src/test/java/br/com/nsfatima/calendario/integration/eventos/EnumNormalizationIntegrationTest.java"
Task: "Add integration coverage proving invalid partial updates do not mutate unrelated fields in app/src/test/java/br/com/nsfatima/calendario/integration/eventos/AtomicUpdateValidationIntegrationTest.java"
Task: "Create event status input enum in app/src/main/java/br/com/nsfatima/calendario/domain/type/EventoStatusInput.java"
Task: "Create event status response enum in app/src/main/java/br/com/nsfatima/calendario/domain/type/EventoStatusResponse.java"
```

---

## Parallel Example: User Story 2

```bash
Task: "Update project collection contract in app/src/test/java/br/com/nsfatima/calendario/contract/ProjetosContractTest.java"
Task: "Create observation contract coverage in app/src/test/java/br/com/nsfatima/calendario/contract/ObservacoesContractTest.java"
Task: "Create project create request DTO in app/src/main/java/br/com/nsfatima/calendario/api/dto/projeto/ProjetoCreateRequest.java"
Task: "Create observation create request DTO in app/src/main/java/br/com/nsfatima/calendario/api/dto/observacao/ObservacaoCreateRequest.java"
Task: "Create approval request DTO in app/src/main/java/br/com/nsfatima/calendario/api/dto/aprovacao/AprovacaoCreateRequest.java"
```

---

## Parallel Example: User Story 3

```bash
Task: "Create contract coverage for UNKNOWN_LEGACY in app/src/test/java/br/com/nsfatima/calendario/contract/LegacyEnumSentinelContractTest.java"
Task: "Create unknown-field rejection integration coverage in app/src/test/java/br/com/nsfatima/calendario/integration/eventos/UnknownFieldRejectionIntegrationTest.java"
Task: "Update public visibility regression coverage in app/src/test/java/br/com/nsfatima/calendario/contract/PublicVisibilityContractTest.java"
Task: "Update RBAC scope regression coverage in app/src/test/java/br/com/nsfatima/calendario/integration/security/RbacOrganizationIntegrationTest.java"
Task: "Create observation response enum with UNKNOWN_LEGACY in app/src/main/java/br/com/nsfatima/calendario/domain/type/TipoObservacaoResponse.java"
Task: "Create recurrence response enum with UNKNOWN_LEGACY in app/src/main/java/br/com/nsfatima/calendario/domain/type/FrequenciaRecorrenciaResponse.java"
Task: "Create approval response enum with UNKNOWN_LEGACY in app/src/main/java/br/com/nsfatima/calendario/domain/type/TipoSolicitacaoResponse.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. Validate event contract hardening independently before expanding scope.

### Incremental Delivery

1. Complete Setup + Foundational.
2. Deliver US1 and validate categorical enum enforcement.
3. Deliver US2 and validate typed DTO contracts on all map-based endpoints.
4. Deliver US3 and validate legacy-safe read behavior plus regression protection.
5. Finish with Polish documentation and evidence updates.

### Parallel Team Strategy

1. One engineer completes Setup + Foundational.
2. After Phase 2:
   - Engineer A: US1 event categorical validation.
   - Engineer B: US2 typed DTO migration for map-based endpoints.
   - Engineer C: US3 regression and legacy-safe projection on the already-existing endpoint set.

---

## Notes

- `[P]` tasks target different files with no immediate dependency on incomplete work.
- `[US#]` labels map implementation back to user stories in the spec.
- Requests must never accept `UNKNOWN_LEGACY`; that sentinel is response-only.
- Unknown request fields must be rejected consistently across all migrated endpoints.
- Preserve current routes and business semantics while hardening contract shape and validation.
