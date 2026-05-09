# Implementation Plan: Modularização do Calendário

## Phase 1: Módulo de Observabilidade & Suporte Técnico [checkpoint: 52914]
- [x] Task: Criar pacote `br.com.nsfatima.gestao.observabilidade`.
- [x] Task: Mover `AuditoriaOperacaoEntity`, `JobLockEntity` e repositórios associados para o novo módulo.
- [x] Task: Mover `AuditLogPersistenceService` e ajustar as injeções em todo o projeto.
- [x] Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md)

## Phase 2: Módulo de Governança Paroquial [checkpoint: 56182]
- [x] Task: Criar pacote `br.com.nsfatima.gestao.governanca`.
- [x] Task: Mover `AnoParoquialEntity`, `AnoParoquialRepository` e `AnoParoquialController`.
- [x] Task: Mover serviços associados (`AnoParoquialAuthorizationService`, etc.).
- [x] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)

## Phase 3: Módulo de Gestão de Projetos
- [ ] Task: Criar pacote `br.com.nsfatima.gestao.projeto`.
- [ ] Task: Mover `ProjetoEventoEntity` e repositórios.
- [ ] Task: Extrair a lógica de agregação (`ProjetoAgregacaoService`) garantindo que as dependências do `EventoRepository` sejam injetadas ou resolvidas via interface/events.
- [ ] Task: Mover controllers de Projeto.
- [ ] Task: Conductor - User Manual Verification 'Phase 3' (Protocol in workflow.md)

## Phase 4: Módulo de Workflow de Aprovação
- [ ] Task: Criar pacote `br.com.nsfatima.gestao.aprovacao`.
- [ ] Task: Mover `AprovacaoEntity`, repositórios e a base de `ApprovalExecutionStrategy`.
- [ ] Task: Refatorar o acoplamento forte entre Aprovação e Evento, mantendo as implementações específicas de execução de eventos dentro do módulo de eventos, mas a engine principal no módulo de aprovação.
- [ ] Task: Mover os casos de uso relacionados a decisão e listagem de aprovações.
- [ ] Task: Conductor - User Manual Verification 'Phase 4' (Protocol in workflow.md)
