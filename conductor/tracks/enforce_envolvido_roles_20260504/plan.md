# Implementation Plan: Enforce Envolvido Roles

## Phase 1: Domain and Infrastructure Alignment
- [x] Task: Create `PapelEnvolvido` enum in `br.com.nsfatima.calendario.domain.type`. e803f15
- [x] Task: Update `EventoEnvolvidoEntity` to use `PapelEnvolvido` enum. 296123a
- [ ] Task: Update `EventoEnvolvidoInput` DTO to use `PapelEnvolvido` enum.
- [ ] Task: Update `EventoEnvolvidosResponse` DTO to use `PapelEnvolvido` enum.
- [ ] Task: Update `UpdateEventoEnvolvidosUseCase` to handle the enum conversion.
- [ ] Task: Update tests to use the new enum and verify validation.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Domain and Infrastructure Alignment' (Protocol in workflow.md)
