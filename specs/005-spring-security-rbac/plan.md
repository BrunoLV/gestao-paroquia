# Implementation Plan: Spring Security RBAC com Usuário/Senha

**Branch**: `005-spring-security-rbac` | **Date**: 2026-03-15 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/005-spring-security-rbac/spec.md`

## Summary

Substituir a segurança atual (httpBasic stateless + cabeçalhos HTTP arbitrários para papel/escopo) por Spring Security 6.x com autenticação por usuário/senha, sessão stateful via cookie (`JSESSIONID`) e autorização derivada do banco compartilhado (`usuarios` + `membros_organizacao`). Todos os endpoints de negócio exigem autenticação; apenas `POST /api/v1/auth/login` permanece público para controle de sessão. Sessão expirada deve retornar `401 SESSION_EXPIRED` em JSON, sem redirecionamento HTTP.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.3.5, Spring Security 6.3.x, Spring Data JPA, Flyway, Spring Validation  
**Storage**: PostgreSQL (banco compartilhado, esquema `calendario`) e H2 em memória para testes  
**Testing**: JUnit 5, Spring Boot Test, Spring Security Test (`MockMvc`, `@WithMockUser`)  
**Target Platform**: Linux server (JVM)  
**Project Type**: web-service (REST API)  
**Performance Goals**: Sem nova meta de throughput; login é operação pontual e endpoints de leitura autenticados devem manter `calendar query latency` com alvo de **p95 ≤ 500ms** (SC-006)  
**Constraints**: Fail-closed obrigatório para indisponibilidade da fonte de autorização; payloads de domínio existentes devem permanecer compatíveis (FR-011)  
**Scale/Scope**: API paroquial de baixo volume; foco em hardening de segurança e consistência de autorização

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### API Contract Gate ✅
- Endpoints de controle de sessão definidos: `POST /api/v1/auth/login` (público) e `POST /api/v1/auth/logout` (autenticado)
- Endpoints de negócio em `/api/v1/**` exigem autenticação
- Erros de segurança padronizados em JSON (`AUTH_REQUIRED`, `AUTH_INVALID`, `ACCESS_DENIED`, `ROLE_SCOPE_INVALID`, `AUTHZ_SOURCE_UNAVAILABLE`, `SESSION_EXPIRED`)
- Sessão expirada definida explicitamente: HTTP 401 + `SESSION_EXPIRED` sem redirecionamento

### Calendar Integrity Gate ✅
- Regras de data/hora, conflito e ordenação não são alteradas
- Segurança atua apenas como gate de autorização, sem alterar regras de calendário

### Testability Gate ✅
- Cenários independentes por user story documentados no spec
- `quickstart.md` define suites por comportamento (auth, login/logout, RBAC, fail-closed, auditoria)
- `tasks.md` inclui tarefas de teste automatizado com critério fail-first para mudanças de contrato/validação

### Observability Gate ✅
- Operações negadas devem registrar endpoint, usuário, resultado e motivo
- Logs de falha de autenticação/autorização com códigos estáveis
- Fail-closed registra indisponibilidade sem vazamento de detalhe sensível
- Instrumentação e coleta de métricas operacionais também abrangem adapters em `infrastructure/observability`

### Metrics Gate ✅
- Métricas mínimas: taxa de negação por endpoint, taxa de falha de autenticação, volume de mutações autorizadas
- Instrumentação mínima constitucional: event registration lead time, calendar query latency e administrative rework indicator
- Baseline e cadência semanal previstos para SC-004
- Evidência de impacto métrico exigida em PR para fluxo crítico alterado

### Simplicity Gate ✅
- Sem novo framework de segurança; uso de recursos nativos do Spring Security
- Reuso de porta existente (`ExternalMembershipReader`) com implementação real
- Sem abstração extra fora do necessário para identidade/sessão

### Architecture Gate ✅
- Domínio (`AuthorizationPolicy`) permanece sem lógica de infraestrutura
- Adapters de segurança e observabilidade concentrados em `infrastructure/security` e `infrastructure/observability`
- Contratos e tratamento de erro concentrados em API/adapters, preservando fronteiras hexagonais

### Java/Spring Gate ✅
- `UserDetailsService` custom + `PasswordEncoder` conforme prática Spring Security
- Sessão stateful em `SessionCreationPolicy.IF_REQUIRED`
- Tratamento explícito de exceções (`AuthenticationException`, `AccessDeniedException`, `DataAccessException`)
- Operações de leitura de identidade com `@Transactional(readOnly = true)`

## Project Structure

### Documentation (this feature)

```text
specs/005-spring-security-rbac/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── spring-security-rbac-access-matrix.yaml
└── tasks.md
```

### Source Code (repository root)

```text
app/src/main/java/br/com/nsfatima/calendario/
├── api/
│   └── error/
│       ├── ErrorCodes.java
│       └── GlobalExceptionHandler.java
├── domain/
│   └── policy/
│       └── AuthorizationPolicy.java
└── infrastructure/
    ├── security/
    │   ├── SecurityConfig.java
    │   ├── ExternalMembershipReader.java
    │   ├── EventoActorContextResolver.java
    │   ├── UsuarioDetails.java
    │   └── UsuarioDetailsService.java
    └── observability/
        ├── AuditLogService.java
        └── ... (instrumentação de métricas operacionais)

app/src/main/resources/db/migration/
└── V011__create_usuarios_membros_organizacao.sql

app/src/test/java/br/com/nsfatima/calendario/
└── ... (tests de integração de segurança)
```

**Structure Decision**: Projeto único `app/`, preservando arquitetura limpa/hexagonal e concentrando alterações em adapters de segurança, observabilidade e contratos/erros.

## Phase 0: Research

**Status**: ✅ concluído  
**Artefato**: [research.md](research.md)

Decisões consolidadas:
1. Form login com sessão stateful (`IF_REQUIRED`)
2. `UserDetailsService` customizado para tabela `usuarios`
3. Authorities derivadas de `membros_organizacao`
4. `EventoActorContextResolver` baseado em `SecurityContext` (sem cabeçalhos)
5. Fail-closed em erro de fonte de autorização (`503 AUTHZ_SOURCE_UNAVAILABLE`)
6. Matriz de acesso por endpoint com login público e endpoints de negócio autenticados
7. Flyway defensivo com `CREATE TABLE IF NOT EXISTS`
8. Catálogo de códigos de erro de segurança incluindo `SESSION_EXPIRED`

## Phase 1: Design

**Status**: ✅ concluído

| Artefato | Conteúdo |
|----------|----------|
| [data-model.md](data-model.md) | Modelo de dados externo (`usuarios`, `membros_organizacao`), entidades de segurança em runtime e regras de consistência |
| [contracts/spring-security-rbac-access-matrix.yaml](contracts/spring-security-rbac-access-matrix.yaml) | Contrato OpenAPI de login/logout + matriz de segurança por endpoint |
| [quickstart.md](quickstart.md) | Guia de execução e validação de testes de segurança |

### Post-design Constitution Check ✅

Revalidação concluída sem violações: todos os gates permanecem atendidos após design e clarificação de sessão expirada (401 JSON).

## Complexity Tracking

| Item | Why Needed | Simpler Alternative Rejected Because |
|------|------------|-------------------------------------|
| Sessão stateful via cookie | Requisito explícito da spec para login único da web app | JWT stateless contraria decisão de clarificação |
| Login público em endpoint único | Necessário para iniciar sessão em API | Tornar login autenticado é inviável semanticamente |
| `UserDetailsService` customizado | Schema externo não é o padrão `users/authorities` do Spring | `JdbcUserDetailsManager` padrão não atende modelo documental |
| Fail-closed com 503 | Requisito de segurança para indisponibilidade da fonte de autorização | Fallback permissivo introduz risco de escalada |
