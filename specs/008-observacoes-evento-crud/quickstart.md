# Quickstart: Observacoes de Evento com Controle de Tipo e Autoria

## 1. Prerequisites
- Java 21
- Gradle Wrapper (`./gradlew`)
- Banco configurado para profile local em `app/src/main/resources/application.yml`

## 2. Build Baseline
1. Validar baseline da branch:
```bash
./gradlew :app:build
```
2. Rodar suites relevantes atuais para mapear regressao inicial:
```bash
./gradlew :app:test --tests '*Observacao*' --tests '*CancelEvento*' --tests '*Aprovacao*'
```

## 3. Implementation Checklist (Feature 008)
1. Substituir os stubs atuais de observacao por persistencia real via JPA.
2. Limitar a criacao manual ao tipo `NOTA` com validacao e erro deterministico.
3. Implementar os dois modos de listagem funcional: `todas` e `minhas`.
4. Implementar edicao de `NOTA` com trilha de revisoes auditavel.
5. Implementar exclusao logica de `NOTA` com ocultacao nas consultas funcionais.
6. Introduzir servico separado para observacoes sistêmicas e adaptar fluxos de evento que ja geram `CANCELAMENTO`.
7. Garantir autoria humana preferencial e fallback tecnico para observacoes sistêmicas.
8. Atualizar contratos, DTOs, migrations, auditoria e testes focados.

## 4. Focused Validation
1. Criacao manual de `NOTA`:
```bash
./gradlew :app:test --tests '*ObservacoesContractTest'
```
2. Listagem `minhas` vs `todas`:
```bash
./gradlew :app:test --tests '*CreateListObservacaoIntegrationTest' --tests '*ListMyObservacoesIntegrationTest'
```
3. Edicao com historico de revisoes:
```bash
./gradlew :app:test --tests '*UpdateObservacaoIntegrationTest'
```
4. Exclusao logica com ocultacao funcional:
```bash
./gradlew :app:test --tests '*DeleteObservacaoIntegrationTest'
```
5. Geracao sistêmica e autoria correta:
```bash
./gradlew :app:test --tests '*CancelEventoObservacaoIntegrationTest' --tests '*SystemObservacaoFallbackAuthorIntegrationTest' --tests '*RejectSystemObservationManualCreationIntegrationTest'
```

## 5. Manual API Smoke
1. Autenticar como usuario colaborador do evento.
2. Executar `POST /api/v1/eventos/{eventoId}/observacoes` com `tipo=NOTA` e conteudo valido -> esperar `201` com `usuarioId` e `criadoEmUtc`.
3. Executar `GET /api/v1/eventos/{eventoId}/observacoes/minhas` -> esperar somente observacoes do usuario autenticado.
4. Executar `GET /api/v1/eventos/{eventoId}/observacoes` com usuario autorizado -> esperar historico completo do evento, ordenado.
5. Executar `PATCH /api/v1/eventos/{eventoId}/observacoes/{observacaoId}` em nota propria -> esperar conteudo atualizado.
6. Executar `DELETE /api/v1/eventos/{eventoId}/observacoes/{observacaoId}` em nota propria -> esperar sucesso e ausencia da nota nas listagens normais.
7. Tentar criar `tipo=CANCELAMENTO` pelo endpoint manual -> esperar erro deterministico.
8. Executar um fluxo sistêmico de cancelamento -> validar criacao automatica de observacao `CANCELAMENTO` com texto e autoria corretos.

## 6. Contract Checklist
- `POST /eventos/{eventoId}/observacoes` aceita apenas `NOTA`.
- `GET /eventos/{eventoId}/observacoes` retorna todas as observacoes funcionais do evento conforme permissao.
- `GET /eventos/{eventoId}/observacoes/minhas` retorna apenas observacoes do usuario autenticado.
- `PATCH /eventos/{eventoId}/observacoes/{observacaoId}` so permite editar `NOTA` propria e preserva trilha de revisoes.
- `DELETE /eventos/{eventoId}/observacoes/{observacaoId}` faz soft delete apenas em `NOTA` propria.
- Respostas funcionais sempre incluem `usuarioId` e `criadoEmUtc`.
- Tipos sistêmicos nao possuem criacao manual via contrato publico.

## 7. Observability Checklist
- Toda operacao funcional de observacao gera log estruturado com `correlationId`, ator, evento, observacao e resultado.
- Edicao registra revisao suficiente para reconstruir conteudo anterior e novo.
- Exclusao logica registra marcador de remocao e autor da remocao.
- Observacoes sistêmicas registram tipo, origem do fluxo e se a autoria veio de ator humano ou fallback tecnico.
- Consultas internas de auditoria conseguem recuperar notas removidas logicamente sem reintroduzi-las nas consultas funcionais.

## 8. Measurement Checklist (SC-001..SC-012)
- SC-001: validar rejeicao de criacao manual com tipo diferente de `NOTA`.
- SC-002: validar bloqueio de edicao/exclusao para usuario nao autor.
- SC-003: validar imutabilidade manual de tipos sistêmicos.
- SC-004: validar bateria de contratos e integracoes da feature sem regressao.
- SC-005: medir p95 de create/edit/list via `ObservacaoTier1PerformanceTest` com limiar `<= 2000ms` por operacao em ambiente Tier 1.
- SC-006: validar presenca de `usuarioId` e `criadoEmUtc` nas respostas.
- SC-007: validar correspondencia entre texto/origem e observacao sistêmica persistida.
- SC-008: validar separacao correta dos modos `minhas` e `todas`.
- SC-009: validar soft delete sem remocao fisica.
- SC-010: validar ocultacao funcional de notas removidas.
- SC-011: validar autoria humana vs fallback tecnico em fluxos sistêmicos.
- SC-012: validar persistencia do historico de revisoes.

## 9. Final Validation Run
1. Executar bateria final focada:
```bash
./gradlew :app:test --tests '*Observacao*' --tests '*CancelEvento*' --tests '*Aprovacao*'
```
2. Resultado esperado para fechamento da feature: `BUILD SUCCESSFUL`.

## 10. Performance Evidence (SC-005)
1. Executar a suite de performance Tier 1:
```bash
./gradlew :app:test --tests '*ObservacaoTier1PerformanceTest'
```
2. Validar no teste os limiares p95 por operacao:
- create de `NOTA` <= 2000ms
- edit de `NOTA` <= 2000ms
- listagem funcional <= 2000ms
3. Registrar baseline semanal em artefato de evidencias da feature (data, ambiente, p95 por operacao) para comparacao pre e pos-implementacao.

## 11. Weekly Baseline Routine (SC-005)
1. Executar semanalmente (segunda-feira, UTC 09:00-10:00):
```bash
./gradlew :app:test --tests '*ObservacaoTier1PerformanceTest'
```
2. Registrar evidencias no formato:
- dataHoraUtc: 2026-04-13T12:00:00Z
- ambiente: tier1-test
- p95CreateMs: <= 2000
- p95EditMs: <= 2000
- p95ListMs: <= 2000
- comparacaoPrePos: pre=pending-baseline-base-branch; pos=pass-feature-branch
