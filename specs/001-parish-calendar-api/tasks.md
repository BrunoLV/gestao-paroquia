# Tasks: API de Calendario Anual Paroquial

**Input**: Design documents from `/specs/001-parish-calendar-api/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar projeto Spring Boot, convenções arquiteturais e base de configuração.

- [X] T001 Converter dependências e plugins para stack Spring Boot em `app/build.gradle.kts`
- [X] T002 Criar bootstrap da aplicação web em `app/src/main/java/br/com/nsfatima/calendario/CalendarApiApplication.java`
- [X] T003 [P] Criar pacote de entrada HTTP em `app/src/main/java/br/com/nsfatima/calendario/api/.gitkeep`
- [X] T004 [P] Criar pacote de casos de uso em `app/src/main/java/br/com/nsfatima/calendario/application/.gitkeep`
- [X] T005 [P] Criar pacote de domínio em `app/src/main/java/br/com/nsfatima/calendario/domain/.gitkeep`
- [X] T006 [P] Criar pacote de infraestrutura em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/.gitkeep`
- [X] T007 Criar configuração base e profile local em `app/src/main/resources/application.yml`
- [X] T008 Definir matriz de códigos de erro determinísticos em `app/src/main/java/br/com/nsfatima/calendario/api/error/ErrorCodes.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Implementar pilares comuns de persistência, segurança, observabilidade e integridade.

**Critical**: Nenhuma user story começa antes desta fase.

- [X] T009 Criar migration baseline do schema de calendário em `app/src/main/resources/db/migration/V001__calendar_baseline.sql`
- [X] T010 [P] Implementar entidades base de localização e categoria em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/LocalEntity.java`
- [X] T011 [P] Implementar repositórios base de localização e categoria em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/LocalJpaRepository.java`
- [X] T012 [P] Implementar configuração de timezone UTC e saída America/Sao_Paulo em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/time/TimezoneConfig.java`
- [X] T013 [P] Implementar filtro de correlation-id e contexto de requisição em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/CorrelationIdFilter.java`
- [X] T014 [P] Implementar serviço de auditoria estruturada em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/AuditLogService.java`
- [X] T015 Implementar configuração base de autenticação/autorização em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/SecurityConfig.java`
- [X] T016 Implementar leitor read-only de vínculos externos em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/ExternalMembershipReader.java`
- [X] T017 Implementar mapeamento global de exceções para erros de negócio em `app/src/main/java/br/com/nsfatima/calendario/api/error/GlobalExceptionHandler.java`
- [X] T018 Implementar política de integridade de calendário (intervalo, ordenação, conflito) em `app/src/main/java/br/com/nsfatima/calendario/domain/policy/CalendarIntegrityPolicy.java`
- [X] T019 Implementar estratégia de concorrência otimista com versionamento em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/BaseVersionedEntity.java`
- [X] T020 Criar teste de integração para infraestrutura fundacional em `app/src/test/java/br/com/nsfatima/calendario/integration/foundation/FoundationInfrastructureTest.java`

**Checkpoint**: Foundation pronta; histórias liberadas para execução.

---

## Phase 3: User Story 1 - Gerir eventos do calendário (Priority: P1) 🎯 MVP

**Goal**: Entregar CRUD de eventos com soft delete, conflitos não bloqueantes, aprovação de alterações sensíveis e consistência concorrente.

**Independent Test**: Criar evento, listar por período, editar com aprovação, cancelar com autorização e validar comportamento sob concorrência.

### Tests for User Story 1

- [X] T021 [P] [US1] Criar teste de contrato para criar/listar eventos em `app/src/test/java/br/com/nsfatima/calendario/contract/EventosContractTest.java`
- [X] T022 [P] [US1] Criar teste de contrato para atualizar/cancelar evento em `app/src/test/java/br/com/nsfatima/calendario/contract/EventoMutacaoContractTest.java`
- [X] T023 [P] [US1] Criar teste de integração de conflito não bloqueante em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ConflitoAgendaIntegrationTest.java`
- [X] T024 [P] [US1] Criar teste de integração de fluxo de aprovação em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/AprovacaoHorarioIntegrationTest.java`
- [X] T025 [P] [US1] Criar teste de integração de concorrência otimista em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ConcorrenciaEventoIntegrationTest.java`

### Implementation for User Story 1

- [X] T026 [P] [US1] Implementar entidade persistente de evento com versionamento em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/EventoEntity.java`
- [X] T027 [P] [US1] Implementar repositório de eventos em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/EventoJpaRepository.java`
- [X] T028 [P] [US1] Implementar migration de eventos com `organizacao_responsavel_id` obrigatório em `app/src/main/resources/db/migration/V002__create_eventos.sql`
- [X] T029 [P] [US1] Implementar DTO de criação de evento em `app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/CreateEventoRequest.java`
- [X] T030 [P] [US1] Implementar DTO de atualização de evento em `app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/UpdateEventoRequest.java`
- [X] T031 [P] [US1] Implementar DTO de resposta de evento em `app/src/main/java/br/com/nsfatima/calendario/api/dto/evento/EventoResponse.java`
- [X] T032 [US1] Implementar casos de uso de criar/consultar/listar evento em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CreateEventoUseCase.java`
- [X] T033 [US1] Implementar casos de uso de atualizar/cancelar evento em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoUseCase.java`
- [X] T034 [US1] Implementar serviço de domínio de status, conflito e rejeição de `ADICIONADO_EXTRA` sem justificativa em `app/src/main/java/br/com/nsfatima/calendario/domain/service/EventoDomainService.java`
- [X] T035 [US1] Implementar endpoint REST de eventos em `app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoController.java`
- [X] T036 [US1] Implementar entidade de aprovação de alterações sensíveis em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/AprovacaoEntity.java`
- [X] T037 [US1] Implementar casos de uso de aprovação em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/aprovacao/CreateSolicitacaoAprovacaoUseCase.java`
- [X] T038 [US1] Implementar endpoint REST de aprovação em `app/src/main/java/br/com/nsfatima/calendario/api/controller/AprovacaoController.java`
- [X] T039 [US1] Implementar publicação de auditoria em mutações de evento em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/EventoAuditPublisher.java`

**Checkpoint**: US1 funcional e testável de forma independente (MVP).

---

## Phase 4: User Story 2 - Organizar eventos por projeto pastoral (Priority: P2)

**Goal**: Entregar gestão de projetos, vínculo evento-projeto, participantes por evento e recorrência, incluindo limpeza total de participantes.

**Independent Test**: Criar projeto, vincular/desvincular eventos, definir participantes, limpar totalmente participantes mantendo evento válido e criar recorrência de evento.

### Tests for User Story 2

- [X] T040 [P] [US2] Criar teste de contrato para criar/listar projetos em `app/src/test/java/br/com/nsfatima/calendario/contract/ProjetosContractTest.java`
- [X] T041 [P] [US2] Criar teste de contrato para atualizar projeto em `app/src/test/java/br/com/nsfatima/calendario/contract/ProjetoMutacaoContractTest.java`
- [X] T042 [P] [US2] Criar teste de contrato para atualizar participantes do evento em `app/src/test/java/br/com/nsfatima/calendario/contract/EventoParticipantesContractTest.java`
- [X] T043 [P] [US2] Criar teste de contrato para limpar participantes via `DELETE /eventos/{eventoId}/participantes` em `app/src/test/java/br/com/nsfatima/calendario/contract/EventoParticipantesLimpezaContractTest.java`
- [X] T044 [P] [US2] Criar teste de contrato para recorrência de eventos em `app/src/test/java/br/com/nsfatima/calendario/contract/EventoRecorrenciaContractTest.java`
- [X] T045 [P] [US2] Criar teste de integração de vínculo/desvínculo de projeto em `app/src/test/java/br/com/nsfatima/calendario/integration/projetos/VinculoProjetoEventoIntegrationTest.java`
- [X] T046 [P] [US2] Criar teste de integração para limpeza total de participantes preservando `organizacao_responsavel_id` em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ParticipantesLimpezaTotalIntegrationTest.java`

### Implementation for User Story 2

- [X] T047 [P] [US2] Implementar entidade de projeto em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/ProjetoEventoEntity.java`
- [X] T048 [P] [US2] Implementar repositório de projeto em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/ProjetoEventoJpaRepository.java`
- [X] T049 [P] [US2] Implementar migration de projetos em `app/src/main/resources/db/migration/V003__create_projetos_eventos.sql`
- [X] T050 [P] [US2] Implementar entidade de participantes de evento em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/EventoEnvolvidoEntity.java`
- [X] T051 [P] [US2] Implementar repositório de participantes de evento em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/EventoEnvolvidoJpaRepository.java`
- [X] T052 [P] [US2] Implementar migration de participantes de evento em `app/src/main/resources/db/migration/V004__create_eventos_envolvidos.sql`
- [X] T053 [P] [US2] Implementar entidade de recorrência de evento em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/EventoRecorrenciaEntity.java`
- [X] T054 [P] [US2] Implementar repositório de recorrência em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/EventoRecorrenciaJpaRepository.java`
- [X] T055 [P] [US2] Implementar migration de recorrência em `app/src/main/resources/db/migration/V005__create_eventos_recorrencia.sql`
- [X] T056 [US2] Implementar casos de uso de projeto em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/projeto/CreateProjetoUseCase.java`
- [X] T057 [US2] Implementar caso de uso de atualização de participantes em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/UpdateEventoParticipantesUseCase.java`
- [X] T058 [US2] Implementar caso de uso explícito de limpeza total de participantes em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/ClearEventoParticipantesUseCase.java`
- [X] T059 [US2] Implementar caso de uso de recorrência em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/CreateEventoRecorrenciaUseCase.java`
- [X] T060 [US2] Implementar endpoint REST de projetos em `app/src/main/java/br/com/nsfatima/calendario/api/controller/ProjetoController.java`
- [X] T061 [US2] Implementar endpoint REST de participantes e recorrência em `app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoParticipacaoController.java`
- [X] T062 [US2] Implementar endpoint `DELETE /eventos/{eventoId}/participantes` em `app/src/main/java/br/com/nsfatima/calendario/api/controller/EventoParticipacaoController.java`

**Checkpoint**: US1 e US2 funcionais e independentes.

---

## Phase 5: User Story 3 - Controlar acesso hierárquico e rastrear observações (Priority: P3)

**Goal**: Entregar enforcement de RBAC por organização responsável, observações append-only e auditoria da proporção ADICIONADO_EXTRA.

**Independent Test**: Bloquear perfis indevidos, permitir observações imutáveis com autoria e consultar indicador de ADICIONADO_EXTRA por período/organização.

### Tests for User Story 3

- [X] T063 [P] [US3] Criar teste de contrato para catálogo de papéis por organização com rejeição de `secretario` fora do Conselho em `app/src/test/java/br/com/nsfatima/calendario/contract/RoleCatalogContractTest.java`
- [X] T064 [P] [US3] Criar teste de integração de RBAC por organização com caso negativo de `secretario` fora do Conselho em `app/src/test/java/br/com/nsfatima/calendario/integration/security/RbacOrganizationIntegrationTest.java`
- [X] T065 [P] [US3] Criar teste de contrato para visibilidade pública por status (`RASCUNHO` oculto, `CONFIRMADO` visível) em `app/src/test/java/br/com/nsfatima/calendario/contract/PublicVisibilityContractTest.java`
- [X] T066 [P] [US3] Criar teste de integração para `CANCELADO` com motivo no histórico interno e observações append-only em `app/src/test/java/br/com/nsfatima/calendario/integration/observacoes/StatusAndHistoryIntegrationTest.java`
- [X] T067 [P] [US3] Criar teste de contrato para rejeitar `ADICIONADO_EXTRA` sem justificativa em `app/src/test/java/br/com/nsfatima/calendario/contract/AddedExtraValidationContractTest.java`

### Implementation for User Story 3

- [X] T068 [P] [US3] Implementar entidade de observação em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/ObservacaoEventoEntity.java`
- [X] T069 [P] [US3] Implementar repositório de observação em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/ObservacaoEventoJpaRepository.java`
- [X] T070 [P] [US3] Implementar migration de observações append-only em `app/src/main/resources/db/migration/V006__create_observacoes_evento.sql`
- [X] T071 [P] [US3] Implementar policy de autorização hierárquica com validação do catálogo por organização e regra de `secretario` exclusivo do Conselho em `app/src/main/java/br/com/nsfatima/calendario/domain/policy/AuthorizationPolicy.java`
- [X] T072 [US3] Implementar caso de uso de criação de observação append-only com vínculo de justificativa para `ADICIONADO_EXTRA` em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/CreateObservacaoUseCase.java`
- [X] T073 [US3] Implementar caso de uso de listagem de observações em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/observacao/ListObservacoesUseCase.java`
- [X] T074 [US3] Implementar caso de uso de auditoria de proporção ADICIONADO_EXTRA em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/evento/GetTaxaEventosExtraUseCase.java`
- [X] T075 [US3] Implementar endpoint REST de observações em `app/src/main/java/br/com/nsfatima/calendario/api/controller/ObservacaoController.java`
- [X] T076 [US3] Implementar endpoint REST de auditoria de ADICIONADO_EXTRA em `app/src/main/java/br/com/nsfatima/calendario/api/controller/AuditoriaEventoController.java`
- [X] T077 [US3] Implementar publicação de auditoria de observações e decisões em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/ObservacaoAuditPublisher.java`

**Checkpoint**: Todas as user stories funcionais e testáveis de forma independente.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Hardening, validação final e documentação transversal.

- [X] T078 [P] Atualizar contrato final da API com exemplos consistentes em `specs/001-parish-calendar-api/contracts/calendar-api.openapi.yaml`
- [X] T079 [P] Atualizar execução real de quickstart em `specs/001-parish-calendar-api/quickstart.md`
- [X] T080 Otimizar índices para consulta por período e status em `app/src/main/resources/db/migration/V007__indexes_eventos_periodo_status.sql`
- [X] T081 Endurecer regras finais de segurança com política anon vs auth em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/security/SecurityConfig.java`
- [X] T082 [P] Criar smoke test de jornada fim-a-fim em `app/src/test/java/br/com/nsfatima/calendario/integration/SmokeCalendarFlowTest.java`
- [X] T083 [P] Instrumentar e medir tempo de cadastro de evento (SC-001) em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/CadastroEventoMetricsPublisher.java`
- [X] T084 [P] Criar teste de carga para latência P95 de consulta por período (SC-002) em `app/src/test/java/br/com/nsfatima/calendario/performance/ConsultaCalendarioPerformanceTest.java`
- [X] T085 [P] Implementar indicador de retrabalho administrativo de calendário (SC-005) em `app/src/main/java/br/com/nsfatima/calendario/application/usecase/metrics/GetIndicadorRetrabalhoUseCase.java`
- [X] T086 [P] Criar job/rotina de baseline semanal das métricas SC-001/SC-002/SC-005 em `app/src/main/java/br/com/nsfatima/calendario/infrastructure/observability/WeeklyMetricsSnapshotJob.java`
- [X] T087 [P] Criar teste de integração do baseline semanal de métricas em `app/src/test/java/br/com/nsfatima/calendario/integration/metrics/WeeklyMetricsSnapshotIntegrationTest.java`
- [X] T088 [P] Atualizar guideline operacional de medição contínua em `specs/001-parish-calendar-api/quickstart.md`
- [X] T089 Validar checklist de conformidade constitucional e release em `specs/001-parish-calendar-api/checklists/release-readiness.md`
- [X] T090 [P] Criar teste de integração dedicado para visibilidade pública por status (`RASCUNHO` oculto e `CONFIRMADO` visível) em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/PublicStatusVisibilityIntegrationTest.java`
- [X] T091 [P] Criar teste de integração para validar `ACCESS_DENIED` com trilha de auditoria em escrita bloqueada (SC-003) em `app/src/test/java/br/com/nsfatima/calendario/integration/security/DeniedWriteAuditIntegrationTest.java`
- [X] T092 [P] Criar teste de integração dedicado para rejeitar `ADICIONADO_EXTRA` sem justificativa em `app/src/test/java/br/com/nsfatima/calendario/integration/eventos/AddedExtraValidationIntegrationTest.java`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: inicia imediatamente.
- **Phase 2 (Foundational)**: depende da Phase 1 e bloqueia todas as user stories.
- **Phase 3 (US1)**: depende da Phase 2.
- **Phase 4 (US2)**: depende da Phase 2 e dos artefatos de evento base de US1.
- **Phase 5 (US3)**: depende da Phase 2.
- **Phase 6 (Polish)**: depende das histórias concluídas.

### User Story Dependencies

- **US1 (P1)**: sem dependência de outras histórias, inicia após Foundation.
- **US2 (P2)**: depende explicitamente dos artefatos de evento base (modelo/endpoint de eventos) entregues em Foundation+US1; inicia após Foundation com esses artefatos disponíveis.
- **US3 (P3)**: sem dependência de outras histórias, inicia após Foundation.

### Within Each User Story

- Escrever testes primeiro e validar falha inicial.
- Implementar entidades/migrations antes dos casos de uso.
- Implementar casos de uso antes dos controllers.
- Validar logs/auditoria antes do checkpoint de história.

## Parallel Opportunities

- Setup: T003-T006 podem ocorrer em paralelo.
- Foundational: T010-T014 podem ocorrer em paralelo.
- US1: T021-T025 em paralelo; T026-T031 em paralelo.
- US2: T040-T046 em paralelo; T047-T055 em paralelo.
- US3: T063-T067 em paralelo; T068-T071 em paralelo.
- Polish: T078, T079, T082, T083, T084, T085, T086, T090, T091 e T092 em paralelo.

## Parallel Example: User Story 1

```bash
Task T021: app/src/test/java/br/com/nsfatima/calendario/contract/EventosContractTest.java
Task T022: app/src/test/java/br/com/nsfatima/calendario/contract/EventoMutacaoContractTest.java
Task T023: app/src/test/java/br/com/nsfatima/calendario/integration/eventos/ConflitoAgendaIntegrationTest.java
Task T024: app/src/test/java/br/com/nsfatima/calendario/integration/eventos/AprovacaoHorarioIntegrationTest.java

Task T026: app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/EventoEntity.java
Task T027: app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/repository/EventoJpaRepository.java
Task T028: app/src/main/resources/db/migration/V002__create_eventos.sql
```

## Parallel Example: User Story 2

```bash
Task T042: app/src/test/java/br/com/nsfatima/calendario/contract/EventoParticipantesContractTest.java
Task T043: app/src/test/java/br/com/nsfatima/calendario/contract/EventoParticipantesLimpezaContractTest.java
Task T050: app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/EventoEnvolvidoEntity.java
Task T053: app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/EventoRecorrenciaEntity.java
```

## Parallel Example: User Story 3

```bash
Task T063: app/src/test/java/br/com/nsfatima/calendario/contract/RoleCatalogContractTest.java
Task T067: app/src/test/java/br/com/nsfatima/calendario/contract/AddedExtraValidationContractTest.java
Task T068: app/src/main/java/br/com/nsfatima/calendario/infrastructure/persistence/entity/ObservacaoEventoEntity.java
Task T071: app/src/main/java/br/com/nsfatima/calendario/domain/policy/AuthorizationPolicy.java
```

## Implementation Strategy

### MVP First (US1)

1. Concluir Setup.
2. Concluir Foundational.
3. Concluir US1.
4. Validar critérios independentes da US1.
5. Publicar MVP interno.

### Incremental Delivery

1. Setup + Foundational.
2. Entregar US1.
3. Entregar US2.
4. Entregar US3.
5. Finalizar com Polish e checklist de release.

### Parallel Team Strategy

1. Time fecha Setup + Foundational.
2. Depois divide por história:
   - Dev A: US1
   - Dev B: US2
   - Dev C: US3
3. Integrar por história com contrato + integração.

## Notes

- Tasks com `[P]` não conflitam entre si por arquivo/dependência imediata.
- Tasks com `[US#]` garantem rastreabilidade por história.
- Não vincular eventos diretamente a usuário.
- Manter tabelas externas em modo somente leitura.
