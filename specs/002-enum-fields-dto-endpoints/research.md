# Research: Enum Mapping and Endpoint DTO Contracts

## Decision 1: Catalog Categorical Values as Explicit Enums
- Decision: Every categorical field currently accepted or returned as free-form `String` in affected HTTP flows must map to an explicit enum catalog.
- Rationale: Satisfies FR-001/FR-002 and removes ambiguity from domain-relevant values.
- Alternatives considered:
  - Manual string validation only: rejected due to drift and duplicated logic.
  - Validation only near persistence: rejected because invalid values would propagate too far.

## Decision 2: Normalize Enum Input at API Boundary Only
- Decision: Requests accept documented enum values after `trim` + case-insensitive normalization and convert them immediately to canonical enum representation.
- Rationale: Preserves client ergonomics for casing/whitespace mistakes while keeping the canonical contract strict.
- Alternatives considered:
  - Exact-match only: rejected because it adds avoidable client friction.
  - Aliases/localized spellings: rejected because they reintroduce contract ambiguity.

## Decision 3: Reject Unknown Extra Fields in All Changed Requests
- Decision: Every changed request DTO must reject undocumented fields rather than ignoring them.
- Rationale: Directly supports FR-011 and prevents continued dependence on dynamic payload behavior inherited from `Map`.
- Alternatives considered:
  - Ignore silently: rejected because hidden client errors would persist.
  - Vary by endpoint: rejected because it weakens predictability and complicates tests.

## Decision 4: Replace Map-Based Requests with Endpoint-Specific DTOs
- Decision: Controllers using `@RequestBody Map<...>` will bind to explicit request DTOs with named fields and validation metadata.
- Rationale: Makes request schemas authoritative and testable.
- Alternatives considered:
  - Keep `Map` plus manual checks: rejected because the contract remains implicit.
  - One generic DTO for all endpoints: rejected because it lowers cohesion.

## Decision 5: Replace Map-Based Responses with Typed DTOs
- Decision: Controllers and use cases returning `Map<String, Object>` will return explicit response DTOs.
- Rationale: Supports SC-002 and keeps HTTP serialization decoupled from ad-hoc map assembly.
- Alternatives considered:
  - Return domain entities directly: rejected to avoid leaking internal structure.
  - Keep map responses and document them informally: rejected because schema remains weak.

## Decision 6: Expose Legacy Invalid Stored Values via `UNKNOWN_LEGACY`
- Decision: When persisted legacy data contains an invalid categorical value, the resource remains readable and the affected field is exposed as `UNKNOWN_LEGACY`, with an auditable inconsistency signal.
- Rationale: Preserves operational readability without hiding data-quality issues.
- Alternatives considered:
  - Fail the whole resource read: rejected because it would create unnecessary operational breakage.
  - Auto-correct to a normal enum value: rejected because it would destroy meaning.
  - Omit the field silently: rejected because it hides inconsistency.

## Decision 7: Separate Input Enums from Response Enums When Needed
- Decision: Input schemas will not accept `UNKNOWN_LEGACY`; response schemas for legacy-exposed fields may include it.
- Rationale: Prevents clients from writing the sentinel while still documenting real read behavior.
- Alternatives considered:
  - Same enum for request and response: rejected because it would imply valid client input of `UNKNOWN_LEGACY`.

## Decision 8: Standardize Validation Error Semantics
- Decision: Invalid enum values and unknown extra fields must produce deterministic machine-readable codes with field identity and rejected value context when applicable.
- Rationale: Required by FR-009 and observability gates.
- Alternatives considered:
  - Generic 400 error: rejected because it weakens diagnosability.
  - Ad-hoc error format per controller: rejected because it creates drift.

## Decision 9: Preserve Route and Business Semantics While Hardening Contracts
- Decision: Routes, operations, and business outcomes remain unchanged; only payload shape and categorical validation become stricter.
- Rationale: Supports FR-006 and US3 by minimizing regression surface.
- Alternatives considered:
  - Introduce v2 routes immediately: rejected as unnecessary migration cost for this scope.

## Decision 10: No New Framework Dependencies
- Decision: Use only the current Spring Boot/Jackson/Bean Validation stack already present in the repository.
- Rationale: Meets simplicity and operational-risk constraints.
- Alternatives considered:
  - Add dedicated mapping library solely for DTO conversion: rejected as unnecessary overhead.

## Decision 11: Regression-Oriented Test Strategy
- Decision: Extend contract/integration tests to cover enum normalization, alias rejection, unknown field rejection, `UNKNOWN_LEGACY` exposure, and unchanged calendar behavior.
- Rationale: Provides executable evidence for SC-001..SC-004.
- Alternatives considered:
  - Unit-only checks: rejected because contract regressions would be missed.
  - Manual verification only: rejected due to low repeatability.

## Migration Mapping Examples
- `ProjetoController#create`
  - Legacy key map: `nome`, `descricao`
  - Explicit DTO: `ProjetoCreateRequest.nome`, `ProjetoCreateRequest.descricao`
- `ObservacaoController#create`
  - Legacy key map: `usuarioId`, `tipo`, `conteudo`
  - Explicit DTO: `ObservacaoCreateRequest.usuarioId`, `ObservacaoCreateRequest.tipo`, `ObservacaoCreateRequest.conteudo`
- `EventoParticipacaoController#putParticipantes`
  - Legacy key map: `organizacoesParticipantes`
  - Explicit DTO: `EventoParticipantesRequest.organizacoesParticipantes`
- `EventoParticipacaoController#createRecorrencia`
  - Legacy key map: `frequencia`, `intervalo`
  - Explicit DTO: `EventoRecorrenciaRequest.frequencia`, `EventoRecorrenciaRequest.intervalo`
- `AprovacaoController#create`
  - Legacy key map: `eventoId`, `tipoSolicitacao`
  - Explicit DTO: `AprovacaoCreateRequest.eventoId`, `AprovacaoCreateRequest.tipoSolicitacao`

## Implemented Evidence Summary
- Shared validation payloads and strict unknown-field rejection are active in the global HTTP boundary.
- Event status requests use typed enums with deterministic error projection and atomic rejection evidence.
- Observation, recurrence, and approval flows now use input enums and response enums with response-only `UNKNOWN_LEGACY` support.
- Compatibility notice is required because clients that relied on silent extra fields or free-form enum values will now receive deterministic 400 responses.
