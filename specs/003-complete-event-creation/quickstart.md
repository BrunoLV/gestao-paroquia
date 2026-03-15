# Quickstart: Criacao Completa de Evento

## 1. Prerequisites
- Java 21
- Gradle Wrapper (`./gradlew`)
- Banco configurado para profile local (`app/src/main/resources/application.yml`)

## 2. Build Baseline
1. Validar baseline atual:
```bash
./gradlew :app:build
```
2. Executar suites de contrato/integracao relacionadas a eventos:
```bash
./gradlew :app:test --tests '*EventosContractTest' --tests '*CreateEventoPersistenciaIntegrationTest' --tests '*EventoIdempotencyIntegrationTest' --tests '*UnknownFieldRejectionIntegrationTest'
```

## 3. Estado Implementado
1. `POST /api/v1/eventos` recebe payload completo, exige `Idempotency-Key` e persiste em banco.
2. Retries equivalentes retornam a mesma resposta; reuse divergente retorna `409` com `IDEMPOTENCY_KEY_CONFLICT`.
3. Regras de dominio do create validam intervalo temporal, justificativa de `ADICIONADO_EXTRA` e integridade entre organizacao responsavel e participantes.
4. Sobreposicao nao bloqueia a criacao: a resposta retorna `conflictState=CONFLICT_PENDING` quando aplicavel.
5. `GET /api/v1/eventos` lista dados persistidos, em ordem deterministica, e exige autenticacao.

## 4. Focused Validation
1. Criacao valida persiste e retorna `201`:
```bash
./gradlew :app:test --tests '*EventosContractTest' --tests '*CreateEventoPersistenciaIntegrationTest'
```
2. Rejeicao de payload invalido e campos extras:
```bash
./gradlew :app:test --tests '*UnknownFieldRejectionIntegrationTest' --tests '*ContractValidationErrorIntegrationTest'
```
3. Idempotencia e conflito nao bloqueante:
```bash
./gradlew :app:test --tests '*EventoIdempotencyIntegrationTest' --tests '*CreateEventoConflitoPendingIntegrationTest'
```
4. Regras de status/lifecycle no create:
```bash
./gradlew :app:test --tests '*AddedExtraValidationContractTest' --tests '*CreateEventoValidationIntegrationTest'
```
5. RBAC e visibilidade autenticada:
```bash
./gradlew :app:test --tests '*DeniedWriteAuditIntegrationTest' --tests '*PublicVisibilityContractTest' --tests '*PublicStatusVisibilityIntegrationTest'
```
6. Compatibilidade de leitura legada e integridade organizacao/participantes:
```bash
./gradlew :app:test --tests '*LegacyEventoReadCompatibilityIntegrationTest' --tests '*EventoOrganizacaoParticipantesIntegrityIntegrationTest'
```
7. Observabilidade e budget de metricas/performance:
```bash
./gradlew :app:test --tests '*EventoCreateAuditIntegrationTest' --tests '*WeeklyMetricsSnapshotIntegrationTest' --tests '*ConsultaCalendarioPerformanceTest'
```

## 5. Manual API Smoke
1. `POST /api/v1/eventos` com `Idempotency-Key` e payload completo -> `201`.
2. Repetir mesma chamada com mesma chave -> mesma resposta sem duplicar evento.
3. Repetir mesma chave com payload diferente -> erro de conflito de idempotencia.
4. `GET /api/v1/eventos` sem autenticacao -> `401/403`.
5. `GET /api/v1/eventos` autenticado -> lista com dados reais persistidos.

## 6. Contract Checklist
- `POST` exige `Idempotency-Key`.
- Campos desconhecidos no payload retornam erro deterministico.
- `ADICIONADO_EXTRA` sem justificativa e rejeitado.
- Sobreposicao de agenda nao bloqueia create e gera estado `CONFLICT_PENDING`.
- `GET /eventos` nao retorna mock e respeita autenticacao obrigatoria.

## 7. Observability Checklist
- Cada tentativa de create gera trilha com `correlationId`, ator, acao, alvo e resultado.
- Falhas de validacao e de regra de negocio sao distinguiveis nos logs.
- Reuso de idempotencia e conflitos de chave deixam evidencia auditavel.

## 8. Measurement Checklist (SC-003 e SC-004)
- SC-003: usar a evidencia consolidada em `specs/003-complete-event-creation/implementation-evidence.md`; como nao havia telemetria historica pre-feature, registrar baseline operacional por numero de round-trips obrigatorios do fluxo legado e comparar com o fluxo de payload unico.
- SC-004: registrar o guardrail de p95 na mesma evidencia consolidada, usando a suite `ConsultaCalendarioPerformanceTest` como budget automatizado e as suites autenticadas de listagem como confirmacao funcional.
- Registrar qualquer execucao adicional de regressao no mesmo artefato de evidencia, sem recriar `release-readiness.md`.
