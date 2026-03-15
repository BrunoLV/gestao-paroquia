# Implementation Evidence: Criacao Completa de Evento

## Regression Summary

- Artefato substitui o antigo `checklists/release-readiness.md`, removido por decisao explicita durante a implementacao.
- Evidencias finais devem ser consolidadas aqui para evitar recriar o checklist removido.

## Regression Suites

- Core create/list:
  - `EventosContractTest`
  - `CreateEventoPersistenciaIntegrationTest`
  - `EventoIdempotencyIntegrationTest`
- Domain validation:
  - `CreateEventoValidationIntegrationTest`
  - `CreateEventoConflitoPendingIntegrationTest`
  - `UnknownFieldRejectionIntegrationTest`
  - `ContractValidationErrorIntegrationTest`
  - `EventoOrganizacaoParticipantesIntegrityIntegrationTest`
- Constitutional and compatibility coverage:
  - `PublicVisibilityContractTest`
  - `PublicStatusVisibilityIntegrationTest`
  - `LegacyEventoReadCompatibilityIntegrationTest`
- Observability and operational budget:
  - `EventoCreateAuditIntegrationTest`
  - `DeniedWriteAuditIntegrationTest`
  - `WeeklyMetricsSnapshotIntegrationTest`
  - `ConsultaCalendarioPerformanceTest`

## SC-003 Evidence

- Goal: reduzir o tempo mediano de cadastro em pelo menos 30%.
- Historical limitation: nao havia telemetria pre-feature capturada antes da remocao do fluxo mockado/multioperacao.
- Baseline surrogate: fluxo legado exigia 3 interacoes obrigatorias para concluir o cadastro operacional minimo.
  - 1. `POST /api/v1/eventos`
  - 2. endpoint complementar de participantes/organizacoes
  - 3. endpoint complementar para completar dados dependentes do cadastro
- Post-implementation flow: 1 interacao obrigatoria (`POST /api/v1/eventos` com payload completo).
- Operational delta: reducao de 3 para 1 round-trip obrigatorio, equivalente a 66.7% menos interacoes de cadastro.
- Decision: SC-003 atendido por proxy operacional, com limitacao explicitada por ausencia de baseline historico de telemetria.

## SC-004 Evidence

- Goal: manter p95 de listagem em ate 2s.
- Baseline budget: guardrail historico do plano permaneceu em 2000 ms.
- Post-implementation guardrail:
  - `ConsultaCalendarioPerformanceTest` preserva o budget de `p95 <= 2000 ms`.
  - `GET /api/v1/eventos` passou a usar consulta JPA ordenada (`findAllByOrderByInicioUtcAscIdAsc`) sem mock.
  - Suites autenticadas de listagem validam a resposta funcional após create e leitura legada.
- Decision: SC-004 protegido por teste automatizado de budget e por regressao funcional autenticada; medicao de runtime real continua recomendada em ambiente de homologacao.

## Constitutional Validation

- `GET /api/v1/eventos` permanece autenticado.
- A cobertura constitucional de visibilidade/status foi preservada com testes dedicados, sem relaxar a autenticacao do endpoint.
- Leitura legada permanece suportada com `UNKNOWN_LEGACY` para status obsoletos.

## Follow-up Recommendation

- Registrar metrica real de tempo mediano de cadastro e p95 de listagem em homologacao/operacao para substituir os proxies documentados acima no primeiro ciclo pos-merge.
