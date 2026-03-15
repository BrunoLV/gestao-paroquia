# Feature Specification: Enum Mapping and Endpoint DTO Contracts

**Feature Branch**: `002-enum-fields-dto-endpoints`  
**Created**: 2026-03-15  
**Status**: Implemented  
**Input**: User description: "Ajustar para que campos string como por exemplo nao sejam mapeados com String e que ao inves sejam criados Enums para os valores mapeados. Para os endpoints devem ser criados dtos para mapear corretamente o dominio e nao Map."

## Clarifications

### Session 2026-03-15

- Q: Como a API deve aceitar valores de enum vindos do cliente? → A: Aceitar valor com trim e case-insensitive, convertendo para o enum canonico, sem aliases ou localizacoes.
- Q: Como tratar campos extras nao documentados nos payloads de entrada? → A: Rejeitar qualquer campo extra nao documentado em todos os requests alterados.
- Q: Como tratar leitura de registros legados com valor categorico invalido? → A: Retornar o recurso, mapear o campo invalido para um sentinela explicito e auditar a inconsistencia.
- Q: Como representar o valor sentinela para dados legados invalidos? → A: Adicionar o membro padronizado `UNKNOWN_LEGACY` em cada enum afetado por leitura de legado.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Enforce Valid Categorical Values (Priority: P1)

As a backend maintainer, I want categorical business fields to use explicit value sets so invalid textual values are rejected consistently.

**Why this priority**: This prevents silent data inconsistency and contract drift in the core scheduling domain.

**Independent Test**: Can be fully tested by sending create/update requests with valid and invalid category/status values and verifying deterministic acceptance/rejection behavior.

**Acceptance Scenarios**:

1. **Given** an endpoint receives a request with a supported categorical value, **When** the request is processed, **Then** the value is accepted and persisted as the corresponding domain classification.
2. **Given** an endpoint receives a request with an unsupported categorical value, **When** validation runs, **Then** the request is rejected with a machine-readable validation error and no state change.

---

### User Story 2 - Replace Generic Maps in API Contracts (Priority: P2)

As an API consumer, I want stable request and response structures so I can integrate without guessing dynamic key/value payloads.

**Why this priority**: Explicit payload structures reduce integration ambiguity and regressions across client applications.

**Independent Test**: Can be tested by invoking each affected endpoint and verifying request/response payloads conform to documented fields without generic map-shaped sections.

**Acceptance Scenarios**:

1. **Given** a consumer calls an affected endpoint, **When** the endpoint returns data, **Then** the response follows a documented object structure with explicit named fields.
2. **Given** a consumer sends data to an affected endpoint, **When** required fields are missing or malformed, **Then** the API returns deterministic validation errors that identify each invalid field.

---

### User Story 3 - Preserve Calendar Domain Behavior During Contract Hardening (Priority: P3)

As a parish operations user, I want calendar behavior to remain consistent while contracts become stricter, so event scheduling outcomes do not change unexpectedly.

**Why this priority**: Contract hardening must not introduce regressions in date, recurrence, participants, or ordering behavior.

**Independent Test**: Can be tested by re-running existing calendar flow scenarios (create, update, list, recurrence and participant operations) and verifying the same functional outcomes.

**Acceptance Scenarios**:

1. **Given** existing valid event workflows, **When** they are executed after the contract updates, **Then** the same business outcomes are produced.
2. **Given** historical data containing recognized categorical values, **When** it is read through updated endpoints, **Then** equivalent business meaning is preserved in API responses.

### Edge Cases

- Requests containing mixed casing or extra whitespace for categorical values must be normalized with trim + case-insensitive matching before enum resolution.
- Requests containing aliases, sinonimos ou valores localizados para campos categoricos must be rejected unless they match a documented canonical enum value after normalization.
- Requests containing null, empty, or omitted categorical values must return consistent validation outcomes based on field mandatory/optional rules.
- Legacy records with unsupported categorical values must remain readable, with the invalid field mapped to an explicit documented sentinel value and the inconsistency recorded in audit/observability outputs.
- Partial update operations must not overwrite unrelated categorical fields when one field fails validation.

## API Contract & Validation *(mandatory)*

- Affected endpoints: all create/update/read endpoints that currently expose or accept generic map payload sections or categorical string fields in calendar/event/project-related contracts.
- Contract status per endpoint:
- `POST`/`PUT`/`PATCH` endpoints with map-based request sections: changed.
- `GET` endpoints returning map-shaped sections: changed.
- Endpoints without categorical fields or map payloads: unchanged.
- Request/response expectations:
- Affected requests must define explicit field names, value domains, and required/optional semantics.
- Affected responses must expose deterministic field sets with stable meaning across releases.
- Validation rules:
- Categorical fields accept only documented values.
- Categorical enum parsing must apply trim + case-insensitive normalization at the API boundary and convert accepted values to the canonical documented enum representation.
- Legacy invalid categorical values read from persisted data must be exposed using the documented enum sentinel `UNKNOWN_LEGACY` for the affected field.
- Invalid categorical values produce a machine-readable validation code per field.
- Missing required fields produce a machine-readable required-field code.
- Unknown extra fields must be rejected in all changed requests with deterministic machine-readable validation errors.
- Backward compatibility and migration notes:
- This change is contract-tightening for endpoints previously accepting dynamic maps or arbitrary strings.
- Client migration guidance must include a before/after payload mapping and accepted categorical value catalog.
- A compatibility notice must be published for consumers relying on dynamic keys or unsupported values.

## Calendar Integrity Rules *(mandatory for calendar/event features)*

- Canonical timezone handling remains unchanged from the current calendar policy; contract hardening must not alter stored or returned temporal meaning.
- Date/time normalization behavior remains deterministic for create/update/list operations.
- Conflict detection (overlaps, duplicate slots, exceptional dates) must keep existing business rules unchanged.
- Retrieval ordering (including recurring and participant-related listings) must remain stable for identical query inputs.

## Operational Observability *(mandatory)*

- Each create/update/delete call on affected endpoints must emit traceable operation outcomes including request correlation id, endpoint id, and validation result class.
- Rejected requests due to categorical validation must be auditable with field-level error codes and safe context (without sensitive payload leakage).
- Logs must distinguish contract validation failures from business rule failures.
- Error diagnostics returned to clients must be specific enough for correction while avoiding internal structure exposure.

## Architecture and Code Standards *(mandatory)*

- Domain layer owns business classifications and allowed categorical values.
- Application layer owns use-case orchestration and contract-to-domain translation.
- Infrastructure/web adapter layer owns transport contract parsing/serialization.
- Hexagonal boundaries must prevent transport-specific structures from leaking into domain behavior.
- Endpoint contracts must use explicit schema objects instead of generic map payloads for domain-relevant data.
- Changed components must keep readable naming, bounded method complexity, and deterministic error mapping.
- Validation, transaction boundaries, and exception mapping must stay consistent with current project conventions.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST define an explicit allowed-value catalog for each categorical business field currently represented as free-form string in affected API contracts.
- **FR-002**: The system MUST reject unsupported categorical values with field-specific, machine-readable validation errors.
- **FR-003**: The system MUST accept all documented categorical values consistently across create, update, and retrieval flows, using trim + case-insensitive normalization at the API boundary and canonical enum representation internally.
- **FR-004**: The system MUST replace generic map-based request payload sections in affected endpoints with explicit structured contract objects.
- **FR-005**: The system MUST replace generic map-based response payload sections in affected endpoints with explicit structured contract objects.
- **FR-006**: The system MUST maintain existing business behavior for valid calendar/event/project operations after contract hardening.
- **FR-007**: The system MUST publish endpoint-level contract documentation for all changed payloads, including field-level semantics and allowed categorical values.
- **FR-008**: The system MUST provide migration guidance from previous dynamic-map payloads to the new explicit contract structures.
- **FR-009**: The system MUST preserve deterministic error response structure for validation failures across all changed endpoints.
- **FR-010**: The system MUST ensure that invalid values in one field do not cause unintended mutation of unrelated domain fields in the same request.
- **FR-011**: The system MUST reject any undocumented extra input field in changed request payloads instead of ignoring it silently.
- **FR-012**: The system MUST keep legacy records readable when a stored categorical value is no longer valid, exposing an explicit documented sentinel value for the affected field and emitting an auditable inconsistency signal.
- **FR-013**: The system MUST use the standardized sentinel enum member `UNKNOWN_LEGACY` for any affected categorical field exposed from legacy data with unsupported stored values.

### Key Entities *(include if feature involves data)*

- **Categorical Field Definition**: Represents a business field that is restricted to a finite value set, including field name, allowed values, and validation semantics.
- **Legacy Enum Sentinel**: Represents the standardized enum member `UNKNOWN_LEGACY` used when legacy persisted data contains a categorical value that is no longer supported.
- **Endpoint Contract Object**: Represents the explicit request/response shape for an endpoint, including field definitions, required/optional status, and value constraints.
- **Validation Error Item**: Represents a machine-readable error entry with field identifier, error code, and human-readable correction guidance.
- **Contract Migration Mapping**: Represents the mapping between previous dynamic map keys and the new explicit contract fields for client transition.

## Assumptions

- Existing endpoint URIs and primary business operations remain unchanged.
- Existing authentication and authorization behavior remains unchanged.
- The project has baseline contract/integration tests that can be extended to validate changed payload structures.
- Existing clients will receive versioned release notes and migration guidance before enforcement is activated in production.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of affected categorical fields reject unsupported values with deterministic machine-readable validation errors.
- **SC-002**: 100% of affected endpoints expose explicit request/response field structures with no generic map-shaped domain payload sections.
- **SC-003**: At least 95% of contract-related integration tests for affected endpoints pass on first run after migration updates.
- **SC-004**: 0 critical regressions are observed in core calendar flows (create, update, list, recurrence, participants) across acceptance testing for this feature.

## Measurement and Evidence Plan *(mandatory)*

- SC-001 evidence: contract and integration test cases covering valid/invalid categorical values per affected field, with captured error code assertions.
- SC-002 evidence: endpoint contract snapshots and automated schema assertions showing explicit fields for all changed payloads.
- SC-003 evidence: CI test report percentage for contract-focused suites tied to changed endpoints.
- SC-004 evidence: regression test report for core calendar scenarios before and after feature merge.
- Baseline cadence: capture one pre-change baseline and one post-change validation run during feature completion.
- Pull request evidence: include changed contract examples, validation test outputs including atomic rejection on invalid partial updates, a migration mapping summary for each changed endpoint, and the published compatibility notice for API consumers.

## Implementation Validation Summary

- Shared validation foundation completed with deterministic payloads and strict unknown-field rejection.
- User Story 1 validation passed for enum normalization, invalid enum rejection, atomic partial-update rejection, and event legacy sentinel exposure.
- User Story 2 validation passed for explicit DTO contracts on project, observation, participants, recurrence, and approval endpoints.
- User Story 3 validation passed for `UNKNOWN_LEGACY` projection on read, unknown-field rejection across migrated endpoints, and hardened regression coverage for public visibility, RBAC, invalid lifecycle checks, project linkage, and smoke flow.
- Consumer migration guidance was published in `compatibility-notice.md`.
