# Implementation Plan: Gestão Administrativa de Usuários e Organizações

## Phase 1: Modularization & Core Infrastructure
- [ ] Task: Move `OrganizacaoEntity`, `MembroOrganizacaoEntity` and related repositories to `br.com.nsfatima.gestao.organizacao`.
- [ ] Task: Move `UsuarioEntity` and IAM related classes (Security fixtures, details service) to `br.com.nsfatima.gestao.iam`.
- [ ] Task: Update all imports across the project to reflect the new modular structure.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Modularization & Core Infrastructure' (Protocol in workflow.md)

## Phase 2: Organization Management (CRUD & Rules)
- [ ] Task: Implement `OrganizacaoRepository` and `OrganizacaoService` with complete CRUD.
- [ ] Task: Implement validation to block deletion of Organizations with active dependencies (Members/Events).
- [ ] Task: Create `OrganizacaoController` with secured endpoints (`ROLE_ADMIN`).
- [ ] Task: Write unit and integration tests for Organization management.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Organization Management (CRUD & Rules)' (Protocol in workflow.md)

## Phase 3: IAM Administrative Management (Users & Roles)
- [ ] Task: Implement `UsuarioService` actions: `createByAdmin`, `updateRoles`, `toggleActiveStatus`, `resetPassword`.
- [ ] Task: Implement delegation logic (Coordinators managing their own organization members).
- [ ] Task: Create `UsuarioAdminController` with secured endpoints.
- [ ] Task: Write unit and integration tests for administrative user management and delegation.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: IAM Administrative Management (Users & Roles)' (Protocol in workflow.md)

## Phase 4: Final Refinement & Security Verification
- [ ] Task: Ensure audit trail is captured for all administrative operations.
- [ ] Task: Perform a full security audit on the new endpoints.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Final Refinement & Security Verification' (Protocol in workflow.md)
