# Implementation Plan: Fechamento de Auditoria e Retrabalho

**Branch**: `009-audit-observability-closure` | **Date**: 2026-04-18 | **Spec**: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/009-audit-observability-closure/spec.md`
**Input**: Feature specification from `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/009-audit-observability-closure/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Fechar os gaps constitucionais de auditabilidade e observabilidade operacional do calendário paroquial, introduzindo persistência imutável e consulta operacional autenticada e autorizada para trilhas auditáveis por período e organização, além de operacionalizar o indicador de retrabalho administrativo como taxa consultável por período e organização. A abordagem técnica preserva o stack Java 21 + Spring Boot + JPA + Flyway existente, adiciona persistência estruturada de auditoria com comportamento fail-closed para mutações cobertas, define contratos REST específicos para leitura operacional e converte a instrumentação de retrabalho hoje apenas incremental em cálculo reproduzível com numerador, denominador e baseline semanal rastreável.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.3.13 (Web, Validation, Security, Data JPA, Actuator), Flyway 10.22.0, PostgreSQL driver  
**Storage**: PostgreSQL em runtime com migrations Flyway; H2 para testes de integração  
**Testing**: JUnit 5, Spring Boot Test, Spring Security Test, MockMvc, testes de contrato/integração/performance existentes no módulo `app`  
**Target Platform**: Serviço backend Spring Boot executado em Linux
**Project Type**: Web service backend em módulo Gradle único (`app`)  
**Performance Goals**: manter p95 <= 2s para `GET /api/v1/auditoria/eventos/trilha` e `GET /api/v1/auditoria/eventos/retrabalho` em validação Tier 1; preservar snapshot semanal das métricas constitucionais  
**Constraints**: `organizacaoId` obrigatório em ambos os endpoints novos; período informado por granularidade ou `inicio/fim`, nunca ambos; gravação auditável obrigatória com fail-closed; sem quebra de contratos REST existentes; erros determinísticos com `correlationId`; ordenação determinística da trilha; sem novas dependências externas  
**Scale/Scope**: escopo restrito ao módulo `app`, cobrindo controladores de auditoria, casos de uso de métricas, serviços de observabilidade, persistência JPA/Flyway e suites focadas de contrato/integração/performance

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

- API Contract Gate: PASS. Endpoints afetados identificados: `GET /api/v1/auditoria/eventos/extras` (mantido), `GET /api/v1/auditoria/eventos/trilha` (novo) e `GET /api/v1/auditoria/eventos/retrabalho` (novo). Regras de erro, filtros obrigatórios e compatibilidade aditiva estão declaradas na spec.
- Calendar Integrity Gate: PASS. A feature não altera semântica de agenda ou lifecycle; apenas torna auditáveis as mutações já permitidas e exige timestamps canônicos e ordenação determinística.
- Testability Gate: PASS. As user stories P1..P3 possuem cenários independentes e critérios mensuráveis cobrindo consulta auditável, cálculo de retrabalho, evidência operacional e regressões mandatórias de visibilidade pública por status e transições inválidas de lifecycle nos fluxos existentes impactados.
- Observability Gate: PASS. A spec exige persistência auditável estruturada, consulta por período/organização, `correlationId`, outcomes normalizados e fail-closed para falhas de gravação obrigatória.
- Metrics Gate: PASS. A feature define sucesso mensurável para trilha auditável, taxa de retrabalho, erros determinísticos e latência de consulta, além de baseline semanal.
- Simplicity Gate: PASS. A solução reutiliza Spring, JPA, Flyway e infraestrutura de observabilidade já existentes, sem introduzir dependências novas.
- Architecture Gate: PASS. O desenho pretendido separa domínio/aplicação de persistência e transporte, respeitando os pacotes `api`, `application`, `domain` e `infrastructure` do repositório.
- Java/Spring Gate: PASS. O plano permanece aderente a DI por construtor, validação declarativa, transações explícitas, JPA/Flyway e mapeamento centralizado de exceções.

### Post-Design Constitution Check

- API Contract Gate: PASS. Contrato inicial em `contracts/calendar-api-audit-observability.openapi.yaml` formaliza parâmetros, respostas e matriz de erros dos endpoints novos.
- Calendar Integrity Gate: PASS. `data-model.md` mantém UTC, ordenação determinística e não introduz mudanças em conflitos de agenda ou lifecycle funcional.
- Testability Gate: PASS. `quickstart.md` mapeia suites focadas para trilha auditável, cálculo do retrabalho, erro fail-closed, performance de leitura e regressões mandatórias de compatibilidade para visibilidade pública por status e lifecycle inválido.
- Observability Gate: PASS. O design faz a trilha auditável sair do log não estruturado como fonte primária e passar a existir como persistência consultável e imutável.
- Metrics Gate: PASS. O cálculo da taxa de retrabalho, o numerador/denominador e a cadência de baseline semanal foram explicitados.
- Simplicity Gate: PASS. A solução incremental evolui o módulo atual e privilegia tabela/repositório dedicados à auditoria e projeção de retrabalho sem nova stack.
- Architecture Gate: PASS. O design mantém portas e adaptadores claros para escrita/leitura auditável e casos de uso específicos para consulta operacional.
- Java/Spring Gate: PASS. O plano preserva `@Transactional`, validação HTTP, repositórios JPA, migrations Flyway e handler global determinístico.

## Phase 0: Research Output

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/009-audit-observability-closure/research.md`

- Todas as ambiguidades relevantes da spec foram resolvidas em clarificações formais e consolidadas em decisões de design rastreáveis.
- A feature adota persistência auditável imutável como fonte consultável e usa o indicador de retrabalho como taxa por organização.
- Não há `NEEDS CLARIFICATION` remanescente para a fase de design.

## Phase 1: Design & Contracts Output

### Data Model

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/009-audit-observability-closure/data-model.md`

- Modelos centrais definidos para registro auditável persistido, filtro de consulta, janela operacional, cálculo da taxa de retrabalho e snapshot semanal de métricas.
- Invariantes explícitas para imutabilidade, escopo organizacional obrigatório, ordenação determinística, exclusão mútua entre granularidade e intervalo explícito e fail-closed de gravação auditável.

### Contracts

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/009-audit-observability-closure/contracts/calendar-api-audit-observability.openapi.yaml`

- Contrato delta formaliza os endpoints `GET /api/v1/auditoria/eventos/trilha` e `GET /api/v1/auditoria/eventos/retrabalho`, além da permanência compatível de `GET /api/v1/auditoria/eventos/extras`.
- A matriz de erros cobre ausência de `organizacaoId`, definição ambígua de período, acesso fora do escopo, falha de persistência auditável e indisponibilidade da fonte operacional.

### Quickstart

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/009-audit-observability-closure/quickstart.md`

- Sequência objetiva para baseline, implementação, smoke manual e validação focada em trilha auditável, retrabalho, rollback fail-closed e evidência de performance.

### Implementation Boundaries

- `api`: ampliar `AuditoriaEventoController`, DTOs de auditoria/métricas e contratos de validação HTTP.
- `application`: novos casos de uso para consulta da trilha auditável e cálculo/consulta da taxa de retrabalho, além de ajuste do placeholder atual.
- `domain`: definições de regra para elegibilidade de retrabalho, invariantes de imutabilidade e semântica de filtros operacionais.
- `infrastructure`: nova persistência auditável via JPA/Flyway, repositórios de leitura, adaptação de `AuditLogService`/publishers e manutenção do snapshot semanal.

### Compatibility and Historical Evidence Boundaries

- A feature deve preservar e revalidar explicitamente os contratos existentes impactados, com regressões automatizadas para visibilidade pública por status e rejeição de transições inválidas de lifecycle.
- A evidência operacional final deve comprovar que a baseline semanal continua preservando histórico suficiente para comparação de tendências entre snapshots consecutivos ou evidência equivalente.

### Deterministic Error Matrix

- `VALIDATION_REQUIRED_FIELD`: ausência de `organizacaoId`, `periodo`, `inicio` ou `fim` quando obrigatórios.
- `VALIDATION_FIELD_INVALID`: granularidade desconhecida, intervalo inválido ou combinação de filtros não suportada.
- `ACCESS_DENIED`: tentativa de consultar organização fora do escopo do usuário autenticado.
- `AUTH_REQUIRED`: acesso sem autenticação aos endpoints operacionais.
- `DOMAIN_RULE_VIOLATION`: cálculo ou consulta solicitando modo incompatível com a regra declarada.
- `AUTHZ_SOURCE_UNAVAILABLE`: indisponibilidade temporária da fonte de autorização necessária para validar o escopo.
- `CONFLICT` ou código dedicado de auditoria: falha de persistência auditável obrigatória impedindo confirmação de mutação.

### Agent Context

- Script executado nesta fase: `.specify/scripts/bash/update-agent-context.sh copilot`
- Resultado esperado: contexto do agente atualizado com a feature de auditoria consultável, taxa de retrabalho e persistência fail-closed.

## Project Structure

### Documentation (this feature)

```text
specs/009-audit-observability-closure/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── calendar-api-audit-observability.openapi.yaml
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
│   │   │       │   ├── dto/metrics/
│   │   │       │   └── error/
│   │   │       ├── application/
│   │   │       │   ├── usecase/evento/
│   │   │       │   ├── usecase/metrics/
│   │   │       │   └── usecase/observacao/
│   │   │       ├── domain/
│   │   │       │   ├── policy/
│   │   │       │   └── service/
│   │   │       └── infrastructure/
│   │   │           ├── observability/
│   │   │           ├── persistence/entity/
│   │   │           ├── persistence/repository/
│   │   │           └── security/
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
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── gradlew.bat
```

**Structure Decision**: Manter o módulo único `app` e implementar a feature como evolução incremental dos componentes existentes de auditoria, métricas e persistência. A nova capacidade se concentra em `api/controller`, `api/dto/metrics`, `application/usecase/metrics`, `infrastructure/observability`, `infrastructure/persistence/entity`, `infrastructure/persistence/repository` e `db/migration`, sem introduzir novo módulo ou stack paralela.

## Complexity Tracking

Sem violações constitucionais que exijam justificativa nesta fase.

## Evidence Verification (SC-001..SC-005)

| Criterion | Planned Evidence |
|-----------|------------------|
| SC-001 | Testes de integração e contrato que executem mutações reais e consultem a trilha auditável persistida para eventos, aprovações e observações. |
| SC-002 | Testes de integração com múltiplas organizações cobrindo escopo permitido, escopo negado e ausência de vazamento entre organizações. |
| SC-003 | Testes determinísticos do cálculo da taxa de retrabalho com numerador/denominador explícitos e smoke do endpoint consolidado. |
| SC-004 | Teste de performance Tier 1 para os endpoints `trilha` e `retrabalho`, validando p95 <= 2000ms. |
| SC-005 | Testes de contrato e integração para ausência de `organizacaoId`, período ambíguo, indisponibilidade da persistência auditável e rollback fail-closed. |

### Constitutional Regression Evidence

- Regressões automatizadas devem cobrir explicitamente a preservação da visibilidade pública por status nos fluxos existentes impactados.
- Regressões automatizadas devem cobrir explicitamente a rejeição de transições inválidas de lifecycle nos fluxos existentes impactados.
- A validação final deve comprovar retenção histórica suficiente da baseline semanal para comparação de tendência entre snapshots consecutivos ou evidência equivalente.

## Implementation Evidence Snapshot (2026-04-18)

- Baseline atual do repositório: `./gradlew cleanTest :app:test` retornou `BUILD SUCCESSFUL`, com 144 testes, 0 falhas, 0 erros e 0 ignorados.
- Situação identificada antes da implementação:
  - `AuditLogService` grava apenas logs estruturados, sem persistência consultável.
  - `AuditoriaEventoController` expõe apenas `GET /api/v1/auditoria/eventos/extras`.
  - `GetIndicadorRetrabalhoUseCase` retorna placeholder fixo `0.0`.
  - `CadastroEventoMetricsPublisher` já coleta `administrative_rework_indicator`, mas ainda sem projeção operacional consultável.
- Risco principal controlado pelo plano: introduzir auditabilidade consultável sem quebrar contratos existentes e sem permitir sucesso funcional sem trilha persistida.

## Final Evidence Update (2026-04-18)

- Implementacao concluida no codigo para:
  - persistencia imutavel da trilha auditavel via `V017__create_auditoria_operacao.sql`
  - consulta autenticada/autorizada de `GET /api/v1/auditoria/eventos/trilha`
  - consulta autenticada/autorizada de `GET /api/v1/auditoria/eventos/retrabalho`
  - fail-closed com `AUDIT_PERSISTENCE_REQUIRED` para mutacoes auditaveis obrigatorias
  - snapshot semanal enriquecido com payload consistente de retrabalho e historico de snapshots
- Ajustes finais de consistencia realizados:
  - publishers de evento e observacao agora propagam `resourceType`, `resourceId`/ids auxiliares e `correlationId` quando disponivel
  - calculo de retrabalho passou a usar consultas agregadas na trilha auditavel persistida
  - fluxo de cancelamento inclui metadata organizacional e do evento para reforcar a persistencia auditavel obrigatoria

### SC-001..SC-005 Status

| Criterion | Status | Evidence |
|-----------|--------|----------|
| SC-001 | PASS | `AuditoriaInfrastructureIntegrationTest`, `AuditTrailQueryIntegrationTest`, `AuditoriaEventosContractTest` |
| SC-002 | PASS | `AuditTrailAuthorizationIntegrationTest`, `AuditTrailQueryIntegrationTest` |
| SC-003 | PASS | `IndicadorRetrabalhoContractTest`, `IndicadorRetrabalhoIntegrationTest`, `IndicadorRetrabalhoZeroIntegrationTest` |
| SC-004 | PASS | `AuditoriaTier1PerformanceTest` |
| SC-005 | PASS | `PeriodoOperacionalValidationIntegrationTest`, `AuditPersistenceFailureIntegrationTest`, `AuditRollbackConsistencyIntegrationTest` |

### Final Command Evidence

```bash
./gradlew :app:compileJava :app:compileTestJava
./gradlew :app:test --tests 'br.com.nsfatima.calendario.integration.foundation.*' --tests 'br.com.nsfatima.calendario.contract.AuditoriaEventosContractTest' --tests 'br.com.nsfatima.calendario.integration.auditoria.*' --tests 'br.com.nsfatima.calendario.contract.IndicadorRetrabalhoContractTest' --tests 'br.com.nsfatima.calendario.integration.metrics.IndicadorRetrabalho*' --tests 'br.com.nsfatima.calendario.integration.metrics.WeeklyMetricsSnapshotIntegrationTest' --tests 'br.com.nsfatima.calendario.integration.metrics.WeeklyMetricsSnapshotHistoryIntegrationTest' --tests 'br.com.nsfatima.calendario.performance.AuditoriaTier1PerformanceTest' --tests 'br.com.nsfatima.calendario.contract.EventoStatusVisibilityRegressionContractTest' --tests 'br.com.nsfatima.calendario.integration.evento.EventoLifecycleTransitionRegressionIntegrationTest'
```

Resultado observado: `BUILD SUCCESSFUL`.
