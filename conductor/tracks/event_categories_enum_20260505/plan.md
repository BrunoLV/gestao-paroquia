# Implementation Plan: Event Categories Enum

## Phase 1: Domain and Database Migrations
- [ ] Task: Create the new `CategoriaEvento` Enum in the domain package with the specified values (PASTORAL, SOCIAL, LITURGICO, ADMINISTRATIVO, SACRAMENTAL, FORMATIVO, ASSISTENCIAL) and JSON/String mapping utilities.
- [ ] Task: Create a Flyway migration to add a `categoria` VARCHAR column to the `eventos` table, drop the `categoria_id` foreign key and column, and drop the `categorias` table.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Domain and Database Migrations' (Protocol in workflow.md)

## Phase 2: Entity and Repository Refactoring
- [ ] Task: Delete `CategoriaEntity` and `CategoriaJpaRepository`.
- [ ] Task: Update `EventoEntity` to replace the `categoriaId` (UUID) field with `categoria` (String or Enum type). Update related getter/setter methods.
- [ ] Task: Update any custom queries in `EventoJpaRepository` or fake repositories used in tests that reference `categoriaId` to use the new `categoria` field.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Entity and Repository Refactoring' (Protocol in workflow.md)

## Phase 3: DTO and Service Refactoring
- [ ] Task: Update event-related DTOs (`CreateEventoRequest`, `UpdateEventoRequest`, `EventoResponse`, and any filter/search requests) to use `CategoriaEvento` or its string representation instead of `categoriaId`.
- [ ] Task: Refactor use cases (`CreateEventoUseCase`, `UpdateEventoUseCase`, etc.) and domain services to remove logic that fetches or validates the `CategoriaEntity` from the database.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: DTO and Service Refactoring' (Protocol in workflow.md)

## Phase 4: Test Updates and Final Verification
- [ ] Task: Update all unit and integration tests (e.g., `EventoController*Test`, fake repositories, use case tests) to align with the new DTO structure and the removal of the `categorias` table. Fix all compilation errors caused by the refactoring.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Test Updates and Final Verification' (Protocol in workflow.md)