# Research: Spring Security RBAC com Usuário/Senha e Sessão Stateful

**Feature**: 005-spring-security-rbac  
**Date**: 2026-03-15

---

## Decision 1: Mecanismo de Autenticação — Form Login com Sessão Stateful

- **Decision**: Usar Spring Security form login (ou endpoint POST de login) gerenciado por `UsernamePasswordAuthenticationFilter`, com sessão HTTP stateful mantida via `HttpSession` e cookie `JSESSIONID` seguro.
- **Rationale**: A decisão da spec (Q1/Q5) define autenticação por usuário e senha com sessão stateful e cookie. Spring Security 6.x suporta esse padrão nativamente via `formLogin()` e `SessionCreationPolicy.IF_REQUIRED`. É o caminho de menor risco e menor overhead para uma API consumida por uma web app.
- **Alternatives considered**:
  - HTTP Basic stateless por requisição: rejeitado porque a spec exige sessão stateful com cookie.
  - Bearer JWT: rejeitado — spec Q1 corrigida para usuário/senha.
  - Sessão stateful com token opaco customizado: rejeitado por não aproveitar o suporte nativo do Spring Security.
- **Spring Security 6.x specifics**:
  - `httpSecurity.formLogin()` com `loginProcessingUrl("/api/v1/auth/login")`, retorno JSON ao invés de redirect (via `AuthenticationSuccessHandler` e `AuthenticationFailureHandler` customizados).
  - `SessionCreationPolicy.IF_REQUIRED` (ou `ALWAYS`).
  - CSRF deve ser habilitado quando usando cookie session + web app. Para API JSON, usar `CookieCsrfTokenRepository.withHttpOnlyFalse()` ou desabilitar com documentação explícita do risco e do controle compensatório (SameSite=Strict cookie). Decisão adotada: desabilitar CSRF explicitamente com flag de risco aceito na arquitetura, dado que a web app opera no mesmo domínio.

---

## Decision 2: UserDetailsService — Carga de Identidade do Banco

- **Decision**: Implementar `UserDetailsService` customizado que carrega usuário da tabela `usuarios` do banco compartilhado, expondo `username`, `password` (hash) e `enabled`.
- **Rationale**: Spring Security 6.x exige um bean `UserDetailsService` (ou `UserDetailsManager`) para autenticação baseada em formulário. A tabela `usuarios` já existe no banco compartilhado conforme documentado nas specs anteriores.
- **Alternatives considered**:
  - `InMemoryUserDetailsManager`: rejeitado porque usuários e senhas são gerenciados externamente.
  - `JdbcUserDetailsManager` padrão (schema Spring): rejeitado porque a tabela `usuarios` tem estrutura própria diferente do schema padrão do Spring.
- **Spring Security 6.x specifics**:
  - `PasswordEncoder` bean via `BCryptPasswordEncoder` (ou compatível com hash existente na tabela).
  - `DaoAuthenticationProvider` configurado com o `UserDetailsService` e `PasswordEncoder` customizados.

---

## Decision 3: Derivação de Papéis — Banco como Fonte Oficial

- **Decision**: Após autenticação bem-sucedida, derivar `GrantedAuthority` do usuário consultando `membros_organizacao` (banco compartilhado). Os papéis são carregados em `UserDetails` na forma de `GrantedAuthority`, portanto disponíveis no `SecurityContext` para consulta de autorização. `ExternalMembershipReader` passa a ser uma porta real de leitura desses dados.
- **Rationale**: Spec Q3 e FR-003 exigem banco como fonte oficial. Carregar os `GrantedAuthority` no momento do login e persistir na sessão é a abordagem canônica do Spring Security — elimina overhead de consulta por requisição quando a sessão está ativa.
- **Trade-off**: Papéis em sessão podem ficar desatualizados se mudança ocorrer após login. Aceitável para contexto paroquial de baixo dinamismo; invalidação de sessão ao alterar membros_organizacao documenta mitigação.
- **Alternatives considered**:
  - Consulta ao banco a cada requisição: rejeitado por overhead desnecessário para o volume de uso esperado.
  - Trust em cabeçalhos HTTP arbitrários (estado atual): **eliminado** — vetor de elevação de privilégio não autenticada.

---

## Decision 4: Autorização por Escopo Organizacional — SecurityContext + AuthorizationPolicy

- **Decision**: `EventoActorContextResolver` passa a derivar `actor`, `role` e `organizationId` exclusivamente do `Principal` do `SecurityContext` (carregado de banco), removendo leitura de cabeçalhos HTTP arbitrários. `AuthorizationPolicy.isRoleAllowed()` permanece inalterada para validação de catálogo.
- **Rationale**: Leitura de papel por cabeçalho HTTP é vetor crítico de escalada de privilégios (OWASP A01:2021). Centralizar a autoridade no `SecurityContext` elimina esse vetor sem alterar a lógica de domínio.
- **Alternatives considered**:
  - Manter cabeçalhos com validação de assinatura: rejeitado por complexidade desnecessária quando o principal já está disponível na sessão.
  - `@PreAuthorize` com SpEL: pode ser introduzido como camada adicional no futuro, mas não elimina o problema raiz dos cabeçalhos.

---

## Decision 5: Tratamento de Fail-Closed — DataAccessException

- **Decision**: Se a consulta ao banco de identidade/papéis falhar com `DataAccessException` (ex.: `CannotGetJdbcConnectionException`), o sistema deve negar a requisição com `AUTHZ_SOURCE_UNAVAILABLE` (HTTP 503) ao invés de permitir. Implementado via tratamento explícito no `GlobalExceptionHandler` e no `UserDetailsService`.
- **Rationale**: FR-003B e SC-005 exigem fail-closed. Em caso de indisponibilidade, acesso não autorizado é mais danoso que indisponibilidade temporária.
- **Alternatives considered**:
  - Cache de último estado válido: rejeitado pela spec (opção A escolhida sobre opção C).
  - Propagação silenciosa como 401: rejeitado porque `AUTH_REQUIRED` e `AUTHZ_SOURCE_UNAVAILABLE` têm semânticas distintas e diagnóstico diferente.

---

## Decision 6: Matriz de Acesso por Endpoint (Baseline)

- **Decision**: Todos os endpoints da API exigem autenticação. Nenhum `permitAll()` para recursos de negócio. Actuator `/health` e `/info` permanecem abertos como antes.

| Endpoint | Método | Nível exigido |
|----------|--------|---------------|
| `/api/v1/eventos` | GET | Autenticado |
| `/api/v1/eventos` | POST | Autenticado + papel autorizado por org |
| `/api/v1/eventos/{id}` | GET | Autenticado |
| `/api/v1/eventos/{id}` | PATCH | Autenticado + papel autorizado por org |
| `/api/v1/eventos/{id}/participantes` | PUT/DELETE | Autenticado + papel autorizado por org |
| `/api/v1/eventos/{id}/recorrencia` | POST | Autenticado + papel autorizado por org |
| `/api/v1/projetos` | GET/POST | Autenticado |
| `/api/v1/projetos/{id}` | GET | Autenticado |
| `/api/v1/aprovacoes` | GET/POST | Autenticado + papel autorizado por org |
| `/api/v1/aprovacoes/{id}` | POST | Autenticado + papel autorizado por org |
| `/api/v1/observacoes` | GET/POST | Autenticado |
| `/api/v1/auditoria` | GET | Autenticado |
| `/api/v1/auth/login` | POST | Público (endpoint de login) |
| `/api/v1/auth/logout` | POST | Autenticado |
| `/actuator/health`, `/actuator/info` | GET | Público |

- **Rationale**: Dec Q2 da spec — nenhum endpoint público. Login e logout são endpoints de controle de sessão, não de negócio.

---

## Decision 7: Migração Flyway — Tabelas usuarios e membros_organizacao

- **Decision**: Criar migração Flyway para `usuarios` e `membros_organizacao` no esquema compartilhado (se ainda não existirem), com colunas mínimas necessárias para autenticação e RBAC. Script com `CREATE TABLE IF NOT EXISTS` para compatibilidade com banco compartilhado já existente.
- **Rationale**: `UserDetailsService` e `ExternalMembershipReader` precisam de tabelas acessíveis. O banco compartilhado pode já ter essas tabelas — migração defensiva com `IF NOT EXISTS`.
- **Columns mínimas**:
  - `usuarios`: `id UUID PK`, `username VARCHAR`, `password_hash VARCHAR`, `enabled BOOLEAN`
  - `membros_organizacao`: `id UUID PK`, `usuario_id UUID FK usuarios.id`, `organizacao_id UUID`, `tipo_organizacao VARCHAR`, `papel VARCHAR`, `ativo BOOLEAN`

---

## Decision 8: Novos ErrorCodes

- **Decision**: Adicionar `AUTH_INVALID`, `ROLE_SCOPE_INVALID`, `AUTHZ_SOURCE_UNAVAILABLE`, `SESSION_EXPIRED` ao enum `ErrorCodes`.
- **Rationale**: Spec FR-008 exige códigos de máquina estáveis e semanticamente corretos para cada categoria de erro de segurança.
