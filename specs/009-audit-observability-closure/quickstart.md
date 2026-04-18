# Quickstart: Fechamento de Auditoria e Retrabalho

## 1. Prerequisites
- Java 21
- Gradle Wrapper (`./gradlew`)
- Banco configurado para o profile local em `app/src/main/resources/application.yml`
- Usuários de teste com escopos organizacionais distintos para validação de RBAC

## 2. Build Baseline
1. Validar baseline da branch:
```bash
./gradlew :app:build
```
2. Rodar a suíte atual completa para confirmar baseline sem regressão antes da implementação:
```bash
./gradlew cleanTest :app:test
```

## 3. Implementation Checklist (Feature 009)
1. Introduzir entidade, repositório e migration para persistência imutável da trilha auditável.
2. Adaptar o fluxo de auditoria atual para garantir gravação persistida com fail-closed nas mutações cobertas.
3. Implementar `GET /api/v1/auditoria/eventos/trilha` com filtros por período e `organizacaoId` obrigatórios.
4. Substituir o placeholder de `GetIndicadorRetrabalhoUseCase` por cálculo real da taxa com numerador e denominador.
5. Implementar `GET /api/v1/auditoria/eventos/retrabalho` com `organizacaoId` e período obrigatórios.
6. Atualizar o snapshot semanal para refletir a taxa de retrabalho operacional.
7. Consolidar contrato OpenAPI, DTOs, handlers de erro e suites focadas de contrato/integração/performance.

## 4. Focused Validation
1. Contrato da trilha auditável:
```bash
./gradlew :app:test --tests '*Auditoria*ContractTest'
```
2. Escopo organizacional e filtros inválidos:
```bash
./gradlew :app:test --tests '*AuditTrail*IntegrationTest' --tests '*RbacOrganizationIntegrationTest'
```
3. Cálculo da taxa de retrabalho:
```bash
./gradlew :app:test --tests '*Retrabalho*IntegrationTest' --tests '*GetIndicadorRetrabalho*Test'
```
4. Fail-closed da persistência auditável:
```bash
./gradlew :app:test --tests '*AuditPersistenceFailure*IntegrationTest'
```
5. Performance Tier 1 dos endpoints novos:
```bash
./gradlew :app:test --tests '*AuditoriaTier1PerformanceTest'
```

## 5. Manual API Smoke
1. Autenticar com um usuário que possua acesso a uma organização conhecida.
2. Executar uma mutação coberta de evento ou observação com `X-Correlation-Id` explícito.
3. Consultar `GET /api/v1/auditoria/eventos/trilha` com `organizacaoId` e granularidade compatível.
4. Validar retorno com `acao`, `resultado`, `correlationId`, `ocorridoEmUtc` e ordenação estável.
5. Consultar `GET /api/v1/auditoria/eventos/retrabalho` para a mesma organização e período.
6. Validar presença de `taxaRetrabalho`, `numeradorOcorrenciasElegiveis` e `denominadorEventosAfetados`.
7. Repetir consulta com `organizacaoId` fora do escopo do usuário e esperar erro determinístico de acesso.
8. Repetir consulta informando granularidade e `inicio/fim` simultaneamente e esperar erro determinístico de validação.

## 6. Contract Checklist
- `GET /api/v1/auditoria/eventos/trilha` exige `organizacaoId` e período válidos.
- `GET /api/v1/auditoria/eventos/retrabalho` exige `organizacaoId` e período válidos.
- O contrato aceita granularidade ou `inicio/fim`, nunca ambos na mesma requisição.
- A resposta da trilha é ordenada deterministicamente por timestamp e identificador estável.
- A resposta do retrabalho sempre inclui taxa, numerador e denominador.
- Erros de validação e autorização retornam `errorCode` determinístico e `correlationId`.

## 7. Observability Checklist
- Toda mutação coberta persiste trilha auditável consultável, e não apenas log textual.
- `correlationId` é preservado na persistência auditável e reaparece na resposta de consulta.
- Falha na persistência auditável impede confirmação da mutação.
- O snapshot semanal inclui baseline consistente das métricas constitucionais.
- A taxa de retrabalho consultada e a taxa consolidada no snapshot seguem a mesma semântica.

## 8. Measurement Checklist (SC-001..SC-005)
- SC-001: validar que mutações de evento, aprovação e observação geram registros consultáveis.
- SC-002: validar ausência de vazamento entre organizações e entre períodos.
- SC-003: validar taxa, numerador e denominador contra ocorrências preparadas.
- SC-004: medir p95 <= 2000ms para `trilha` e `retrabalho` em Tier 1.
- SC-005: validar erros determinísticos para filtros inválidos, escopo negado e persistência auditável indisponível.

## 9. Final Validation Run
1. Executar bateria final focada:
```bash
./gradlew :app:test --tests '*Auditoria*' --tests '*Retrabalho*' --tests '*RbacOrganization*'
```
2. Resultado esperado para fechamento da feature: `BUILD SUCCESSFUL`.

## 9.1 Executado em 2026-04-18
1. Compilacao focada da feature:
```bash
./gradlew :app:compileJava :app:compileTestJava
```
Resultado: `BUILD SUCCESSFUL`.
2. Bateria focada realmente executada para fechamento parcial:
```bash
./gradlew :app:test --tests 'br.com.nsfatima.calendario.integration.foundation.*' --tests 'br.com.nsfatima.calendario.contract.AuditoriaEventosContractTest' --tests 'br.com.nsfatima.calendario.integration.auditoria.*' --tests 'br.com.nsfatima.calendario.contract.IndicadorRetrabalhoContractTest' --tests 'br.com.nsfatima.calendario.integration.metrics.IndicadorRetrabalho*' --tests 'br.com.nsfatima.calendario.integration.metrics.WeeklyMetricsSnapshotIntegrationTest' --tests 'br.com.nsfatima.calendario.integration.metrics.WeeklyMetricsSnapshotHistoryIntegrationTest' --tests 'br.com.nsfatima.calendario.performance.AuditoriaTier1PerformanceTest' --tests 'br.com.nsfatima.calendario.contract.EventoStatusVisibilityRegressionContractTest' --tests 'br.com.nsfatima.calendario.integration.evento.EventoLifecycleTransitionRegressionIntegrationTest'
```
Resultado: `BUILD SUCCESSFUL`.
3. Evidencias confirmadas nesta execucao:
- trilha auditavel persistida e consultavel por organizacao/periodo
- taxa de retrabalho com numerador e denominador explicitos
- rollback fail-closed quando a persistencia auditavel falha
- snapshot semanal com payload de retrabalho e historico local de snapshots
- regressao de visibilidade por status e transicao invalida de lifecycle preservadas

## 10. Performance Evidence (SC-004)
1. Executar a suíte de performance Tier 1:
```bash
./gradlew :app:test --tests '*AuditoriaTier1PerformanceTest'
```
2. Validar no teste os limiares:
- `GET /api/v1/auditoria/eventos/trilha` p95 <= 2000ms
- `GET /api/v1/auditoria/eventos/retrabalho` p95 <= 2000ms
3. Registrar baseline semanal com data, ambiente, p95 e organização ou escopo aferido.

## 11. Weekly Baseline Routine
1. Executar semanalmente a rotina de coleta validando consistência do snapshot:
```bash
./gradlew :app:test --tests '*WeeklyMetricsSnapshotIntegrationTest' --tests '*AuditoriaTier1PerformanceTest'
```
2. Registrar evidências mínimas:
- `capturedAtUtc`
- `ambiente`
- `calendarQueryLatencyMsP95`
- `administrativeReworkRate` ou payload equivalente
- `comparacaoPrePos`

## 12. Pendencias Restantes
- Consolidar smoke manual contra ambiente com PostgreSQL real para registrar baseline operacional externa ao teste automatizado.
- Registrar em artefato de release o comparativo entre snapshots semanais consecutivos coletados fora da JVM de teste.
