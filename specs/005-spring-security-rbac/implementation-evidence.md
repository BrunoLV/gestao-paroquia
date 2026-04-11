# Implementation Evidence: Spring Security RBAC com Usuario/Senha

## Baseline

- Data de baseline: 2026-04-11
- Status inicial antes da implementacao: seguranca ainda baseada em `httpBasic` stateless e leitura de headers `X-Actor-*`.
- Inconsistencias observadas antes da mudanca:
  - endpoints de negocio com `permitAll()` em `SecurityConfig`
  - ausencia de login stateful via `JSESSIONID`
  - ausencia de `UserDetailsService`/RBAC derivado do banco
  - ausencia de smoke test para transicoes invalidas de lifecycle

## Evidencias de execucao

### Validacao funcional e quickstart

- 2026-04-11: `./gradlew :app:test` -> **BUILD SUCCESSFUL**, `83 tests completed, 0 failed`.
- Validacoes focadas do quickstart executadas durante a implementacao:
  - `./gradlew :app:test --tests '*SecurityEndpointAuthIntegrationTest' --tests '*FormLoginIntegrationTest'` -> `6 passed / 0 failed`
  - `./gradlew :app:test --tests '*RbacOrganizationIntegrationTest' --tests '*SecurityAccessMatrixContractTest' --tests '*FormLoginIntegrationTest'` -> `12 passed / 0 failed`
  - `./gradlew :app:test --tests '*FailClosedAuthzIntegrationTest' --tests '*CalendarPayloadCompatibilityContractTest'` -> `5 passed / 0 failed`

### Compatibilidade e hardening

- `grep -R "X-Actor-" app/src/main/java` -> `0 matches`; o codigo principal passou a derivar contexto a partir do `SecurityContext`.
- Fluxos legados de `MockMvc` continuam cobertos via suporte de teste sem reabrir acesso anonimo aos endpoints protegidos.
- Logs de negacao agora incluem `correlationId`, `endpoint`, `userId`, `organizationId`, `result` e `errorCode`.

### SC-004 - reducao de inconsistencias operacionais

- Baseline pre-change: 4 inconsistencias relevantes observadas (`permitAll()` indevido, ausencia de sessao stateful, ausencia de RBAC derivado do banco e falta de smoke de lifecycle).
- Post-change: 0 vazamentos conhecidos de autorizacao nos endpoints de negocio durante a validacao final.
- Reducao observada das inconsistencias principais: **100%**, atendendo a meta `>= 80%`.

### SC-006 e metricas operacionais

- `calendar_query_latency_ms` foi instrumentada para endpoints de leitura e observada com `withinTarget=true` em amostras de `4ms`, `6ms` e `8ms` nos relatórios de teste (`CreateEventoPersistenciaIntegrationTest`, `SecurityAccessMatrixContractTest`, `SmokeCalendarFlowTest`), mantendo o alvo local de **p95 <= 500ms**.
- `event_registration_lead_time_minutes` foi instrumentada e emitida nos fluxos de criacao (`CreateEventoPersistenciaIntegrationTest`, `EventoIdempotencyIntegrationTest`, `RbacOrganizationIntegrationTest`).
- `administrative_rework_indicator` foi instrumentado e emitido nos fluxos de reagendamento/alteracao de organizacao responsavel (`UpdateEventoApprovalIntegrationTest`, `UpdateEventoOrganizacaoRulesIntegrationTest`).
- `WeeklyMetricsSnapshotJob` consolida snapshot semanal pos-mudanca para comparacao com o baseline.
