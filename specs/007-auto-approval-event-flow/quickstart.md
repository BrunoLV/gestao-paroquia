# Quickstart: Execucao Automatica Pos-Aprovacao para Criacao e Edicao

## 1. Prerequisites
- Java 21
- Gradle Wrapper (`./gradlew`)
- Perfil de teste configurado no modulo `app`

## 2. Baseline
```bash
./gradlew :app:build
```

## 3. Implementation Checklist
1. Evoluir fluxo de `POST /api/v1/eventos` para criar solicitacao pendente quando autorizacao previa for necessaria.
2. Evoluir fluxo de `PATCH /api/v1/eventos/{eventoId}` para criar solicitacao pendente de edicao sensivel sem exigir reenvio manual posterior.
3. Garantir snapshot imutavel de acao pendente (`CRIACAO_EVENTO` e `EDICAO_EVENTO`) no subdominio de aprovacao.
4. Estender `PATCH /api/v1/aprovacoes/{id}` para executar automaticamente criacao/edicao ao aprovar.
5. Preservar comportamento sincrono atual para perfis com permissao imediata.
6. Garantir auditoria e metricas por etapa (pending, approved, rejected, executed, failed).

## 4. Focused Validation
1. Criacao pendente para perfil com autorizacao previa:
```bash
./gradlew :app:test --tests '*CreateEvento*Approval*IntegrationTest'
```
2. Aprovacao executa criacao automaticamente sem novo POST:
```bash
./gradlew :app:test --tests '*ApproveCreateEventoIntegrationTest'
```
3. Edicao sensivel pendente para perfil com autorizacao previa:
```bash
./gradlew :app:test --tests '*UpdateEvento*ApprovalPending*IntegrationTest'
```
4. Aprovacao executa edicao automaticamente sem novo PATCH:
```bash
./gradlew :app:test --tests '*ApproveUpdateEventoIntegrationTest'
```
5. Reprovacao nao altera recurso alvo:
```bash
./gradlew :app:test --tests '*Reject*Evento*IntegrationTest'
```
6. Falha segura pos-aprovacao com erro deterministico:
```bash
./gradlew :app:test --tests '*ApprovalExecutionFailure*IntegrationTest'
```

## 5. Manual API Smoke
1. Autenticar com perfil que exige aprovacao para criacao/edicao.
2. Enviar criacao de evento e confirmar retorno `APPROVAL_PENDING`.
3. Aprovar solicitacao em `PATCH /api/v1/aprovacoes/{id}` e validar evento criado sem novo POST.
4. Enviar edicao sensivel e confirmar retorno `APPROVAL_PENDING`.
5. Aprovar solicitacao e validar alteracao aplicada sem novo PATCH.
6. Repetir com `REPROVADA` e validar ausencia de mutacao.

## 6. Contract Checklist
- `POST /api/v1/eventos`: retorno pendente quando aplicavel.
- `PATCH /api/v1/eventos/{eventoId}`: retorno pendente para mudancas sensiveis quando aplicavel.
- `PATCH /api/v1/aprovacoes/{id}`: retorna status da decisao e outcome operacional de execucao.
- Erros obrigatorios: `APPROVAL_REQUIRED`, `APPROVAL_ALREADY_DECIDED`, `APPROVAL_NOT_FOUND`, `APPROVAL_EXECUTION_FAILED`, `FORBIDDEN`, `CONFLICT`.

## 7. Observability Checklist
- Log estruturado para criacao de solicitacao pendente.
- Log estruturado para decisao de aprovacao/reprovacao.
- Log estruturado para execucao automatica e falha.
- Correlation id consistente do request original ate o resultado.

## 8. Final Validation Run
```bash
./gradlew :app:test --tests '*Aprovacao*' --tests '*CreateEvento*' --tests '*UpdateEvento*' --tests '*LifecycleTransitionRegressionTest'
```

## 9. Evidencias Executadas (2026-04-12)

### 9.1 US3 Focus Pack
Comando executado:

```bash
./gradlew :app:test \
	--tests 'br.com.nsfatima.calendario.integration.eventos.CreateEventoApprovalAuditTrailIntegrationTest' \
	--tests 'br.com.nsfatima.calendario.integration.eventos.UpdateEventoApprovalAuditTrailIntegrationTest' \
	--tests 'br.com.nsfatima.calendario.integration.eventos.ApprovalAutoExecutionFailureConsistencyIntegrationTest' \
	--tests 'br.com.nsfatima.calendario.contract.AprovacoesDecisionExecutionContractTest' \
	--tests 'br.com.nsfatima.calendario.integration.eventos.UpdateEventoApprovalExecutionFailureIntegrationTest'
```

Resultado: `BUILD SUCCESSFUL`.

### 9.2 Regressao Final de Fluxo
Comando executado:

```bash
./gradlew :app:test --tests 'br.com.nsfatima.calendario.integration.eventos.ApprovalFlowRegressionSuite'
```

Resultado: `BUILD SUCCESSFUL`.

### 9.3 Verificacao de SC-003 (p95 <= 60s)
Durante os cenarios de aprovacao, a metrica `approval_execution_latency_ms` foi registrada com valores em milissegundos (por exemplo, 13 ms), abaixo do alvo de 60.000 ms.
