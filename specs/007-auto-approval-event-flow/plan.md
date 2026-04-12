# Implementation Plan: Execucao Automatica Pos-Aprovacao para Criacao e Edicao de Evento

**Branch**: `007-auto-approval-event-flow` | **Date**: 2026-04-12 | **Spec**: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/007-auto-approval-event-flow/spec.md`
**Input**: Feature specification from `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/007-auto-approval-event-flow/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Implementar um fluxo assincorno de autorizacao para criacao e edicao sensivel de eventos, equivalente ao modelo ja adotado em cancelamento: o cliente submete a intencao uma vez, o sistema cria solicitacao pendente quando necessario, e apos decisao `APROVADA` a mutacao e executada automaticamente sem novo request de negocio. A entrega reaproveita o subdominio de aprovacao existente com snapshot da acao pendente (`CRIACAO_EVENTO`, `EDICAO_EVENTO`), reforca trilha auditavel e metricas, e preserva compatibilidade para atores com permissao de execucao imediata.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.3.5 (Web, Validation, Security, Data JPA, Actuator), Flyway 10.20.1  
**Storage**: PostgreSQL (schema `calendario`) em runtime; H2 em testes  
**Testing**: JUnit 5, Spring Boot Test, MockMvc, Spring Security Test  
**Target Platform**: Linux server para API REST stateful via sessao  
**Project Type**: Backend web-service em modulo unico Gradle (`app`)  
**Performance Goals**: efetivacao automatica pos-aprovacao para criacao/edicao em p95 <= 60s  
**Constraints**: sem reenvio manual do cliente apos aprovacao; sem mutacao parcial; codigos de erro deterministicos; aderencia a lifecycle e validacoes de dominio existentes; rastreabilidade obrigatoria  
**Scale/Scope**: escopo limitado ao fluxo de evento e aprovacao no modulo `app`, sem alteracoes de UI

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Design Gate Review

- API Contract Gate: PASS. Endpoints mapeados: `POST /api/v1/eventos`, `PATCH /api/v1/eventos/{eventoId}`, `PATCH /api/v1/aprovacoes/{id}`.
- Calendar Integrity Gate: PASS. Estrategia UTC, validacao de intervalo e regras de lifecycle permanecem obrigatorias tambem na execucao automatica.
- Testability Gate: PASS. Historias P1/P1/P2 tem cenarios independentes com prova de nao reenvio manual.
- Observability Gate: PASS. Trilha prevista para solicitacao, decisao, execucao e falha em criacao/edicao.
- Metrics Gate: PASS. Success criteria mensuraveis definidos para throughput, efetivacao automatica e falhas.
- Simplicity Gate: PASS. Sem nova stack; reaproveito do subdominio de aprovacao existente.
- Architecture Gate: PASS. Limites clean/hexagonal preservados entre API/application/domain/infrastructure.
- Java/Spring Gate: PASS. Fluxo permanece com DI por construtor, `@Transactional`, Bean Validation e exception mapping global.

### Post-Design Gate Review

- API Contract Gate: PASS. Contrato delta formalizado em `contracts/calendar-api-approval-execution.openapi.yaml` com outcomes `EXECUTED`, `REJECTED`, `FAILED` e `APPROVAL_PENDING`.
- Calendar Integrity Gate: PASS. `data-model.md` formaliza snapshot imutavel e revalidacao de dominio na efetivacao automatica.
- Testability Gate: PASS. `quickstart.md` inclui testes dedicados para criacao/edicao pendente, aprovacao automatica e falha segura.
- Observability Gate: PASS. Auditoria com correlation id e metadados de acao por tipo (`CRIACAO_EVENTO`, `EDICAO_EVENTO`).
- Metrics Gate: PASS. Indicadores de pending/sucesso/falha e latencia entre decisao e execucao definidos.
- Simplicity Gate: PASS. Solucao evita motor de workflow externo e reutiliza infraestrutura existente.
- Architecture Gate: PASS. Execucao automatica ocorre na camada de aplicacao, mantendo adaptadores HTTP e persistencia desacoplados.
- Java/Spring Gate: PASS. Sem violacoes de praticas Spring/Java para transacao, seguranca e handlers.

## Phase 0: Research Output

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/007-auto-approval-event-flow/research.md`

- Decisoes de desenho consolidam fluxo unico de solicitacao + decisao + execucao automatica para criacao e edicao.
- Definidas estrategias de idempotencia, concorrencia e falha segura no pos-aprovacao.
- Nao ha `NEEDS CLARIFICATION` remanescente.

## Phase 1: Design & Contracts Output

### Data Model

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/007-auto-approval-event-flow/data-model.md`

- Entidades de solicitacao/aprovacao e snapshots de acao pendente para criacao e edicao foram formalizadas.
- Invariantes de integridade, autorizacao por escopo e resultados de execucao padronizados foram explicitados.

### Contracts

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/007-auto-approval-event-flow/contracts/calendar-api-approval-execution.openapi.yaml`

- Contrato delta define respostas pendentes para criacao/edicao e detalha efetivacao automatica na decisao de aprovacao.
- Matriz de erro e outcomes funcionais especificados para consumo de clientes e operacao.

### Quickstart

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/007-auto-approval-event-flow/quickstart.md`

- Sequencia objetiva de implementacao e validacao de ponta a ponta para os fluxos de criacao e edicao com autorizacao.

### Implementation Boundaries

- `api`: controllers e DTOs expõem resposta pendente para criacao/edicao quando aplicavel e mantem contratos de mutacao imediata para perfis autorizados.
- `application`: use cases de evento criam snapshots pendentes; use case de decisao de aprovacao aplica automaticamente criacao/edicao aprovadas.
- `domain`: politicas de validacao de status, faixa temporal, participantes e escopo organizacional continuam fonte unica de verdade.
- `infrastructure`: persistencia JPA/Flyway para solicitacao e snapshot, auditoria estruturada e metricas operacionais.

### Deterministic Error Matrix

- `VALIDATION_ERROR`: payload invalido ou faltante.
- `APPROVAL_REQUIRED`: somente no caminho de compatibilidade em que o cliente tenta forcar execucao imediata de mutacao sensivel sem aprovacao valida; no modo pendente padrao nao deve ocorrer por ausencia de `aprovacaoId`.
- `APPROVAL_NOT_FOUND`: solicitacao/aprovacao inexistente.
- `APPROVAL_ALREADY_DECIDED`: decisao duplicada.
- `APPROVAL_EXECUTION_FAILED`: decisao aprovada registrada, mas efetivacao automatica nao concluida.
- `FORBIDDEN`: papel/escopo sem permissao para solicitar ou decidir.
- `EVENT_NOT_FOUND`: evento alvo inexistente para edicao automatica.
- `INVALID_STATUS_TRANSITION`: violacao de lifecycle no momento da efetivacao.
- `CONFLICT`: conflito otimista/de concorrencia sem resolucao automatica.

### Deterministic Non-Error Outcomes

- `APPROVAL_PENDING`: requisicao aceita com solicitacao pendente criada.
- `EXECUTED`: acao pendente aprovada e efetivada automaticamente.
- `REJECTED`: solicitacao decidida como reprovada sem mutacao no evento.

### Operational Metrics Baseline Coverage

- A implementacao deve manter cobertura explicita da baseline constitucional para `event_registration_lead_time_minutes`, `calendar_query_latency_ms` e `administrative_rework_indicator`, alem das metricas especificas do fluxo de aprovacao.

### Agent Context

- Script executado nesta fase: `.specify/scripts/bash/update-agent-context.sh copilot`
- Resultado esperado: contexto do agente atualizado com o padrao de execucao automatica pos-aprovacao para criacao/edicao.

## Project Structure

### Documentation (this feature)

```text
specs/007-auto-approval-event-flow/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── calendar-api-approval-execution.openapi.yaml
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
└── 007-auto-approval-event-flow/

gradle/
├── libs.versions.toml
└── wrapper/

settings.gradle.kts
gradle.properties
gradlew
gradlew.bat
```

**Structure Decision**: Manter modulo unico `app` e evoluir o fluxo de aprovacao existente para incluir criacao e edicao automatica pos-decisao, evitando novos modulos e preservando padrao arquitetural atual.

## Complexity Tracking

Sem violacoes constitucionais que exijam justificativa nesta fase.

## Evidencias de Implementacao (SC-001..SC-005)

### Snapshot de Evidencias

- SC-001 (criacao pendente + execucao automatica): coberto por `CreateEventoApprovalPendingIntegrationTest` e `ApproveCreateEventoIntegrationTest`.
- SC-002 (edicao pendente + execucao automatica): coberto por `UpdateEventoApprovalPendingIntegrationTest` e `ApproveUpdateEventoIntegrationTest`.
- SC-003 (p95 tempo decisao->execucao <= 60s): metrica `approval_execution_latency_ms` instrumentada e observada em execucoes de teste com valores na ordem de milissegundos.
- SC-004 (falha segura sem mutacao parcial): coberto por `ApprovalAutoExecutionFailureConsistencyIntegrationTest` e `UpdateEventoApprovalExecutionFailureIntegrationTest`.
- SC-005 (trilha auditavel de ponta a ponta): coberto por `CreateEventoApprovalAuditTrailIntegrationTest` e `UpdateEventoApprovalAuditTrailIntegrationTest`.

### Comandos Executados na Validacao Final

```bash
./gradlew :app:test \
	--tests 'br.com.nsfatima.calendario.integration.eventos.CreateEventoApprovalAuditTrailIntegrationTest' \
	--tests 'br.com.nsfatima.calendario.integration.eventos.UpdateEventoApprovalAuditTrailIntegrationTest' \
	--tests 'br.com.nsfatima.calendario.integration.eventos.ApprovalAutoExecutionFailureConsistencyIntegrationTest' \
	--tests 'br.com.nsfatima.calendario.contract.AprovacoesDecisionExecutionContractTest' \
	--tests 'br.com.nsfatima.calendario.integration.eventos.UpdateEventoApprovalExecutionFailureIntegrationTest'

./gradlew :app:test --tests 'br.com.nsfatima.calendario.integration.eventos.ApprovalFlowRegressionSuite'
```

Resultados observados: `BUILD SUCCESSFUL` nos dois blocos.
