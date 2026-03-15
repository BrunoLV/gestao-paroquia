# Implementation Plan: PATCH Completo de Evento

**Branch**: `004-complete-event-patch` | **Date**: 2026-03-15 | **Spec**: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/004-complete-event-patch/spec.md`
**Input**: Feature specification from `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/004-complete-event-patch/spec.md`

## Summary

Substituir o fluxo mockado de PATCH por atualizacao parcial real com persistencia transacional no banco, aplicacao de regras de dominio e autorizacao alinhada ao create, incluindo regras especificas para participantes e troca de organizacao responsavel, mais aprovacao obrigatoria para alteracoes de data e cancelamento. A implementacao deve manter contratos deterministas de erro, trilha auditavel e cobertura de testes de integracao para sucesso, negacoes por permissao e negacoes por ausencia de aprovacao.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.3.5 (Web, Validation, Security, Data JPA, Actuator), Flyway 10.20.1  
**Storage**: PostgreSQL (schema `calendario`) para runtime; H2 para testes de integracao  
**Testing**: JUnit 5 + Spring Boot Test + Spring Security Test + MockMvc  
**Target Platform**: Linux server (API REST stateless)
**Project Type**: Web service backend (single Gradle module `app`)  
**Performance Goals**: manter SC-003 (>=95% dos PATCH validos em ate 2s) e preservar p95 operacional de consultas/listagens sem degradacao relevante apos a mudanca  
**Constraints**: operacao PATCH com merge parcial deterministico; proibicao de persistencia parcial em falha; codigos de erro machine-readable; timezone canonico unico; lock otimista via `@Version`; sem quebra de contrato de rota/metodo  
**Scale/Scope**: escopo restrito ao lifecycle de atualizacao de `Evento` e subdominio de autorizacao/aprovacao relacionado; sem mudancas de UI e sem operacoes em lote

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Design Gate Review

- API Contract Gate: PASS. Endpoint principal afetado identificado (`PATCH /api/v1/eventos/{eventoId}`), com contrato delta para erros `EVENT_NOT_FOUND`, `VALIDATION_ERROR`, `BUSINESS_RULE_VIOLATION`, `FORBIDDEN`, `APPROVAL_REQUIRED`, `CONFLICT`.
- Calendar Integrity Gate: PASS. Regras temporais e de normalizacao UTC definidas, com bloqueio de alteracoes invalidas e regras de aprovacao para data/cancelamento.
- Testability Gate: PASS. Historias US1/US2/US3 possuem cenarios independentes e convertiveis em testes de integracao.
- Observability Gate: PASS. Requisito de auditoria para tentativa/sucesso/falha e metadados de aprovacao definido na spec.
- Metrics Gate: PASS. SC-001..SC-004 com plano de evidencia e baseline semanal definidos.
- Simplicity Gate: PASS. Sem introducao obrigatoria de nova stack; reaproveita Spring/JPA/Flyway e componentes de auditoria ja existentes.
- Architecture Gate: PASS. Separacao controller/usecase/domain/infrastructure preservada como restricao explicita.
- Java/Spring Gate: PASS. Planejamento aderente a DI por construtor, `@Transactional`, Bean Validation e exception mapping centralizado.

### Post-Design Gate Review

- API Contract Gate: PASS. Contrato de PATCH detalhado em `contracts/calendar-api-complete-event-patch.openapi.yaml`, incluindo campos de atualizacao parcial, erros deterministas e exemplos de aprovacao/permissao.
- Calendar Integrity Gate: PASS. `data-model.md` formaliza invariantes de intervalo, restricoes de participantes, troca de organizacao responsavel e condicoes de aprovacao.
- Testability Gate: PASS. `quickstart.md` mapeia suites para sucesso, autorizacao, aprovacao, concorrencia e ausencia de persistencia parcial em falhas.
- Observability Gate: PASS. Design exige publicacao auditavel de sucesso/falha com correlation id, ator e motivo.
- Metrics Gate: PASS. Evidencias planejadas para SC-001..SC-004 com rotina de coleta semanal.
- Simplicity Gate: PASS. Nenhuma dependencia nova obrigatoria alem de possivel migration faltante de aprovacao no mesmo stack existente.
- Architecture Gate: PASS. Distribuicao de responsabilidades mantida em camadas clean/hexagonal.
- Java/Spring Gate: PASS. Decisoes de implementacao preservam praticas recomendadas do baseline Java/Spring.

## Phase 0: Research Output

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/004-complete-event-patch/research.md`

- Todos os pontos de ambiguidade relevantes para implementacao foram resolvidos em decisoes tecnicas.
- Confirmado que o fluxo atual de PATCH e mockado e que autorizacao/aprovacao precisam ser efetivamente conectadas ao fluxo transacional.
- Sem `NEEDS CLARIFICATION` remanescente.

## Phase 1: Design & Contracts Output

### Data Model

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/004-complete-event-patch/data-model.md`

- Modelos centrais definidos para patch parcial, autorizacao por escopo/papel, aprovacao de alteracoes sensiveis e auditoria operacional.
- Invariantes explicitas para campos patchaveis, restricoes de participantes e mutacao condicionada da organizacao responsavel.

### Contracts

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/004-complete-event-patch/contracts/calendar-api-complete-event-patch.openapi.yaml`

- Contrato delta formaliza o comportamento alvo de `PATCH /eventos/{eventoId}` com payload parcial estrito e erros deterministas.
- Mantida compatibilidade de rota/metodo e explicitados cenarios de aprovacao/permissao.

### Quickstart

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/004-complete-event-patch/quickstart.md`

- Sequencia objetiva para baseline, implementacao e validacao por suites focadas em persistencia real, autorizacao e aprovacao.

### Implementation Boundaries

- `api`: controller e DTOs validam contrato HTTP e delegam a use cases sem conter regra de negocio.
- `application`: use case de update orquestra merge parcial, autorizacao, aprovacao, transacao e auditoria.
- `domain`: politicas de integridade de calendario e autorizacao permanecem no core.
- `infrastructure`: JPA/Flyway/observabilidade realizam persistencia, locking otimista e trilha operacional.

### Deterministic Error Matrix

- `VALIDATION_ERROR`: payload vazio, campo desconhecido, formato invalido e violacao de validacao de entrada.
- `BUSINESS_RULE_VIOLATION`: regras de dominio de calendario e integridade de relacionamento violadas.
- `FORBIDDEN`: usuario autenticado sem permissao para o tipo de mutacao solicitada.
- `APPROVAL_REQUIRED`: alteracao de data/cancelamento sem aprovacao valida.
- `EVENT_NOT_FOUND`: evento alvo inexistente.
- `CONFLICT`: conflito de concorrencia/versao em atualizacao simultanea.

### Agent Context

- Script de sincronizacao a executar nesta fase: `.specify/scripts/bash/update-agent-context.sh copilot`
- Resultado esperado: arquivo de contexto do agente atualizado sem perder customizacoes manuais entre marcadores.

## Project Structure

### Documentation (this feature)

```text
specs/004-complete-event-patch/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── calendar-api-complete-event-patch.openapi.yaml
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
├── 003-complete-event-creation/
└── 004-complete-event-patch/

gradle/
├── libs.versions.toml
└── wrapper/

settings.gradle.kts
gradle.properties
gradlew
gradlew.bat
```

**Structure Decision**: Manter modulo unico `app` com implementacao nas camadas existentes e limitar mudancas desta feature aos componentes de eventos, aprovacao, erro e observabilidade, preservando fronteiras clean/hexagonal.

## Complexity Tracking

Sem violacoes constitucionais que exijam justificativa nesta fase.
