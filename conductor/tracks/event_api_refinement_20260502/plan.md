# Implementation Plan: Implement Event API Refinement and Validation

## Phase 1: Validation Refinement [checkpoint: de5411d]
- [x] Task: Update Event DTOs with standard Bean Validation annotations [25e14a5]
    - [x] Add `@NotBlank`, `@NotNull`, and `@Size` to `CreateEventoRequest`, `UpdateEventoRequest`, and related classes.
    - [x] Add `@Valid` to nested objects and collections.
- [x] Task: Implement custom validation for Event business rules [277c7d5]
    - [x] Create a validator to ensure `dataFim` is after `dataInicio`.
    - [x] Implement validation for conflicting event times (initial check).
- [x] Task: Conductor - User Manual Verification 'Phase 1: Validation Refinement' (Protocol in workflow.md)

## Phase 2: Error Handling and API Documentation [checkpoint: 6ef686f]
- [x] Task: Refine Global Exception Handler for validation errors [7ad8152]
    - [x] Ensure `MethodArgumentNotValidException` is handled and returns a structured response.
    - [x] Test the error response format with various validation failures.
- [x] Task: Add OpenAPI annotations to Event controllers [e5b088d]
    - [x] Document request/response schemas and possible error codes for `EventoController`.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Error Handling and API Documentation' (Protocol in workflow.md)

## Phase 3: Integration Testing
- [ ] Task: Create comprehensive integration tests for Event creation
    - [ ] Implement `EventoIntegrationTest` using `@SpringBootTest` and `MockMvc`.
    - [ ] Test successful creation and various validation failure scenarios.
- [ ] Task: Create integration tests for Event status transitions
    - [ ] Verify that status transitions follow the defined business rules and permissions.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Integration Testing' (Protocol in workflow.md)
