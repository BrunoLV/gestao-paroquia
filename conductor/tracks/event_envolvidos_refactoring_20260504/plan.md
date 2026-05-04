# Implementation Plan: Refactor Event Participation to Envolvidos

## Phase 1: Padronização do Conceito de "Envolvidos"
- [x] Task: Create `EventoEnvolvidoInput` DTO and update `EventoEnvolvidosRequest` and `EventoEnvolvidosResponse`. e4e594c
- [ ] Task: Rename and refactor `UpdateEventoParticipantesUseCase` to `UpdateEventoEnvolvidosUseCase`, including support for participation roles.
- [ ] Task: Rename and refactor `ClearEventoParticipantesUseCase` to `ClearEventoEnvolvidosUseCase`.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Padronização do Conceito de "Envolvidos"' (Protocol in workflow.md)

## Phase 2: Decomposição do Controller
- [ ] Task: Create `EventoEnvolvidoController` and migrate involvement endpoints.
- [ ] Task: Create `EventoRecorrenciaController` and migrate recurrence endpoints.
- [ ] Task: Delete `EventoParticipacaoController`.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Decomposição do Controller' (Protocol in workflow.md)

## Phase 3: Limpeza e Alinhamento
- [ ] Task: Update existing tests to reflect the new naming and endpoint structure.
- [ ] Task: Ensure `EventoEnvolvidoEntity` and repository are fully aligned with the new DTOs.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Limpeza e Alinhamento' (Protocol in workflow.md)
