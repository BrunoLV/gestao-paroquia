# Implementation Plan: Implement Project Management API Refinement

## Phase 1: DTO and Validation Refinement
- [x] Task: Update Project DTOs with standard Bean Validation annotations [48fba23]
    - [x] Add `@NotBlank` and `@Size(max = 160)` to `ProjetoCreateRequest`.
    - [x] Add `@Size(min = 1, max = 160)` to `ProjetoPatchRequest`.
- [x] Task: Add OpenAPI annotations to Project Controller and DTOs [2f815f7]
    - [x] Document all endpoints in `ProjetoController`.
    - [x] Add `@Schema` descriptions to Project DTOs.
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
