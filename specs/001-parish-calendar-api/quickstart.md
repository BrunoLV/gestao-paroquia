# Quickstart: API de Calendario Anual Paroquial

## 1. Prerequisitos
- Java 21
- Docker (para PostgreSQL local de desenvolvimento)
- Gradle Wrapper (`./gradlew`)

## 2. Preparar ambiente local
1. Subir PostgreSQL local:
```bash
docker run --name calendario-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=calendario -p 5432:5432 -d postgres:16
```
2. Configurar variaveis:
```bash
export DB_URL='jdbc:postgresql://localhost:5432/calendario'
export DB_USER='postgres'
export DB_PASSWORD='postgres'
export APP_TIMEZONE='America/Sao_Paulo'
```
3. Rodar migrations (quando implementadas):
```bash
./gradlew :app:flywayMigrate
```

## 3. Build e testes
1. Compilar:
```bash
./gradlew :app:build
```
2. Rodar unit tests:
```bash
./gradlew :app:test --tests '*unit*'
```
3. Rodar integration tests:
```bash
./gradlew :app:test --tests '*integration*'
```
4. Rodar contract tests:
```bash
./gradlew :app:test --tests '*contract*'
```

## 4. Subir a API
```bash
./gradlew :app:bootRun
```

## 5. Fluxo rapido de verificacao funcional
1. Criar evento em RASCUNHO com `organizacao_responsavel_id`.
2. Adicionar participantes via `eventos_envolvidos`.
3. Registrar observacao com `usuario_id` autor.
4. Solicitar alteracao sensivel (horario/cancelamento).
5. Aprovar com papel administrativo valido.
6. Limpar totalmente participantes do evento via `DELETE /eventos/{eventoId}/participantes` e validar evento sem participantes.
7. Consultar calendario por periodo (resposta em America/Sao_Paulo) com sessao autenticada para validar acesso protegido de leitura.

## 6. Checklist de aceite por historia
- Historia 1 (CRUD de eventos): validar create/list/get/update/cancel.
- Historia 2 (projetos e agrupamento): validar vinculo/desvinculo de projeto e filtro por projeto.
- Historia 3 (RBAC e observacoes): validar bloqueio de perfis nao autorizados e append-only de observacoes.

## 7. Suite dedicada de contrato/integration (obrigatoria)
1. RBAC e catalogo de papeis por organizacao:
```bash
./gradlew :app:test --tests '*RoleCatalogContractTest*' --tests '*RbacOrganizationIntegrationTest*'
```
- Deve incluir caso negativo: `secretario` fora do Conselho retorna `403 ACCESS_DENIED`.

2. Visibilidade por status:
```bash
./gradlew :app:test --tests '*PublicVisibilityContractTest*' --tests '*StatusVisibilityIntegrationTest*'
```
- Deve validar: `RASCUNHO` fora do publico, `CONFIRMADO` visivel anonimo, `CANCELADO` com motivo no historico interno.

3. Regra de dominio `ADICIONADO_EXTRA`:
```bash
./gradlew :app:test --tests '*AddedExtraValidationContractTest*' --tests '*AddedExtraDomainIntegrationTest*'
```
- Deve rejeitar criacao/atualizacao sem justificativa obrigatoria e rastreavel.

## 8. Criterios tecnicos obrigatorios
- Nao escrever em `usuarios`, `organizacoes`, `membros_organizacao`.
- Nao vincular evento diretamente a usuario.
- Persistir timestamps em UTC e converter para America/Sao_Paulo na borda da API.
- Exigir autenticacao para leituras e mutacoes da API (`GET /api/v1/eventos` e demais endpoints de negocio).
- Retornar codigos de erro deterministicos para validacao, autorizacao, nao encontrado e conflito.
- Rejeitar `ADICIONADO_EXTRA` sem justificativa obrigatoria.
- Rejeitar papel `secretario` fora de organizacao do tipo Conselho.

## 9. Smoke e observabilidade de release
1. Rodar smoke E2E:
```bash
./gradlew :app:test --tests '*SmokeCalendarFlowTest*'
```
2. Rodar baseline semanal de metricas:
```bash
./gradlew :app:test --tests '*WeeklyMetricsSnapshotIntegrationTest*'
```
3. Validar escrita bloqueada com trilha de auditoria:
```bash
./gradlew :app:test --tests '*DeniedWriteAuditIntegrationTest*'
```

## 10. Medicao continua (SC-001/SC-002/SC-005)
- Registrar semanalmente: tempo medio de cadastro, latencia p95 de consultas, indicador de retrabalho.
- Exportar snapshot de metricas por periodo para analise de tendencia.
- Manter historico de snapshots com correlação por janela semanal.
