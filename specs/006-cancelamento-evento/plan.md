# Implementation Plan: Cancelamento de Evento

**Branch**: `006-cancelamento-evento` | **Date**: 2026-04-12 | **Spec**: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/006-cancelamento-evento/spec.md`
**Input**: Feature specification from `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/006-cancelamento-evento/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Implementar o cancelamento real de eventos `CONFIRMADO` via soft delete com motivo obrigatório, respeitando RBAC por papel e escopo organizacional, com dois caminhos: efetivação imediata para pároco e liderança do conselho, e criação de solicitação pendente para vigário e liderança de pastoral/laicato. Quando a aprovação for exigida, a própria decisão `APROVADA` deve executar automaticamente a ação pendente sem ressubmissão. Nesta entrega, essa execução automática está delimitada ao fluxo de cancelamento de evento. A abordagem técnica reaproveita o stack Java/Spring/JPA/Flyway existente, estende o subdomínio de aprovação para armazenar snapshot da ação pendente e reforça rastreabilidade por auditoria estruturada e observações append-only.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.3.5 (Web, Validation, Security, Data JPA, Actuator), Flyway 10.20.1  
**Storage**: PostgreSQL (schema `calendario`) em runtime; H2 em testes de integração  
**Testing**: JUnit 5 + Spring Boot Test + Spring Security Test + MockMvc  
**Target Platform**: Linux server para API REST com autenticação por sessão  
**Project Type**: Web service backend em módulo único Gradle (`app`)  
**Performance Goals**: manter cancelamento direto com tempo operacional <= 2s (p95) na carga normal e manter decisão de aprovação (`PATCH /api/v1/aprovacoes/{id}`) com p95 <= 2s na carga normal  
**Constraints**: somente eventos `CONFIRMADO` podem ser cancelados; soft delete obrigatório; motivo obrigatório; execução automática após aprovação sem ressubmissão; sem persistência parcial; códigos de erro determinísticos; trilha auditável obrigatória; timezone canônico já adotado pela aplicação  
**Scale/Scope**: escopo restrito ao lifecycle de cancelamento de `Evento`, ao subdomínio de `Aprovacao` associado e à observabilidade/auditoria do fluxo; sem UI e sem operações em lote

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Design Gate Review

- API Contract Gate: PASS. Endpoints afetados identificados: `DELETE /api/v1/eventos/{eventoId}` para cancelamento/solicitação e `PATCH /api/v1/aprovacoes/{id}` para decidir e executar automaticamente a ação pendente.
- Calendar Integrity Gate: PASS. Regras de lifecycle definidas: apenas `CONFIRMADO -> CANCELADO`; soft delete preservando histórico, vínculos e motivo.
- Testability Gate: PASS. User stories P1..P4 possuem cenários independentes cobrindo cancelamento direto, fluxo pendente com aprovação automática, negação por papel e auditoria.
- Observability Gate: PASS. Spec exige logs estruturados para solicitação, aprovação, reprovação, efetivação e falha de efetivação, além de observação append-only do tipo `CANCELAMENTO`.
- Metrics Gate: PASS. Success criteria definem persistência, rejeição correta, auditoria, aprovação automática e visibilidade histórica como evidências mensuráveis.
- Simplicity Gate: PASS. Sem nova stack ou dependência externa; extensão do fluxo de aprovação é rastreável aos requisitos da feature.
- Architecture Gate: PASS. Responsabilidades previstas entre controller, use cases, políticas de domínio, persistência JPA e observabilidade preservam limites clean/hexagonais.
- Java/Spring Gate: PASS. Solução aderente a DI por construtor, `@Transactional`, Bean Validation, exception mapping centralizado e persistência JPA/Flyway.

### Post-Design Gate Review

- API Contract Gate: PASS. Contrato detalhado em `contracts/calendar-api-event-cancellation.openapi.yaml` define respostas `200/202`, decisão de aprovação e matriz de erros específica.
- Calendar Integrity Gate: PASS. `data-model.md` formaliza a transição única `CONFIRMADO -> CANCELADO`, snapshot de ação pendente e falha segura quando a pré-condição deixa de valer antes da execução automática.
- Testability Gate: PASS. `quickstart.md` mapeia testes focados para cancelamento direto, solicitação pendente, aprovação com execução automática, reprovação, erro de escopo e erro de status.
- Observability Gate: PASS. Artefatos de design exigem correlação entre solicitação, decisão e execução com `correlationId`, `solicitacaoAprovacaoId`, `atorId` e `aprovadorId`.
- Metrics Gate: PASS. Critérios e evidências continuam mensuráveis para SC-001..SC-007 com coleta por suites e baseline operacional.
- Simplicity Gate: PASS. A decisão de armazenar o snapshot da ação na própria infraestrutura de aprovação evita abstrações extras sem perder rastreabilidade.
- Architecture Gate: PASS. Design limita mudanças ao módulo `app` e separa fluxo imediato e fluxo pendente em orquestração de aplicação, não em adaptadores HTTP.
- Java/Spring Gate: PASS. Execução automática após aprovação permanece compatível com transações Spring e handlers determinísticos.

## Phase 0: Research Output

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/006-cancelamento-evento/research.md`

- Todas as ambiguidades relevantes foram resolvidas em decisões de design rastreáveis aos requisitos.
- O fluxo de aprovação foi consolidado para criar solicitação pendente na ação original e efetivar automaticamente a ação após aprovação.
- Não há `NEEDS CLARIFICATION` remanescente.

## Phase 1: Design & Contracts Output

### Data Model

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/006-cancelamento-evento/data-model.md`

- Modelos centrais definidos para requisição de cancelamento, solicitação pendente com snapshot, execução automática, observação de cancelamento e trilha operacional.
- Invariantes explícitas para escopo organizacional, autoridade por papel, status elegível e reaproveitamento proibido de solicitação reprovada.

### Contracts

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/006-cancelamento-evento/contracts/calendar-api-event-cancellation.openapi.yaml`

- Contrato delta formaliza o comportamento bifurcado de `DELETE /eventos/{eventoId}` (`200` ou `202`) e a execução automática embutida em `PATCH /aprovacoes/{id}`.
- Erros determinísticos e payloads de observabilidade/decisão estão explicitados para clientes e suporte.

### Quickstart

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/006-cancelamento-evento/quickstart.md`

- Sequência objetiva para baseline, implementação e validação focada em cancelamento direto, autorização pendente e aprovação com efetivação automática.

### Implementation Boundaries

- `api`: controllers e DTOs recebem `DELETE /eventos/{id}` e `PATCH /aprovacoes/{id}`, validam o contrato HTTP e delegam a use cases.
- `application`: `CancelEventoUseCase` decide entre cancelamento imediato e criação de solicitação pendente; fluxo de decisão de aprovação executa automaticamente a ação pendente em transação controlada.
- `domain`: políticas de autorização, elegibilidade de status, invariantes do lifecycle e regras de escopo organizacional permanecem no core.
- `infrastructure`: JPA/Flyway persistem evento, aprovação e observação; segurança resolve ator e escopo; observabilidade registra trilha estruturada.

### Deterministic Error Matrix

- `VALIDATION_ERROR`: motivo ausente/vazio/tamanho inválido ou payload/decisão inválidos.
- `EVENT_NOT_FOUND`: evento alvo inexistente para `DELETE /eventos/{id}`.
- `FORBIDDEN`: papel sem permissão de solicitar ou decidir a ação.
- `INVALID_STATUS_TRANSITION`: evento não está `CONFIRMADO` no momento da solicitação ou da execução automática.
- `APPROVAL_EXECUTION_FAILED`: aprovação registrada, mas a efetivação automática falhou por violação de pré-condição ou corrida de estado.
- `APPROVAL_NOT_FOUND` / `APPROVAL_ALREADY_DECIDED`: falhas do endpoint de decisão quando aplicável ao contrato final do recurso de aprovação.

### Deterministic Non-Error Outcomes

- `APPROVAL_PENDING`: resultado assíncrono de sucesso (`202 Accepted`) quando a solicitação pendente é criada e aguarda decisão.

### Agent Context

- Script de sincronização a executar nesta fase: `.specify/scripts/bash/update-agent-context.sh copilot`
- Resultado esperado: contexto do agente atualizado com a stack já confirmada e a nova convenção de fluxo pendente com execução automática.

## Project Structure

### Documentation (this feature)

```text
specs/006-cancelamento-evento/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── calendar-api-event-cancellation.openapi.yaml
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
│       │       ├── infrastructure/
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
└── 006-cancelamento-evento/

gradle/
├── libs.versions.toml
└── wrapper/

settings.gradle.kts
gradle.properties
gradlew
gradlew.bat
```

**Structure Decision**: Manter o módulo único `app` e concentrar as mudanças da feature nos subdomínios de eventos, aprovação e auditoria já existentes, preservando as fronteiras clean/hexagonais e evitando introduzir um novo módulo apenas para workflow de autorização.

## Complexity Tracking

Sem violações constitucionais que exijam justificativa nesta fase.

## Evidence Verification (SC-001..SC-007)

| Criterion | Status | Evidence |
|-----------|--------|----------|
| SC-001 | PASS | `CancelEventoImmediateIntegrationTest` + `ApproveCancelEventoIntegrationTest` confirmam efetivação do cancelamento em fluxo direto e pós-aprovação. |
| SC-002 | PASS | `CancelEventoAuthorizationIntegrationTest` + `CancelEventoOrganizationScopeIntegrationTest` validam rejeição por papel/escopo sem mutação persistida. |
| SC-003 | PASS | `CancelEventoImmediateIntegrationTest` + `CancelEventoObservacaoIntegrationTest` comprovam observação append-only do tipo `CANCELAMENTO`. |
| SC-004 | PASS | `ApproveCancelEventoIntegrationTest` + `CancelEventoAuditTrailIntegrationTest` validam auditoria do aprovador na efetivação automática. |
| SC-005 | PASS | `CancelEventoAuditTrailIntegrationTest` + `CancelEventoApprovalExecutionFailureIntegrationTest` cobrem trilha de solicitação/decisão/execução/falha. |
| SC-006 | PASS | `CancelEventoTier1PerformanceTest` valida latência de cancelamento direto `<= 2000ms` em Tier 1. |
| SC-007 | PASS | `CancelledEventoVisibilityIntegrationTest` + `CancelEventoHistoricalLinksPreservationIntegrationTest` validam visibilidade/histórico do cancelado. |

### Run Evidence Snapshot

- Focused cancellation battery: `BUILD SUCCESSFUL` em 2026-04-12.
- Medição SC-006 executada por teste dedicado de performance Tier 1 com assert de limiar `<= 2s`.

