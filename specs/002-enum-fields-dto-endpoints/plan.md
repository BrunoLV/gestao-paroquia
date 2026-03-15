# Implementation Plan: Enum Mapping and Endpoint DTO Contracts

**Branch**: `002-enum-fields-dto-endpoints` | **Date**: 2026-03-15 | **Spec**: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/spec.md`
**Input**: Feature specification from `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/spec.md`

## Summary

Endurecer os contratos HTTP do backend paroquial substituindo payloads dinamicos baseados em `Map` por DTOs explicitos e substituindo campos categoricos livres por enums com catalogos documentados. A solucao usa normalizacao `trim` + case-insensitive apenas na borda da API, rejeita campos extras nao documentados, preserva comportamento funcional do calendario e define exposicao controlada de `UNKNOWN_LEGACY` apenas em respostas quando dados persistidos antigos contiverem valores invalidos.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.3.5 (Web, Validation, Security, Data JPA, Actuator), Flyway, PostgreSQL driver  
**Storage**: PostgreSQL (schema `calendario`) com migrations Flyway; H2 para testes  
**Testing**: JUnit 5 + Spring Boot Test + Spring Security Test + suites de contrato/integracao existentes  
**Target Platform**: Linux server (API REST stateless)  
**Project Type**: Web service backend (single Gradle module `app`)  
**Performance Goals**: Manter p95 de consultas <= 2s; manter regressao de latencia de validacao abaixo de 10% no baseline semanal; manter 95%+ de sucesso nas suites de contrato afetadas  
**Constraints**: Preservar rotas e semantica de operacao; zero `Map` em contratos HTTP de dominio alterados; rejeicao deterministica de campos extras via request binding/serialization strict; normalizacao de entrada apenas `trim` + case-insensitive; `UNKNOWN_LEGACY` nunca aceito em requests; invalidacao de um campo em update parcial deve abortar a mutacao inteira; nenhuma escrita em tabelas externas; publicacao obrigatoria de compatibility notice para consumidores impactados  
**Scale/Scope**: Endpoints de projetos, observacoes, participacoes, recorrencia e aprovacoes atualmente baseados em `Map` e/ou `String` categorica no backend paroquial

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Design Gate Review

- API Contract Gate: PASS. Endpoints afetados foram identificados em `ProjetoController`, `ObservacaoController`, `EventoParticipacaoController` e `AprovacaoController`, com impacto explicito em request/response DTO e catalogos enum.
- Calendar Integrity Gate: PASS. Mudanca restringe contrato e validacao sem alterar timezone, ordenacao, recorrencia funcional ou estrategia de conflito.
- Testability Gate: PASS. US1/US2/US3 mapeiam para testes de aceitacao independentes cobrindo enum valido/invalido, rejeicao de campos extras, leitura de legado com `UNKNOWN_LEGACY` e regressao funcional.
- Observability Gate: PASS. Falhas de enum e campos extras continuam auditaveis com codigos deterministicos; leitura de legado invalido gera sinal auditavel sem falhar o recurso inteiro.
- Metrics Gate: PASS. SC-001..SC-004 seguem mensuraveis por suites de contrato/integracao e baseline comparativo pre/post.
- Simplicity Gate: PASS. Nenhuma dependencia nova e nenhuma abstracao injustificada; usa Spring MVC/Jackson/Bean Validation existentes.
- Architecture Gate: PASS. Conversao e normalizacao ficam na borda HTTP/DTO; dominio/aplicacao passam a trafegar tipos explicitos e nao estruturas dinamicas.
- Java/Spring Gate: PASS. Solucao aderente a Java 21 + Spring Boot com validacao declarativa, serializacao controlada e tratamento padrao de erros.

### Post-Design Gate Review

- API Contract Gate: PASS. `contracts/calendar-api-enum-dto.openapi.yaml` agora separa enums de entrada e resposta quando necessario e marca `additionalProperties: false` para requests alterados.
- Calendar Integrity Gate: PASS. `data-model.md` documenta que `UNKNOWN_LEGACY` serve apenas para exposicao de leitura legada, sem alterar estados de negocio do calendario.
- Testability Gate: PASS. `quickstart.md` cobre casos de normalizacao de enum, rejeicao de alias, rejeicao de campo extra, rejeicao atomica em update parcial, e leitura de dado legado com sentinela, alem das suites constitucionais obrigatorias de visibilidade publica, RBAC e lifecycle rejection.
- Observability Gate: PASS. `research.md` define rastreabilidade para valor legado inconsistente e distingue falha de contrato de falha de negocio.
- Metrics Gate: PASS. Plano inclui taxa de rejeicao valida, execucao de suites direcionadas e comparativo de regressao funcional.
- Simplicity Gate: PASS. Enums de entrada e resposta evitam overloading sem introduzir nova camada de complexidade fora da borda HTTP.
- Architecture Gate: PASS. DTOs/adaptadores absorvem parsing e normalizacao; core continua protegido de detalhes de transporte e legado.
- Java/Spring Gate: PASS. Design segue bind/validation padrao de Spring e mantem contratos previsiveis.

## Phase 0: Research Output

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/research.md`

- Todas as clarificacoes da spec foram consolidadas em decisoes tecnicas executaveis.
- Decisoes principais: DTO por endpoint, enums canonicos, normalizacao de entrada restrita, rejeicao de campos extras, estrategia `UNKNOWN_LEGACY` apenas para resposta de leitura.
- Resultado: nenhum `NEEDS CLARIFICATION` pendente.

## Phase 1: Design & Contracts Output

### Data Model

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/data-model.md`

- Modelo atualizado com `CampoCategorico`, `LegacyEnumSentinel`, `ErroValidacaoContrato` e `MapaMigracaoContrato`.
- Diferencia catalogos de entrada e de exposicao para evitar aceitar `UNKNOWN_LEGACY` em requests.

### Contracts

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/contracts/calendar-api-enum-dto.openapi.yaml`

- Delta OpenAPI cobre DTOs tipados para endpoints alterados.
- Requests usam `additionalProperties: false` e enums de entrada.
- Responses expostas para leitura legada usam enums de resposta com `UNKNOWN_LEGACY` quando aplicavel.

### Compatibility Notice

- Deliverable obrigatorio para implementacao: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/compatibility-notice.md`
- Deve resumir impacto de contrato, mapeamento antes/depois e comportamento de campos extras/enums para consumidores.

### Quickstart

Arquivo gerado: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/specs/002-enum-fields-dto-endpoints/quickstart.md`

- Fluxo reproduzivel para validar migracao `Map` -> DTO e `String` -> enum.
- Inclui cenarios obrigatorios de normalizacao, campo extra, valor invalido, rejeicao atomica em update parcial, legado com sentinela e regressao funcional constitucional.

### Agent Context

- Script de sincronizacao executado: `.specify/scripts/bash/update-agent-context.sh copilot`
- Arquivo alvo: `/home/bruno/DEV/WORKSPACES/calendario-paroquia/.github/agents/copilot-instructions.md`
- Contexto sincronizado com baseline Java/Spring e escopo de contratos DTO/enums.

## Project Structure

### Documentation (this feature)

```text
specs/002-enum-fields-dto-endpoints/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── calendar-api-enum-dto.openapi.yaml
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
│   └── test/
│       ├── java/
│       └── resources/
└── bin/

specs/
├── 001-parish-calendar-api/
└── 002-enum-fields-dto-endpoints/

gradle/
├── libs.versions.toml
└── wrapper/

gradle.properties
settings.gradle.kts
gradlew
gradlew.bat
```

**Structure Decision**: Projeto backend unico em modulo Gradle (`app`) com mudancas concentradas em contratos HTTP, DTOs, enums de dominio/aplicacao e tratamento de legado na borda de leitura.

## Complexity Tracking

Sem violacoes constitucionais que exijam justificativa nesta fase.
