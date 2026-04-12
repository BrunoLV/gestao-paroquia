# Quickstart: Cancelamento de Evento

## 1. Prerequisites
- Java 21
- Gradle Wrapper (`./gradlew`)
- Banco configurado para profile local em `app/src/main/resources/application.yml`

## 2. Build Baseline
1. Validar baseline da branch:
```bash
./gradlew :app:build
```
2. Rodar suites relevantes atuais para mapear regressão inicial:
```bash
./gradlew :app:test --tests '*Evento*ContractTest' --tests '*Aprovacao*IntegrationTest' --tests '*Security*IntegrationTest'
```

## 3. Implementation Checklist (Feature 006)
1. Implementar `DELETE /api/v1/eventos/{eventoId}` com persistência real de cancelamento para papéis com poder imediato.
2. Fazer `DELETE /api/v1/eventos/{eventoId}` criar solicitação pendente com snapshot da ação para vigário e liderança de pastoral/laicato.
3. Estender o fluxo de `PATCH /api/v1/aprovacoes/{id}` para decidir a solicitação e executar automaticamente o cancelamento quando `APROVADA`.
4. Garantir regra de escopo organizacional por papel.
5. Persistir observação append-only do tipo `CANCELAMENTO` na efetivação.
6. Garantir logs/auditoria para solicitação, decisão, efetivação e falha de efetivação.

## 4. Focused Validation
1. Cancelamento direto por pároco/conselho:
```bash
./gradlew :app:test --tests '*CancelEventoImmediateIntegrationTest'
```
2. Solicitação pendente para pastoral/laicato/vigário:
```bash
./gradlew :app:test --tests '*CancelEventoApprovalRequestIntegrationTest'
```
3. Aprovação com efetivação automática:
```bash
./gradlew :app:test --tests '*ApproveCancelEventoIntegrationTest'
```
4. Reprovação sem alteração do evento:
```bash
./gradlew :app:test --tests '*RejectCancelEventoIntegrationTest'
```
5. Escopo organizacional e papéis proibidos:
```bash
./gradlew :app:test --tests '*CancelEventoAuthorizationIntegrationTest'
```
6. Visibilidade e histórico de cancelado:
```bash
./gradlew :app:test --tests '*CancelledEventoVisibilityIntegrationTest'
```

## 5. Manual API Smoke
1. Autenticar como pároco ou coordenador do conselho.
2. Executar `DELETE /api/v1/eventos/{eventoId}` com `{ "motivo": "..." }` para evento `CONFIRMADO` -> esperar `200` e evento `CANCELADO`.
3. Autenticar como coordenador de pastoral da organização responsável.
4. Executar o mesmo `DELETE` -> esperar `202` com `solicitacaoAprovacaoId` e evento ainda `CONFIRMADO`.
5. Autenticar como pároco ou liderança do conselho e executar `PATCH /api/v1/aprovacoes/{id}` com decisão `APROVADA`.
6. Reconsultar o evento -> esperar `CANCELADO` com `canceladoMotivo` original.
7. Repetir o fluxo com decisão `REPROVADA` -> esperar evento inalterado e trilha de reprovação auditada.

## 6. Contract Checklist
- `DELETE /api/v1/eventos/{eventoId}` mantém rota e ganha comportamento real.
- Corpo do `DELETE` é obrigatório e não aceita `aprovacaoId`.
- `DELETE` pode retornar `200` ou `202` conforme o papel do solicitante.
- `APPROVAL_PENDING` é outcome funcional do `202 Accepted`, indicando solicitação criada e aguardando decisão.
- `PATCH /api/v1/aprovacoes/{id}` deve registrar a decisão e, em `APROVADA`, executar automaticamente a ação pendente.
- Erros `VALIDATION_ERROR`, `EVENT_NOT_FOUND`, `FORBIDDEN`, `INVALID_STATUS_TRANSITION` e `APPROVAL_EXECUTION_FAILED` devem ser observáveis e testáveis.

## 7. Observability Checklist
- Toda solicitação de cancelamento gera log com `correlationId`, ator, papel, organização, evento e resultado inicial (`pending` ou `success`).
- Toda decisão de aprovação/reprovação gera log estruturado com aprovador e `solicitacaoAprovacaoId`.
- Toda efetivação automática gera log distinto de execução, além da observação append-only de domínio.
- Falha de efetivação após aprovação deve permanecer diagnosticável sem expor dados sensíveis.

## 8. Measurement Checklist (SC-001..SC-007)
- SC-001: comparar o estado do evento após cancelamento direto e após aprovação.
- SC-002: validar papéis proibidos e escopos inválidos sem alteração persistida.
- SC-003: confirmar observação do tipo `CANCELAMENTO` em todos os caminhos bem-sucedidos.
- SC-004: confirmar que o aprovador é registrado quando a ação é efetivada após aprovação.
- SC-005: confirmar audit trail para solicitação, decisão e resultado.
- SC-006: medir tempo de cancelamento direto em suite/perfil de homologação.
- SC-007: validar que evento cancelado sai do calendário ativo e permanece visível no histórico interno.

## 9. Final Validation Run
1. Executar bateria final focada:
```bash
./gradlew :app:test --tests '*CancelEvento*' --tests '*Aprovacao*' --tests '*Visibility*' --tests '*LifecycleTransitionRegressionTest'
```
2. Resultado esperado para fechamento da feature: `BUILD SUCCESSFUL`.

## 10. Evidence Log (2026-04-12)
### 10.1 Focused Regression Battery
- Comando executado:
```bash
./gradlew :app:test --tests 'br.com.nsfatima.calendario.contract.EventoMutacaoContractTest' --tests 'br.com.nsfatima.calendario.contract.AprovacoesContractTest' --tests 'br.com.nsfatima.calendario.contract.LifecycleTransitionRegressionTest' --tests 'br.com.nsfatima.calendario.integration.eventos.CancelEvento*IntegrationTest' --tests 'br.com.nsfatima.calendario.integration.eventos.ApproveCancelEventoIntegrationTest' --tests 'br.com.nsfatima.calendario.integration.eventos.RejectCancelEventoIntegrationTest'
```
- Resultado observado: `BUILD SUCCESSFUL`.

### 10.2 Tier 1 Performance Evidence (SC-006)
- Comando executado:
```bash
./gradlew :app:test --tests 'br.com.nsfatima.calendario.performance.CancelEventoTier1PerformanceTest'
```
- Métrica validada no teste: latência do `DELETE /api/v1/eventos/{eventoId}` para cancelamento direto medida em tempo de execução do request e validada com limiar `<= 2000ms`.

### 10.3 SC Coverage Mapping
- SC-001: cobertura por `CancelEventoImmediateIntegrationTest` e `ApproveCancelEventoIntegrationTest` confirma estado final `CANCELADO` no fluxo direto e no pós-aprovação.
- SC-002: cobertura por `CancelEventoAuthorizationIntegrationTest` e `CancelEventoOrganizationScopeIntegrationTest` valida `FORBIDDEN` sem mutação.
- SC-003: cobertura por `CancelEventoImmediateIntegrationTest` e `CancelEventoObservacaoIntegrationTest` valida observação append-only `CANCELAMENTO`.
- SC-004: cobertura por `ApproveCancelEventoIntegrationTest` e `CancelEventoAuditTrailIntegrationTest` valida registro do aprovador e vínculo com solicitação.
- SC-005: cobertura por `CancelEventoAuditTrailIntegrationTest` e `CancelEventoApprovalExecutionFailureIntegrationTest` valida trilha de solicitação, decisão, execução e falha segura.
- SC-006: cobertura por `CancelEventoTier1PerformanceTest` valida `<= 2s` para cancelamento direto em Tier 1.
- SC-007: cobertura por `CancelledEventoVisibilityIntegrationTest` e `CancelEventoHistoricalLinksPreservationIntegrationTest` valida saída do calendário ativo e preservação no histórico interno autorizado.
