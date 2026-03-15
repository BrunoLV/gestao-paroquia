# Implementation Plan: Criacao Completa de Evento

**Branch**: `003-complete-event-creation` | **Date**: 2026-03-15 | **Spec**: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/003-complete-event-creation/spec.md`
**Input**: Feature specification from `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/003-complete-event-creation/spec.md`

## Summary

Substituir o fluxo mockado de eventos por fluxo real de criacao completa com persistencia em banco, validacao de regras de dominio na operacao de create, idempotencia explicita via `Idempotency-Key`, observabilidade auditavel e listagem autenticada baseada em dados persistidos. A implementacao permanece em Java 21 + Spring Boot 3.3.5 no modulo `app`, reaproveitando politicas de integridade existentes e mantendo compatibilidade operacional para fluxos ja ativos.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.3.5 (Web, Validation, Security, Data JPA, Actuator), Flyway, PostgreSQL driver  
**Storage**: PostgreSQL (`schema calendario`) para persistencia de eventos; H2 para testes  
**Testing**: JUnit 5 + Spring Boot Test + Spring Security Test + suites de contrato/integracao existentes  
**Target Platform**: Linux server (API REST stateless)  
**Project Type**: Web service backend (single Gradle module `app`)  
**Performance Goals**: preservar p95 de consulta em ate 2s (SC-004), manter criacao idempotente sem duplicidade em retries, reduzir tempo mediano de cadastro em 30% (SC-003)  
**Constraints**: `POST /eventos` exige `Idempotency-Key`; rejeicao de campos desconhecidos; `GET /eventos` autenticado; conflito de agenda nao bloqueante com `CONFLICT_PENDING`; persistencia UTC; regras de lifecycle e RBAC por escopo organizacional; janela de transicao do contrato antigo limitada a 2 releases minor ou 90 dias (o que ocorrer primeiro); sem regressao das suites constitucionais  
**Scale/Scope**: fluxo de criacao/listagem de eventos no contexto paroquial, com impacto principal em controller/usecase/repository/contratos HTTP e observabilidade

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Design Gate Review

- API Contract Gate: PASS. Endpoints impactados identificados (`POST /api/v1/eventos`, `GET /api/v1/eventos`) com requisitos de contrato, validacao e codigos de erro deterministas.
- Calendar Integrity Gate: PASS. Timezone canonico (UTC), validacao temporal (`fim > inicio`) e estrategia de conflito nao bloqueante estao definidos.
- Testability Gate: PASS. US1/US2/US3 possuem cenarios independentes com caminhos executaveis de contrato/integracao.
- Observability Gate: PASS. Criacao/listagem devem emitir trilha estruturada com correlation-id, ator, acao, alvo e resultado.
- Metrics Gate: PASS. SC-001..SC-004 possuem estrategia de medicao por testes e baseline operacional semanal.
- Simplicity Gate: PASS. Nao exige nova stack; usa Spring/JPA/Flyway existentes e abstrai apenas o necessario para idempotencia.
- Architecture Gate: PASS. Regras em `domain/application`; transporte e persistencia em `api/infrastructure`, mantendo fronteiras clean/hexagonal.
- Java/Spring Gate: PASS. Solucao aderente a DI, Bean Validation, transacao e mapeamento global de excecoes do projeto.

### Post-Design Gate Review

- API Contract Gate: PASS. Contrato delta documentado em `contracts/calendar-api-complete-event-create.openapi.yaml` com `Idempotency-Key`, `additionalProperties: false` e erros padronizados.
- Calendar Integrity Gate: PASS. `data-model.md` formaliza invariantes de tempo, status e marcador de conflito pendente.
- Testability Gate: PASS. `quickstart.md` define suites para create valido, idempotencia, conflito nao bloqueante, rejeicao de campos extras, compatibilidade legada, integridade organizacional, RBAC, listagem autenticada e visibilidade publica por status.
- Observability Gate: PASS. `research.md` explicita auditoria de sucesso/falha e rastreio de reuso de idempotencia.
- Metrics Gate: PASS. Plano inclui evidencia para SC-001..SC-004, incluindo baseline e pos-implementacao para SC-003 e SC-004 com criterio objetivo de aprovacao.
- Simplicity Gate: PASS. Sem novas dependencias obrigatorias; modelo de idempotencia permanece acoplado ao dominio de eventos.
- Architecture Gate: PASS. Design preserva separacao entre regra de negocio (core) e adaptadores HTTP/JPA.
- Java/Spring Gate: PASS. Estrategia permanece alinhada ao baseline Java 21 + Spring Boot 3.3.5.

## Phase 0: Research Output

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/003-complete-event-creation/research.md`

- Clarificacoes da spec foram convertidas em decisoes tecnicas executaveis.
- Decisoes chave: persistencia real no create/list, idempotencia por chave explicita, conflito nao bloqueante (`CONFLICT_PENDING`), RBAC por escopo e rejeicao de campos desconhecidos.
- Resultado: nenhum `NEEDS CLARIFICATION` pendente.

## Phase 1: Design & Contracts Output

### Data Model

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/003-complete-event-creation/data-model.md`

- Modelos centrais: `EventoCompleto`, `EventoCreateRequestCompleto`, `EventoConflito`, `EventoIdempotencyRecord`, `EventoAuditEntry`.
- Regras explicitadas: obrigatoriedade de `organizacaoResponsavelId`, validacao temporal, exigencia de justificativa em `ADICIONADO_EXTRA`, replay idempotente seguro.

### Contracts

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/003-complete-event-creation/contracts/calendar-api-complete-event-create.openapi.yaml`

- Delta OpenAPI para `POST/GET /eventos` com autenticacao obrigatoria e contrato estrito.
- `POST` exige `Idempotency-Key` e retorna erros deterministas para validacao, autorizacao e conflito de idempotencia.
- Request schema de create com `additionalProperties: false`.

### Quickstart

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/003-complete-event-creation/quickstart.md`

- Sequencia de execucao para baseline, implementacao e verificacao por suites focadas.
- Inclui smoke manual de idempotencia e validacao de listagem autenticada sem mock.

### Implementation Boundaries

- `api`: `EventoController`, DTOs e `GlobalExceptionHandler` expõem create/list autenticados com contrato estrito e erros deterministas.
- `application`: `CreateEventoUseCase`, `ListEventosUseCase` e `EventoIdempotencyService` orquestram transacao, replay idempotente e publicacao de auditoria/metricas.
- `domain`: `EventoDomainService` e `CalendarIntegrityPolicy` concentram validacoes temporais, integridade organizacional e resolucao de `CONFLICT_PENDING`.
- `infrastructure`: JPA, mapeadores, Flyway, seguranca e observabilidade persistem eventos/idempotencia, aplicam ordenacao deterministica e propagam correlation/idempotency ids.

### Deterministic Error Matrix

- `VALIDATION_ERROR`: campos obrigatorios ausentes, enum invalido, campos desconhecidos e trailing tokens.
- `DOMAIN_RULE_VIOLATION`: intervalo temporal invalido, `ADICIONADO_EXTRA` sem justificativa e integridade organizacao/participantes violada.
- `IDEMPOTENCY_KEY_CONFLICT`: reutilizacao de `Idempotency-Key` com payload divergente.
- `AUTH_REQUIRED` / `ACCESS_DENIED`: acesso sem autenticacao ou fora do escopo autorizado.

### Agent Context

- Script de sincronizacao executado: `.specify/scripts/bash/update-agent-context.sh copilot`
- Arquivo atualizado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/.github/agents/copilot-instructions.md`
- Resultado: contexto do agente sincronizado com stack Java 21 + Spring Boot 3.3.5 e escopo da feature.

## Project Structure

### Documentation (this feature)

```text
specs/003-complete-event-creation/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── calendar-api-complete-event-create.openapi.yaml
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
│   │   │       ├── application/
│   │   │       ├── domain/
│   │   │       └── infrastructure/
│   │   └── resources/
│   │       └── db/migration/
│   └── test/
│       ├── java/
│       │   └── br/com/nsfatima/calendario/
│       │       ├── contract/
│       │       ├── integration/
│       │       └── performance/
│       └── resources/

specs/
├── 001-parish-calendar-api/
├── 002-enum-fields-dto-endpoints/
└── 003-complete-event-creation/

gradle/
├── libs.versions.toml
└── wrapper/

settings.gradle.kts
gradle.properties
gradlew
gradlew.bat
```

**Structure Decision**: Manter projeto backend unico no modulo `app` e concentrar mudancas da feature nas camadas `api`, `application`, `domain` e `infrastructure`, com artefatos de planejamento em `specs/003-complete-event-creation`.

## Complexity Tracking

Sem violacoes constitucionais que exijam justificativa nesta fase.
