# Quickstart: Enum Mapping and Endpoint DTO Contracts

## 1. Prerequisites
- Java 21
- Gradle Wrapper (`./gradlew`)
- Local database settings compatible with project (`app/src/main/resources/application.yml`)

## 2. Build Baseline
1. Compile and run all tests before migration work:
```bash
./gradlew :app:build
```
2. Record baseline of current contract suites for affected endpoints:
```bash
./gradlew :app:test --tests '*contract*'
```

## 3. Implement and Verify Endpoint Contract Hardening
1. Replace `@RequestBody Map<...>` payloads with endpoint-specific request DTOs.
2. Replace `Map<String, Object>` responses with endpoint-specific response DTOs.
3. Replace categorical `String` parameters in changed flows with explicit input enums.
4. Expose `UNKNOWN_LEGACY` only in response enums for fields sourced from invalid legacy stored values.
5. Keep URIs, operation semantics, and business behavior unchanged.

## 4. Focused Test Execution
1. Run project mutation and endpoint tests:
```bash
./gradlew :app:test --tests '*Projeto*' --tests '*Observacao*' --tests '*EventoParticipantes*' --tests '*EventoRecorrencia*' --tests '*Aprovacao*'
```
2. Run full contract suites:
```bash
./gradlew :app:test --tests '*contract*'
```
3. Run full integration suites:
```bash
./gradlew :app:test --tests '*integration*'
```
4. Run the feature-focused validation batches used during implementation:
```bash
./gradlew :app:test --tests 'br.com.nsfatima.calendario.integration.foundation.ContractValidationErrorIntegrationTest'
./gradlew :app:test --tests 'br.com.nsfatima.calendario.contract.EventoMutacaoContractTest' --tests 'br.com.nsfatima.calendario.integration.eventos.EnumNormalizationIntegrationTest' --tests 'br.com.nsfatima.calendario.integration.eventos.AtomicUpdateValidationIntegrationTest' --tests 'br.com.nsfatima.calendario.contract.EventosContractTest' --tests 'br.com.nsfatima.calendario.integration.eventos.LegacyEventStatusReadIntegrationTest' --tests 'br.com.nsfatima.calendario.contract.AddedExtraValidationContractTest' --tests 'br.com.nsfatima.calendario.integration.eventos.AddedExtraValidationIntegrationTest'
./gradlew :app:test --tests 'br.com.nsfatima.calendario.contract.ProjetosContractTest' --tests 'br.com.nsfatima.calendario.contract.ProjetoMutacaoContractTest' --tests 'br.com.nsfatima.calendario.contract.ObservacoesContractTest' --tests 'br.com.nsfatima.calendario.contract.EventoParticipantesContractTest' --tests 'br.com.nsfatima.calendario.contract.EventoParticipantesLimpezaContractTest' --tests 'br.com.nsfatima.calendario.contract.EventoRecorrenciaContractTest' --tests 'br.com.nsfatima.calendario.contract.AprovacoesContractTest' --tests 'br.com.nsfatima.calendario.integration.eventos.AprovacaoHorarioIntegrationTest'
./gradlew :app:test --tests 'br.com.nsfatima.calendario.contract.LegacyEnumSentinelContractTest' --tests 'br.com.nsfatima.calendario.integration.eventos.UnknownFieldRejectionIntegrationTest' --tests 'br.com.nsfatima.calendario.integration.projetos.VinculoProjetoEventoIntegrationTest' --tests 'br.com.nsfatima.calendario.integration.SmokeCalendarFlowTest' --tests 'br.com.nsfatima.calendario.contract.PublicVisibilityContractTest' --tests 'br.com.nsfatima.calendario.integration.security.RbacOrganizationIntegrationTest' --tests 'br.com.nsfatima.calendario.contract.AddedExtraValidationContractTest' --tests 'br.com.nsfatima.calendario.integration.eventos.AddedExtraValidationIntegrationTest' --tests 'br.com.nsfatima.calendario.contract.ObservacoesContractTest' --tests 'br.com.nsfatima.calendario.contract.EventoRecorrenciaContractTest' --tests 'br.com.nsfatima.calendario.contract.AprovacoesContractTest' --tests 'br.com.nsfatima.calendario.integration.eventos.AprovacaoHorarioIntegrationTest'
```

## 5. Contract Validation Checklist
- Requests with valid enum values are accepted after `trim` + case-insensitive normalization.
- Requests with aliases/localized enum values are rejected with deterministic machine-readable field errors.
- Requests with undocumented extra fields are rejected with deterministic machine-readable validation errors.
- Responses from changed endpoints no longer include generic map-shaped domain payloads.
- Read flows over invalid legacy categorical data expose `UNKNOWN_LEGACY` and emit auditable inconsistency signals.
- Existing valid calendar flows (create/update/list/participants/recurrence) behave the same as before.

## 6. Implementation Checklist for Shared Validation
- Confirm changed request DTO bindings fail on undocumented JSON fields before controller execution.
- Assert validation responses expose `errorCode`, `correlationId`, and field-level `errors` entries.
- Verify required-field failures emit `VALIDATION_REQUIRED_FIELD` and unsupported enum values emit `VALIDATION_ENUM_VALUE_INVALID`.
- Verify unknown JSON fields emit `VALIDATION_UNKNOWN_FIELD` with the DTO field name.
- Keep normalization limited to canonical enum names after `trim` + case-insensitive matching.
- Preserve safe validation messages without leaking stack traces or persistence internals.

## 7. Observability and Audit Checks
- Validation failures are distinguishable from business-rule failures in logs.
- Mutating operations still emit correlation-aware audit traces.
- No sensitive internal details are leaked in validation errors.

## 8. Suggested Acceptance Evidence for Pull Request
- Before/after payload examples for each changed endpoint.
- Mapping table of legacy keys to DTO fields.
- Test outputs for enum normalization, enum rejection, unknown-field rejection, `UNKNOWN_LEGACY` exposure, and regression of core flows.
- Confirmation that endpoint routes and operation intent remain unchanged.
- Link to the consumer compatibility notice in `specs/002-enum-fields-dto-endpoints/compatibility-notice.md`.

## 9. Compatibility Notice
- Publish and review `specs/002-enum-fields-dto-endpoints/compatibility-notice.md` before merging.
- Ensure release notes mention strict unknown-field rejection and response-only `UNKNOWN_LEGACY` behavior.
