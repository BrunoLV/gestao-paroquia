# Implementation Plan: Implement Project Management API Refinement

## Phase 1: DTO and Validation Refinement
- [ ] Task: Update Project DTOs with standard Bean Validation annotations
    - [ ] Add `@NotBlank` and `@Size(max = 100)` to `ProjetoCreateRequest`.
    - [ ] Add `@Size(min = 1, max = 100)` to `ProjetoPatchRequest`.
- [ ] Task: Add OpenAPI annotations to Project Controller and DTOs
    - [ ] Document all endpoints in `ProjetoController`.
    - [ ] Add `@Schema` descriptions to Project DTOs.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: DTO and Validation Refinement' (Protocol in workflow.md)

## Phase 2: Core Implementation
- [ ] Task: Implement actual logic for Project listing and patching
    - [ ] Update `ProjetoController.list()` to use a `ListProjetosUseCase`.
    - [ ] Update `ProjetoController.patch()` to use an `UpdateProjetoUseCase`.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Core Implementation' (Protocol in workflow.md)

## Phase 3: Integration Testing
- [ ] Task: Create comprehensive integration tests for Project API
    - [ ] Implement `ProjetoIntegrationTest` covering CRUD operations.
    - [ ] Verify validation and error handling for projects.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Integration Testing' (Protocol in workflow.md)
