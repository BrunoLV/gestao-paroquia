# Implementation Plan: Observacoes de Evento com Controle de Tipo e Autoria

**Branch**: `008-observacoes-evento-crud` | **Date**: 2026-04-13 | **Spec**: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/008-observacoes-evento-crud/spec.md`
**Input**: Feature specification from `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/008-observacoes-evento-crud/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Implementar o CRUD funcional de observacoes de evento com separacao rigorosa entre notas manuais e observacoes sistêmicas. O endpoint manual deve aceitar apenas `NOTA`, suportar dois modos de listagem (`minhas` e `todas`), permitir edicao com historico de revisoes e exclusao logica apenas para a nota do proprio autor. Observacoes sistêmicas permanecem fora do contrato publico e passam a ser geradas automaticamente pelos fluxos de evento correspondentes, com autoria humana preferencial e fallback tecnico quando a execucao for automatica sem ator humano. A abordagem tecnica reutiliza o stack Java/Spring/JPA/Flyway existente, evolui a entidade de observacao atual de forma incremental, introduz trilha de revisao para `NOTA` e reforca auditabilidade por logs estruturados e projecoes funcionais deterministicas.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.3.13 (Web, Validation, Security, Data JPA, Actuator), Flyway 10.22.0, PostgreSQL driver  
**Storage**: PostgreSQL (schema `calendario`) em runtime; H2 em testes de integracao  
**Testing**: JUnit 5 + Spring Boot Test + Spring Security Test + MockMvc  
**Target Platform**: Linux server para API REST autenticada
**Project Type**: Web service backend em modulo unico Gradle (`app`)  
**Performance Goals**: manter p95 <= 2s para `POST /eventos/{eventoId}/observacoes`, `PATCH /eventos/{eventoId}/observacoes/{observacaoId}` e `GET /eventos/{eventoId}/observacoes` em ambiente de teste Tier 1 definido no quickstart  
**Constraints**: criacao manual limitada a `NOTA`; revisoes auditaveis obrigatorias; exclusao de `NOTA` apenas por soft delete; notas removidas ficam fora das listagens funcionais; sem persistencia parcial entre observacao e auditoria; codigos de erro deterministicos; autoria e `criadoEmUtc` obrigatorios em respostas  
**Scale/Scope**: escopo restrito ao subdominio de observacoes de evento, seus fluxos sistêmicos produtores, contratos REST do modulo `app` e suites de contrato/integracao/performance relacionadas

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- API Contract Gate: Identify all affected endpoints, request/response contracts, and validation/error
  semantics.
- Calendar Integrity Gate: Document date/time rules, conflict strategy, and ordering assumptions.
- Testability Gate: Map each user story to independent acceptance scenarios and executable test paths.
- Observability Gate: Define logging/audit impacts for create, update, and delete operations.
- Metrics Gate: Define measurable outcomes, instrumentation points, and baseline cadence for operations.
- Simplicity Gate: Justify any new dependency or architectural abstraction with requirement traceability.
- Architecture Gate: Prove clean architecture + hexagonal boundaries (core domain/application vs adapters).
- Java/Spring Gate: Confirm Java and Spring Boot best practices for DI, validation, transactions, and
  error handling.

### Pre-Design Gate Review

- API Contract Gate: PASS. Endpoints afetados identificados: `POST /eventos/{eventoId}/observacoes`, `GET /eventos/{eventoId}/observacoes`, `GET /eventos/{eventoId}/observacoes/minhas`, `PATCH /eventos/{eventoId}/observacoes/{observacaoId}` e `DELETE /eventos/{eventoId}/observacoes/{observacaoId}`; fluxos sistêmicos permanecem internos.
- Calendar Integrity Gate: PASS. Regras temporais e de ordenacao definidas: persistencia em UTC, projecao deterministica por `criadoEmUtc` + `id`, soft delete para `NOTA` e ocultacao funcional.
- Testability Gate: PASS. User stories P1..P3 possuem cenarios independentes cobrindo criacao/listagem manual, edicao/exclusao por autoria e geracao sistêmica automatica.
- Observability Gate: PASS. Spec exige logs estruturados para criacao, edicao, exclusao, listagem e geracao sistêmica, incluindo trilha de revisao e remocao logica.
- Metrics Gate: PASS. Success criteria SC-001..SC-012 definem cobertura objetiva para tipos manuais, autoria, listagem dual, soft delete, revisoes e autoria sistêmica.
- Simplicity Gate: PASS. Sem nova stack; a feature reutiliza entidade e repositorio existentes, adicionando apenas metadados e portas necessárias.
- Architecture Gate: PASS. Fronteiras clean/hexagonais permanecem nítidas entre `api`, `application`, `domain` e `infrastructure`.
- Java/Spring Gate: PASS. Solucao aderente a DI por construtor, Bean Validation, `@Transactional`, exception mapping deterministico e persistencia JPA/Flyway.

### Post-Design Gate Review

- API Contract Gate: PASS. Contrato detalhado em `contracts/calendar-api-observacoes-evento.openapi.yaml` formaliza os cinco endpoints publicos e a matriz de erros associada.
- Calendar Integrity Gate: PASS. `data-model.md` formaliza soft delete, revisoes de `NOTA`, autoria sistêmica e exclusao de removidas das projecoes funcionais.
- Testability Gate: PASS. `quickstart.md` mapeia suites focadas para criacao, listagem dual, edicao, soft delete, autoria sistêmica e regressao dos fluxos produtores.
- Observability Gate: PASS. Artefatos de design exigem correlação entre ator, observacao, fluxo de origem, revisao e remocao logica.
- Metrics Gate: PASS. Criterios e evidencias continuam mensuráveis para SC-001..SC-012 com coleta por suites e baseline operacional.
- Simplicity Gate: PASS. A decisao de evoluir `observacoes_evento` e adicionar trilha de revisao evita novos agregados artificiais.
- Architecture Gate: PASS. Design limita mudancas ao modulo `app`, separando fluxos manuais e sistêmicos em orquestracao de aplicacao e politicas de dominio.
- Java/Spring Gate: PASS. Persistencia incremental e testes com MockMvc/JPA/H2 permanecem coerentes com o baseline do projeto.

## Phase 0: Research Output

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/008-observacoes-evento-crud/research.md`

- Todas as ambiguidades relevantes foram consolidadas em decisoes de design rastreáveis aos requisitos.
- A separacao entre fluxo manual de `NOTA` e fluxo sistêmico reservado foi formalizada com impacto explícito em contrato, autorizacao e persistencia.
- Nao ha `NEEDS CLARIFICATION` remanescente.

## Phase 1: Design & Contracts Output

### Data Model

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/008-observacoes-evento-crud/data-model.md`

- Modelos centrais definidos para criacao manual, projecao funcional, dois modos de listagem, estado de remocao logica, historico de revisoes e comando interno de observacao sistêmica.
- Invariantes explicitas para autoria, tipos permitidos, visibilidade funcional de notas removidas e fallback tecnico de autoria sistêmica.

### Contracts

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/008-observacoes-evento-crud/contracts/calendar-api-observacoes-evento.openapi.yaml`

- Contrato delta formaliza criacao manual de `NOTA`, listagem `todas` e `minhas`, edicao com revisao, exclusao logica e matriz de erros deterministica.
- Fluxos sistêmicos permanecem fora do contrato publico e sao descritos como dependencias internas da feature.

### Quickstart

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/008-observacoes-evento-crud/quickstart.md`

- Sequencia objetiva para baseline, implementacao e validacao focada em observacoes manuais, ocultacao funcional de removidas, revisoes e geracao sistêmica automatica.

### Implementation Boundaries

- `api`: controllers e DTOs publicos para observacoes, incluindo novas operacoes `PATCH`, `DELETE` e modo `minhas`.
- `application`: use cases/servicos separados para nota manual, listagem funcional, revisao/exclusao e registro sistêmico.
- `domain`: politicas de tipo permitido, autoria, remocao logica, revisao e atribuicao de autoria sistêmica.
- `infrastructure`: JPA/Flyway para persistencia incremental, repositorios, auditoria operacional e produtores de logs.

### Deterministic Error Matrix

- `OBSERVACAO_TIPO_MANUAL_INVALIDO`: criacao manual com tipo diferente de `NOTA`.
- `OBSERVACAO_NAO_ENCONTRADA`: observacao inexistente para leitura mutacao ou exclusao.
- `OBSERVACAO_AUTOR_INVALIDO`: tentativa de editar/excluir nota sem autoria correspondente.
- `OBSERVACAO_TIPO_IMUTAVEL`: tentativa de mutar observacao sistêmica.
- `VALIDATION_REQUIRED_FIELD`: conteudo ausente/vazio ou payload invalido.
- `ACCESS_DENIED`: usuario sem permissao de leitura/colaboracao no evento.
- `CONFLICT`: concorrencia de revisao ou exclusao repetida sobre nota ja removida.

### Agent Context

- Script de sincronizacao a executar nesta fase: `.specify/scripts/bash/update-agent-context.sh copilot`
- Resultado esperado: contexto do agente atualizado com a feature de observacoes, persistencia incremental, revisoes de nota e separacao entre fluxos manual e sistêmico.

## Project Structure

### Documentation (this feature)

```text
specs/008-observacoes-evento-crud/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── calendar-api-observacoes-evento.openapi.yaml
└── tasks.md
```

### Source Code (repository root)

```text
app/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/nsfatima/calendario/
│   │   │       ├── api/
│   │   │       │   ├── controller/
│   │   │       │   └── dto/observacao/
│   │   │       ├── application/
│   │   │       │   └── usecase/observacao/
│   │   │       ├── domain/
│   │   │       │   └── type/
│   │   │       └── infrastructure/
│   │   │           ├── observability/
│   │   │           └── persistence/
│   │   │               ├── entity/
│   │   │               └── repository/
│   │   └── resources/
│   │       └── db/migration/
│   └── test/
│       ├── java/
│       │   └── br/com/nsfatima/calendario/
│       │       ├── contract/
│       │       ├── integration/
│       │       ├── performance/
│       │       └── support/
│       └── resources/

specs/
├── 001-parish-calendar-api/
├── 002-enum-fields-dto-endpoints/
├── 003-complete-event-creation/
├── 004-complete-event-patch/
├── 005-spring-security-rbac/
├── 006-cancelamento-evento/
├── 007-auto-approval-event-flow/
└── 008-observacoes-evento-crud/

gradle/
├── libs.versions.toml
└── wrapper/

settings.gradle.kts
gradle.properties
gradlew
gradlew.bat
```

**Structure Decision**: Manter o modulo unico `app` e concentrar a feature nos pacotes ja existentes de observacao, persistencia e auditoria. A extensao inclui novos endpoints/DTOs, use cases especializados, evolucao de entidade/migration e suites de contrato/integracao/performance, sem introduzir novo modulo para observacoes.

## Complexity Tracking

Sem violacoes constitucionais que exijam justificativa nesta fase.

## Evidence Verification (SC-001..SC-012)

| Criterion | Planned Evidence |
|-----------|------------------|
| SC-001 | Contract test para rejeicao de criacao manual com tipo diferente de `NOTA`. |
| SC-002 | Integration tests de edicao/exclusao por autor vs nao autor. |
| SC-003 | Contract/integration tests para imutabilidade manual de tipos sistêmicos. |
| SC-004 | Bateria combinada `*Observacao*`, `*CancelEvento*`, `*Aprovacao*`. |
| SC-005 | `ObservacaoTier1PerformanceTest` validando p95 <= 2s para create, edit e list em perfil de teste Tier 1. |
| SC-006 | Contract tests validando `usuarioId` e `criadoEmUtc` em create/list. |
| SC-007 | Integration tests verificando correspondencia entre texto/origem e observacao sistêmica persistida. |
| SC-008 | Integration tests cobrindo listagem `minhas` vs `todas`. |
| SC-009 | Repository/integration tests garantindo soft delete sem remocao fisica. |
| SC-010 | Integration/audit tests confirmando ocultacao funcional e recuperacao apenas interna. |
| SC-011 | Integration tests de autoria humana e fallback tecnico em fluxos sistêmicos. |
| SC-012 | Repository/integration tests validando persistencia de historico de revisoes. |

## Implementation Evidence Snapshot (2026-04-13)

- Fases Setup, Foundational, US1, US2 e US3 implementadas com checklist atualizado em `tasks.md`.
- Execucoes focadas validadas via Gradle para contrato/integracao de observacoes e fluxo sistêmico de cancelamento.
- Baseline operacional semanal de SC-005 definido com cadencia semanal (segunda-feira, janela UTC 09:00-10:00) usando `ObservacaoTier1PerformanceTest`.
- Comparativo pre x pos implementacao:
  - pre: pendente de coleta oficial na branch base de referencia operacional.
  - pos: coletado nesta feature e registrado no quickstart como baseline inicial de implementacao.
