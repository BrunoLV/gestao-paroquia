# Implementation Plan: Identity and Access Management (IAM) API

## Phase 1: Data Model & Persistence
- [ ] Task: Create `UsuarioEntity` and `MembroOrganizacaoEntity` mapping to existing tables.
- [ ] Task: Create `UsuarioJpaRepository` and `MembroOrganizacaoJpaRepository`.
- [ ] Task: Implement unit tests for repositories.

## Phase 2: Domain Services & Use Cases
- [ ] Task: Implement `ManageUsuarioUseCase` for CRUD operations.
- [ ] Task: Implement `ManageMembershipUseCase` for organization associations.
- [ ] Task: Add validation for organization IDs and types.

## Phase 3: API Layer & Security
- [ ] Task: Create `UsuarioController` with the specified endpoints.
- [ ] Task: Implement `AuthController.getCurrentUser()` (`/api/v1/auth/me`).
- [ ] Task: Apply security constraints to new endpoints.

## Phase 4: Verification
- [ ] Task: Create integration tests for User/Membership management.
- [ ] Task: Verify that new users can log in and have correct authorities.
