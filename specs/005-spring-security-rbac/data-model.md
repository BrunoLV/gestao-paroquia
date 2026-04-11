# Data Model: Spring Security RBAC com Usuário/Senha

**Feature**: 005-spring-security-rbac  
**Date**: 2026-03-15

---

## 1. Entidades Externas (banco compartilhado — somente leitura por esta API)

### 1.1 usuarios

Tabela gerenciada por outra aplicação. Esta API lê para autenticação.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| `id` | UUID | PK | Identificador único do usuário |
| `username` | VARCHAR(255) | UNIQUE NOT NULL | Login do usuário (ex.: email ou nome de acesso) |
| `password_hash` | VARCHAR(255) | NOT NULL | Hash da senha (BCrypt ou compatível) |
| `enabled` | BOOLEAN | NOT NULL DEFAULT TRUE | Usuário ativo/inativo |

**Migração**: `CREATE TABLE IF NOT EXISTS` — compatível com banco pré-existente.

---

### 1.2 membros_organizacao

Tabela gerenciada por outra aplicação. Esta API lê para derivar papéis e escopo de autorização.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| `id` | UUID | PK | Identificador do vínculo |
| `usuario_id` | UUID | FK → usuarios.id NOT NULL | Usuário vinculado |
| `organizacao_id` | UUID | NOT NULL | Identificador da organização |
| `tipo_organizacao` | VARCHAR(32) | NOT NULL | Tipo da organização: `CLERO`, `CONSELHO`, `PASTORAL`, `LAICATO` |
| `papel` | VARCHAR(64) | NOT NULL | Papel do usuário: `paroco`, `vigario`, `coordenador`, `vice-coordenador`, `secretario`, `membro`, etc. |
| `ativo` | BOOLEAN | NOT NULL DEFAULT TRUE | Vínculo ativo |

**Índice recomendado**: `(usuario_id, ativo)` — consulta frequente por usuário ativo.

---

## 2. Entidades Spring Security (derivadas em runtime)

### 2.1 UsuarioDetails (implementação de UserDetails)

Objeto construído na camada de infraestrutura a partir de `usuarios` + `membros_organizacao` após autenticação bem-sucedida. Armazenado na `HttpSession` como `Principal`.

| Campo | Tipo | Origem | Descrição |
|-------|------|--------|-----------|
| `username` | String | `usuarios.username` | Identificador principal no SecurityContext |
| `usuarioId` | UUID | `usuarios.id` | ID para correlação com observações |
| `authorities` | Collection<GrantedAuthority> | `membros_organizacao` (papel + tipo_organizacao) | Papéis carregados ao login; formato: `ROLE_<TIPO_ORG>_<PAPEL>` |
| `enabled` | boolean | `usuarios.enabled` | Controla se o usuário pode autenticar |

**Formato de authority**: `ROLE_CONSELHO_SECRETARIO`, `ROLE_CLERO_PAROCO`, `ROLE_PASTORAL_COORDENADOR`, etc. Derivado concatenando `tipo_organizacao` + `papel` em maiúsculas.

---

### 2.2 EventoActorContext (porta de domínio — inalterada em assinatura)

Objeto resolvido por `EventoActorContextResolver` antes de cada operação autorizada. Passa a ser derivado exclusivamente do `SecurityContext` (não de cabeçalhos HTTP).

| Campo | Tipo | Origem | Descrição |
|-------|------|--------|-----------|
| `actor` | String | `Principal.name` | Username autenticado |
| `role` | String | `GrantedAuthority` selecionada | Papel ativo para a operação |
| `organizationType` | String | Parte da authority | Tipo de organização do papel ativo |
| `organizationId` | UUID | `membros_organizacao.organizacao_id` | Organização de escopo da decisão |

**Nota**: quando o usuário tem múltiplos vínculos, o resolver seleciona o papel de maior hierarquia compatível com o tipo de ocorrência do recurso alvo, ou exige que o caller passe `organizacaoId` como parâmetro de escopo explícito.

---

## 3. Erros de Segurança (novos códigos de máquina)

| Código | HTTP Status | Semântica |
|--------|-------------|-----------|
| `AUTH_REQUIRED` | 401 | Credencial ausente ou sessão não iniciada |
| `AUTH_INVALID` | 401 | Credenciais inválidas no login |
| `ACCESS_DENIED` | 403 | Usuário autenticado sem autorização para a operação |
| `ROLE_SCOPE_INVALID` | 403 | Papel incompatível com o tipo de organização conforme catálogo |
| `AUTHZ_SOURCE_UNAVAILABLE` | 503 | Fonte oficial de autorização (banco) indisponível — fail-closed |
| `SESSION_EXPIRED` | 401 | Sessão expirada ou invalidada; requer novo login |

---

## 4. Regras de Transição e Consistência

- Um usuário sem nenhum vínculo ativo em `membros_organizacao` é autenticado mas não possui `GrantedAuthority` de organização → operações que exigem papel organizacional resultam em `ACCESS_DENIED`.
- O papel `secretario` é válido somente para `tipo_organizacao = CONSELHO`; validado por `AuthorizationPolicy.isRoleAllowed()`.
- A sessão mantém o snapshot dos papéis no momento do login; alterações em `membros_organizacao` só são refletidas após novo login ou invalidação explícita de sessão.
- Qualquer `DataAccessException` durante carga de `UserDetails` ou consulta de membros deve propagar como `AUTHZ_SOURCE_UNAVAILABLE` (fail-closed), nunca como autenticação bem-sucedida.

---

## 5. Impacto em Entidades Existentes

- `EventoActorContext` — assinatura de record inalterada; mudança apenas no `EventoActorContextResolver` (fonte dos dados).
- `AuthorizationPolicy` — lógica inalterada; método `isRoleAllowed(orgType, role)` é preservado.
- `ErrorCodes` enum — adição de 4 novos valores: `AUTH_INVALID`, `ROLE_SCOPE_INVALID`, `AUTHZ_SOURCE_UNAVAILABLE`, `SESSION_EXPIRED`.
- `GlobalExceptionHandler` — adição de handler para `DataAccessException` (fail-closed) e handlers de entrada HTTP de segurança (`AccessDeniedException`, `AuthenticationException`).
