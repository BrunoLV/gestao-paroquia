# Tasks: Spring Security RBAC com Usuario/Senha

**Input**: Design documents from `/specs/005-spring-security-rbac/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Testes automatizados sao obrigatorios para esta feature (contrato e validacao de seguranca), com criterio fail-first por user story.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Setup/Foundational/Polish tasks do not include story labels
- Every task includes explicit file path(s)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish base artifacts and migration baseline for security modernization.

- [X] T001 Criar migracao `V011__create_usuarios_membros_organizacao.sql` com `CREATE TABLE IF NOT EXISTS` para `usuarios` e `membros_organizacao`, FK `usuario_id` com `ON DELETE RESTRICT` e indice `(usuario_id, ativo)` em `app/src/main/resources/db/migration/V011__create_usuarios_membros_organizacao.sql`
- [X] T002 [P] Criar fixture SQL de usuarios e vinculos organizacionais para testes de seguranca em `app/src/test/resources/sql/security-fixtures.sql`
- [X] T003 [P] Registrar baseline pre-change de inconsistencias de autorizacao por endpoint (SC-004) em `specs/005-spring-security-rbac/implementation-evidence.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build the core auth/authz infrastructure that blocks all user stories.

**CRITICAL**: No user story work can begin until this phase is complete.

- [X] T004 [P] Implementar `UsuarioDetails` (UserDetails) com `username`, `usuarioId`, `enabled` e `authorities` em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/UsuarioDetails.java`
- [X] T005 [P] Implementar `ExternalMembershipReader` com consulta real em `membros_organizacao` e derivacao de `ROLE_<TIPO_ORG>_<PAPEL>` em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/ExternalMembershipReader.java`
- [X] T006 Implementar `UsuarioDetailsService` + `PasswordEncoder` + `DaoAuthenticationProvider` para autenticacao por `usuarios.username` em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/UsuarioDetailsService.java` e `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/SecurityConfig.java`
- [X] T007 [P] Adicionar `AUTH_INVALID`, `ROLE_SCOPE_INVALID`, `AUTHZ_SOURCE_UNAVAILABLE` e `SESSION_EXPIRED` em `app/src/main/java/br/com/nsfatima/calendario/api/error/ErrorCodes.java`
- [X] T008 [P] Criar utilitario de teste para login/sessao (helper de cookie JSESSIONID) em `app/src/test/java/br/com/nsfatima/calendario/support/SecurityTestSupport.java`

**Checkpoint**: Foundation ready - user story implementation can now begin.

---

## Phase 3: User Story 1 - Bloquear acesso indevido por perfil (Priority: P1) 🎯 MVP

**Goal**: Garantir autenticacao stateful por sessao e bloqueio de mutacoes sem papel valido, com codigos de erro consistentes.

**Independent Test**: Sem sessao -> 401; login valido -> 200 + JSESSIONID; mutacao com papel invalido -> 403; sessao expirada -> 401 SESSION_EXPIRED.

### Tests for User Story 1 (fail-first)

- [X] T009 [P] [US1] Criar testes fail-first de bloqueio sem autenticacao para endpoints de negocio em `app/src/test/java/br/com/nsfatima/calendario/infrastructure/security/SecurityEndpointAuthIntegrationTest.java`
- [X] T010 [P] [US1] Criar testes fail-first de ciclo login/logout/sessao expirada (401 SESSION_EXPIRED sem redirect) em `app/src/test/java/br/com/nsfatima/calendario/infrastructure/security/FormLoginIntegrationTest.java`

### Implementation for User Story 1

- [X] T011 [US1] Reescrever `SecurityConfig` para `SessionCreationPolicy.IF_REQUIRED`, `formLogin` em `/api/v1/auth/login`, `logout` em `/api/v1/auth/logout`, `AuthenticationEntryPoint` 401 JSON, `AccessDeniedHandler` 403 JSON e configuracao explicita/revisada de CSRF e cookie seguro em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/SecurityConfig.java`
- [X] T012 [US1] Implementar handlers JSON de sucesso/falha de autenticacao sem redirecionamento em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/JsonAuthenticationHandlers.java`
- [X] T013 [US1] Remover leitura de headers `X-Actor-*` e derivar contexto de ator a partir de `SecurityContext` em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/EventoActorContextResolver.java`
- [X] T014 [US1] Mapear `DataAccessException`, `AuthenticationException` e `AccessDeniedException` para erros padronizados em `app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java`
- [X] T015 [US1] Executar testes US1 e ajustar comportamento ate ficarem verdes em `app/src/test/java/br/com/nsfatima/calendario/infrastructure/security/SecurityEndpointAuthIntegrationTest.java` e `app/src/test/java/br/com/nsfatima/calendario/infrastructure/security/FormLoginIntegrationTest.java`

**Checkpoint**: User Story 1 is independently functional and testable.

---

## Phase 4: User Story 2 - Aplicar matriz de permissoes por endpoint (Priority: P2)

**Goal**: Aplicar matriz de acesso consistente em todos os endpoints, eliminando permissoes implicitas e regressao em endpoints equivalentes.

**Independent Test**: Para cada endpoint da matriz, validar 401 sem sessao, 403 sem papel valido e 2xx com papel valido.

### Tests for User Story 2 (fail-first)

- [X] T016 [P] [US2] Criar testes fail-first de autorizacao por escopo organizacional e papel em `app/src/test/java/br/com/nsfatima/calendario/infrastructure/security/RbacOrganizationIntegrationTest.java`
- [X] T017 [P] [US2] Criar teste de contrato da matriz de acesso cruzando OpenAPI x SecurityConfig em `app/src/test/java/br/com/nsfatima/calendario/contract/SecurityAccessMatrixContractTest.java`

### Implementation for User Story 2

- [X] T018 [US2] Auditar e corrigir todos os `requestMatchers` para refletir `contracts/spring-security-rbac-access-matrix.yaml`, revisando explicitamente as regras de CSRF para os fluxos autenticados e sem deixar TODO pendente, em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/SecurityConfig.java`
- [X] T019 [US2] Garantir retorno `ROLE_SCOPE_INVALID` para incompatibilidade papel x tipo organizacao em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/EventoActorContextResolver.java` e `app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java`
- [X] T020 [US2] Validar contrato de logout (204 + invalidacao de sessao + limpeza de cookie) em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/SecurityConfig.java` e `app/src/test/java/br/com/nsfatima/calendario/infrastructure/security/FormLoginIntegrationTest.java`
- [X] T021 [US2] Executar testes US2 e ajustar ate cobertura completa da matriz em `app/src/test/java/br/com/nsfatima/calendario/infrastructure/security/RbacOrganizationIntegrationTest.java` e `app/src/test/java/br/com/nsfatima/calendario/contract/SecurityAccessMatrixContractTest.java`

**Checkpoint**: User Story 2 is independently functional and testable.

---

## Phase 5: User Story 3 - Compatibilidade com modelo documental externo (Priority: P3)

**Goal**: Garantir leitura exclusiva de estruturas externas (`usuarios`, `membros_organizacao`) e preservar payloads de dominio (FR-011).

**Independent Test**: Operacoes de identidade sem escrita em tabelas externas, fail-closed validado e payloads de dominio inalterados.

### Tests for User Story 3 (fail-first)

- [X] T022 [P] [US3] Criar testes fail-first de indisponibilidade da fonte de autorizacao (503 AUTHZ_SOURCE_UNAVAILABLE) em `app/src/test/java/br/com/nsfatima/calendario/infrastructure/security/FailClosedAuthzIntegrationTest.java`
- [X] T023 [P] [US3] Criar teste de regressao de payload para FR-011 (sem alterar contratos de dominio) em `app/src/test/java/br/com/nsfatima/calendario/contract/CalendarPayloadCompatibilityContractTest.java`

### Implementation for User Story 3

- [X] T024 [US3] Aplicar `@Transactional(readOnly = true)` em leituras de identidade/vinculo em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/UsuarioDetailsService.java` e `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/ExternalMembershipReader.java`
- [X] T025 [US3] Revisar e alinhar migracao V011 com data-model (tipos, constraints, FK RESTRICT, indice) em `app/src/main/resources/db/migration/V011__create_usuarios_membros_organizacao.sql`
- [X] T026 [US3] Validar ausencia de escrita em tabelas externas e executar testes US3 em `app/src/test/java/br/com/nsfatima/calendario/infrastructure/security/FailClosedAuthzIntegrationTest.java` e `app/src/test/java/br/com/nsfatima/calendario/contract/CalendarPayloadCompatibilityContractTest.java`

**Checkpoint**: User Story 3 is independently functional and testable.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final hardening, observability evidence, and full validation.

- [X] T027 [P] Localizar com `grep -R "X-Actor-" app/src/main/java` e remover/substituir referencias remanescentes a headers `X-Actor-*` pelo contexto derivado do `SecurityContext` nos arquivos afetados de `app/src/main/java/br/com/nsfatima/calendario/`
- [X] T028 [P] Enriquecer logs de negacao com `correlationId`, `endpoint`, `userId`, `organizationId`, `result`, `errorCode` em `app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java` e `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/AuditLogService.java`
- [X] T029 [P] Publicar snapshot semanal pos-mudanca e comparar com baseline pre-change (meta SC-004 >= 80%) em `specs/005-spring-security-rbac/implementation-evidence.md`
- [X] T030 [P] Registrar nota de impacto metrico para fluxo critico no PR (negacoes, falha de autenticacao, mutacoes autorizadas) em `specs/005-spring-security-rbac/implementation-evidence.md`
- [X] T031 Executar `quickstart.md` e suite completa de testes, anexando evidencias finais em `specs/005-spring-security-rbac/implementation-evidence.md`, incluindo coleta/registro do alvo de `calendar query latency` para SC-006
- [X] T032 [P] Instrumentar e expor metrica de `event registration lead time` (tempo entre criacao e inicio do evento) em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/` e `app/src/main/java/br/com/nsfatima/calendario/application/`
- [X] T033 [P] Instrumentar e expor metrica de `calendar query latency` por endpoint de leitura em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/` e `app/src/main/java/br/com/nsfatima/calendario/api/controller/`, validando evidencia de **p95 <= 500ms** para SC-006
- [X] T034 [P] Instrumentar e calcular `administrative rework indicator` para alteracoes/reagendamentos em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/` e `app/src/main/java/br/com/nsfatima/calendario/domain/`
- [X] T035 [P] Criar smoke test de regressao para transicao de status invalida (ex.: cancelar evento ja cancelado -> 422), confirmando que a camada de seguranca nao altera o comportamento de dominio existente em `app/src/test/java/br/com/nsfatima/calendario/contract/LifecycleTransitionRegressionTest.java`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - blocks all user stories
- **User Stories (Phases 3-5)**: Depend on Foundational completion
  - US1 (P1) starts first (MVP)
  - US2 (P2) depends on US1 security pipeline being stable
  - US3 (P3) starts after US1 to avoid overlap in security adapters
- **Polish (Phase 6)**: Depends on user stories completion

### User Story Dependencies

- **US1 (P1)**: Can start after Foundational
- **US2 (P2)**: Depends on T011 (SecurityConfig rewrite) and T014 (error mapping)
- **US3 (P3)**: Depends on T006 (identity loading), T011 (session/auth pipeline), and migration T001

### Within Each User Story

- Tests must be written first and fail before implementation
- Auth/authz core implementation before endpoint matrix hardening
- Contract/payload compatibility checks before story completion
- Story test suite must pass before moving to next priority

### Parallel Opportunities

- **Phase 1**: T002 and T003 in parallel after T001 is planned
- **Phase 2**: T004, T005, T007, T008 can run in parallel; T006 depends on T004+T005
- **US1**: T009 and T010 in parallel; T012 and T013 can progress after T011 starts
- **US2**: T016 and T017 in parallel; T019 and T020 can run in parallel after T018
- **US3**: T022 and T023 in parallel; T024 and T025 can run in parallel
- **Polish**: T027-T030 and T032-T035 in parallel; T031 is final gate

---

## Parallel Example: User Story 1

```bash
# Fail-first tests in parallel
Task: "T009 [US1] app/src/test/java/br/com/nsfatima/calendario/infrastructure/security/SecurityEndpointAuthIntegrationTest.java"
Task: "T010 [US1] app/src/test/java/br/com/nsfatima/calendario/infrastructure/security/FormLoginIntegrationTest.java"

# Implementation tasks after tests fail
Task: "T011 [US1] app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/SecurityConfig.java"
Task: "T012 [US1] app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/JsonAuthenticationHandlers.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. Stop and validate US1 independently (auth/session/error semantics)
5. Deploy/demo MVP security hardening

### Incremental Delivery

1. Setup + Foundational -> shared security base
2. Deliver US1 (auth/session hardening)
3. Deliver US2 (endpoint matrix consistency)
4. Deliver US3 (external model compatibility + payload stability)
5. Finish with Polish and operational evidence

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Then:
   - Developer A: US1 implementation path
   - Developer B: US2 tests and matrix contract checks
   - Developer C: US3 read-only and compatibility checks (after US1 base ready)
3. Converge on Polish phase and full-suite validation

---

## Notes

- All task lines follow required checklist format
- IDs are sequential (T001-T034)
- Story labels are present only in user-story phases
- [P] marks only safe parallel work
- Each task includes concrete file path(s)
