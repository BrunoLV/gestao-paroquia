# Implementation Plan: Full Implementation of Project Concept

## Phase 1: Data Model and Infrastructure Evolution
- [x] Task: SQL Migration to add `organizacao_responsavel_id`, `inicio_utc`, and `fim_utc` to `projetos_eventos`. 152dc69
- [ ] Task: Update `ProjetoEventoEntity` with new fields and JPA mappings.
- [ ] Task: Update `ProjetoCreateRequest` and `ProjetoResponse` DTOs.
- [ ] Task: Refactor `CreateProjetoUseCase` to validate dates and persist the responsible organization.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Data Model and Infrastructure Evolution' (Protocol in workflow.md)

## Phase 2: Establishing the Link and Constraints
- [ ] Task: SQL Migration to add `projeto_id` to `eventos` table.
- [ ] Task: Update `EventoEntity` and event-related DTOs.
- [ ] Task: Implement `ProjetoIdValidationPolicy` to ensure project existence and status.
- [ ] Task: Enforce recurrence exclusion rule in event creation and project linking.
- [ ] Task: Implement temporal consistency validation (event dates within project dates).
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Establishing the Link and Constraints' (Protocol in workflow.md)

## Phase 3: Governance and Visibility
- [ ] Task: Implement `ProjetoAuthorizationService` (actor-based security).
- [ ] Task: Update `ProjetoController` to use the new authorization service.
- [ ] Task: Add project-based filtering to event list API.
- [ ] Task: Include project details in event responses.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Governance and Visibility' (Protocol in workflow.md)
