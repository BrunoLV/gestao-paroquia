# Quickstart: Spring Security RBAC com Usuário/Senha

**Feature**: 005-spring-security-rbac  
**Branch**: `005-spring-security-rbac`

---

## Pré-requisitos

- Java 21
- Docker (para PostgreSQL local)
- Banco de dados com tabelas `usuarios` e `membros_organizacao` criadas via Flyway

## Executar a aplicação

```bash
# A partir do root do repositório
./gradlew :app:bootRun
```

---

## Cenários de Teste por Classe

### 1. `SecurityEndpointAuthIntegrationTest` — Bloqueio sem autenticação

**Objetivo**: Todo endpoint de negócio retorna 401 sem sessão ativa.

```bash
./gradlew :app:test --tests "*SecurityEndpointAuthIntegrationTest"
```

Cenários cobertos:
- `GET /api/v1/eventos` sem cookie → `401 AUTH_REQUIRED`
- `POST /api/v1/eventos` sem cookie → `401 AUTH_REQUIRED`
- `GET /api/v1/aprovacoes` sem cookie → `401 AUTH_REQUIRED` (era `permitAll()`)
- `POST /api/v1/aprovacoes` sem cookie → `401 AUTH_REQUIRED` (era `permitAll()`)
- `POST /api/v1/aprovacoes/{id}` sem cookie → `401 AUTH_REQUIRED` (era `permitAll()`)
- `GET /api/v1/auditoria` sem cookie → `401 AUTH_REQUIRED`

---

### 2. `FormLoginIntegrationTest` — Ciclo de autenticação

**Objetivo**: Login com credenciais válidas/inválidas; ciclo login-acesso-logout.

```bash
./gradlew :app:test --tests "*FormLoginIntegrationTest"
```

Cenários cobertos:
- `POST /api/v1/auth/login` com credenciais válidas → `200` + cookie `JSESSIONID`
- `POST /api/v1/auth/login` com senha incorreta → `401 AUTH_INVALID`
- `POST /api/v1/auth/login` com usuário inexistente → `401 AUTH_INVALID`
- `GET /api/v1/eventos` com cookie válido → `200`
- `POST /api/v1/auth/logout` com cookie válido → `204` + cookie removido
- `GET /api/v1/eventos` após logout → `401 SESSION_EXPIRED`

---

### 3. `RbacOrganizationIntegrationTest` — Escopo de papel por organização

**Objetivo**: Valida que papéis organizacionais controlam acesso a mutações.

```bash
./gradlew :app:test --tests "*RbacOrganizationIntegrationTest"
```

Cenários cobertos:
- Usuário `secretario` em `CONSELHO` pode criar evento → `201`
- Usuário `secretario` em `CLERO` não pode criar evento → `403 ROLE_SCOPE_INVALID`
- Usuário `membro` sem papel de escrita → `403 ACCESS_DENIED`
- Usuário autenticado sem nenhum vínculo ativo em `membros_organizacao` → `403 ACCESS_DENIED`
- Usuário com papel `coordenador` em `PASTORAL` pode listar eventos → `200`

---

### 4. `FailClosedAuthzIntegrationTest` — Indisponibilidade do banco

**Objetivo**: Confirma comportamento fail-closed quando DB de autorização falha.

```bash
./gradlew :app:test --tests "*FailClosedAuthzIntegrationTest"
```

Cenários cobertos:
- `UserDetailsService` lança `DataAccessException` no login → `503 AUTHZ_SOURCE_UNAVAILABLE`
- `ExternalMembershipReader` lança `DataAccessException` no login → `503 AUTHZ_SOURCE_UNAVAILABLE`
- Nenhum cenário retorna `200` quando DB falha (fail-closed confirmado)

---

### 5. `DeniedWriteAuditIntegrationTest` — Auditoria de tentativas negadas

**Objetivo**: Tentativas de acesso negadas são registradas no log de auditoria.

```bash
./gradlew :app:test --tests "*DeniedWriteAuditIntegrationTest"
```

Cenários cobertos:
- `POST /api/v1/eventos` com papel insuficiente → `403` + evento de auditoria registrado
- Log de auditoria acessível em `GET /api/v1/auditoria` (com autenticação)

---

## Executar todos os testes da feature

```bash
./gradlew :app:test
```

Relatório de testes:
```
app/build/reports/tests/test/index.html
```

---

## Verificar migração Flyway

Após iniciar a aplicação, confirmar que `usuarios` e `membros_organizacao` foram criadas:

```sql
-- Conectar ao banco e verificar
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('usuarios', 'membros_organizacao');
```

Saída esperada:
```
   table_name
-----------------
 membros_organizacao
 usuarios
(2 rows)
```

---

## Dados de Teste (H2 em memória para testes)

Os testes usam H2 com `@Sql` ou `@BeforeEach` para inserção de dados. Estrutura mínima:

```sql
-- usuario ativo
INSERT INTO usuarios (id, username, password_hash, enabled)
VALUES ('00000000-0000-0000-0000-000000000001',
        'joao.silva',
        '$2a$10$HASH_BCRYPT_AQUI',
        true);

-- vinculo ativo: secretario no CONSELHO
INSERT INTO membros_organizacao (id, usuario_id, organizacao_id, tipo_organizacao, papel, ativo)
VALUES ('00000000-0000-0000-0000-000000000010',
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000100',
        'CONSELHO',
        'secretario',
        true);
```

---

## Referências

- [Spec](spec.md)
- [Research](research.md)
- [Data Model](data-model.md)
- [Contratos de Acesso](contracts/spring-security-rbac-access-matrix.yaml)
