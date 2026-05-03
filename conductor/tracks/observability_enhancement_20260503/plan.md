# Implementation Plan: Enhance Observability and Auditing

## Phase 1: API Documentation and DTO Refinement [checkpoint: 0302ffa]
- [x] Task: Document Auditing Controller and DTOs with OpenAPI [0302ffa]
    - [x] Add OpenAPI annotations to `AuditoriaEventoController`.
    - [x] Add `@Schema` descriptions to all metrics and audit response DTOs.
- [x] Task: Standardize filtering parameters [0302ffa]
    - [x] Create a consolidated DTO for audit trail filtering if appropriate.
- [x] Task: Conductor - User Manual Verification 'Phase 1: API Documentation and DTO Refinement' (Protocol in workflow.md)

## Phase 2: Auditing Integrity Verification
- [ ] Task: Verify audit log emission for core operations
    - [ ] Audit Event creation and updates.
    - [ ] Audit Project creation and updates.
    - [ ] Audit Approval decisions.
- [ ] Task: Implement missing audit logs if any are found during verification.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Auditing Integrity Verification' (Protocol in workflow.md)

## Phase 3: Integration Testing
- [ ] Task: Create integration tests for Auditing API
    - [ ] Implement `AuditoriaIntegrationTest` covering audit trail retrieval.
- [ ] Task: Create integration tests for Metrics API
    - [ ] Verify calculation logic for rework indicators and extra event rates.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Integration Testing' (Protocol in workflow.md)
