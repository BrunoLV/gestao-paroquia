# Implementation Plan: Evento Recorrencia Flow

## Phase 1: Data Model and Yearly Job
- [x] Task: Define the Custom JSON schema for recurrence rules and update `EventoEntity` (or a dedicated entity) to map it. 86ee4ea
- [x] Task: Implement the Spring `@Scheduled` job that runs on January 1st to parse rules and generate `EventoEntity` instances for the year. 1e6d540
- [~] Task: Integrate a concurrency control mechanism (e.g., database lock) to ensure the yearly job is cluster-safe and runs only once.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Data Model and Yearly Job' (Protocol in workflow.md)

## Phase 2: Edit Scopes Implementation
- [ ] Task: Implement 'Only This Instance' logic: allowing edits to a generated instance while correctly detaching it from the base recurrence rule.
- [ ] Task: Implement 'This and Following' logic: allowing an edit to split the recurrence rule, terminating the old one and starting a new one.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Edit Scopes Implementation' (Protocol in workflow.md)

## Phase 3: Core Rules Integration
- [ ] Task: Verify and ensure that all generated recurring event instances correctly trigger and pass the standard event validations, authorization policies, and approval workflows.
- [ ] Task: Create robust integration tests for both edit scopes and the yearly generation job.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Core Rules Integration' (Protocol in workflow.md)
