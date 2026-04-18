# Data Model: Fechamento de Auditoria e Retrabalho

## 1. AuditTrailQuery
- Purpose: Representar a consulta pública da trilha auditável.
- Fields:
  - `organizacaoId` (UUID, obrigatório)
  - `granularidade` (`DIARIO`, `SEMANAL`, `MENSAL`, `ANUAL`, opcional)
  - `inicio` (Instant ou LocalDateTime canônico, opcional)
  - `fim` (Instant ou LocalDateTime canônico, opcional)
  - `ator` (string/UUID lógico, opcional)
  - `acao` (string enum-like, opcional)
  - `resultado` (string enum-like, opcional)
  - `correlationId` (string, opcional)
- Validation rules:
  - `organizacaoId` é obrigatório.
  - Deve existir exatamente um modo de período: granularidade ou par `inicio/fim`.
  - `inicio` e `fim` devem ser enviados juntos.
  - `fim` deve ser posterior a `inicio`.
  - O solicitante precisa ter acesso ao escopo da organização filtrada.

## 2. AuditTrailRecord
- Purpose: Representar o registro auditável persistido e consultável.
- Backing entity: nova entidade JPA dedicada, por exemplo `AuditoriaOperacaoEntity`.
- Core fields:
  - `id` (UUID)
  - `organizacaoId` (UUID)
  - `recursoTipo` (string: `EVENTO`, `APROVACAO`, `OBSERVACAO`)
  - `recursoId` (UUID/string)
  - `acao` (string controlada)
  - `resultado` (string controlada: `SUCCESS`, `FAILURE`, `DENY`, `PENDING`, `APPROVED`, `REJECTED`, `EXECUTED`)
  - `ator` (string)
  - `atorUsuarioId` (UUID nullable)
  - `correlationId` (string)
  - `detalhesAuditaveis` (map/json serializado)
  - `ocorridoEmUtc` (Instant)
- Invariants:
  - O registro é imutável após persistência.
  - `organizacaoId`, `acao`, `resultado`, `correlationId` e `ocorridoEmUtc` são obrigatórios.
  - A ordenação de leitura é `ocorridoEmUtc ASC, id ASC`.

## 3. AuditTrailView
- Purpose: Projeção retornada por `GET /api/v1/auditoria/eventos/trilha`.
- Fields:
  - `id`
  - `organizacaoId`
  - `recursoTipo`
  - `recursoId`
  - `acao`
  - `resultado`
  - `ator`
  - `correlationId`
  - `ocorridoEmUtc`
  - `detalhesAuditaveis`
- Guarantees:
  - Ordenação determinística.
  - Não expõe detalhes internos sensíveis da infraestrutura.
  - Reflete exatamente o estado persistido e não uma reconstrução heurística de logs.

## 4. ReworkOccurrence
- Purpose: Representar uma ocorrência elegível para o numerador da taxa de retrabalho.
- Source: derivada da trilha auditável persistida e, quando necessário, enriquecida por dados de domínio.
- Fields:
  - `organizacaoId` (UUID)
  - `tipo` (`CANCELAMENTO`, `REAGENDAMENTO`, `TROCA_ORGANIZACAO_RESPONSAVEL`)
  - `eventoId` (UUID)
  - `auditRecordId` (UUID)
  - `ocorridoEmUtc` (Instant)
- Rules:
  - Uma mesma ocorrência administrativa conta uma vez.
  - Eventos técnicos derivados da mesma ação administrativa não podem duplicar o numerador.

## 5. ReworkRateQuery
- Purpose: Representar a consulta da taxa de retrabalho.
- Fields:
  - `organizacaoId` (UUID, obrigatório)
  - `granularidade` (`DIARIO`, `SEMANAL`, `MENSAL`, `ANUAL`, opcional)
  - `inicio` (opcional)
  - `fim` (opcional)
- Rules:
  - Mesmo contrato temporal da `AuditTrailQuery`.
  - `organizacaoId` obrigatório.

## 6. ReworkRateView
- Purpose: Resposta de `GET /api/v1/auditoria/eventos/retrabalho`.
- Fields:
  - `organizacaoId` (UUID)
  - `periodo` (objeto ou texto normalizado)
  - `taxaRetrabalho` (decimal)
  - `numeradorOcorrenciasElegiveis` (long)
  - `denominadorEventosAfetados` (long)
- Guarantees:
  - Quando o denominador for zero, a resposta explicita taxa zero com numerador zero.
  - A fórmula é sempre `numerador / denominador`.

## 7. MetricsSnapshotState
- Purpose: Representar a baseline periódica das métricas constitucionais.
- Existing backing: extensão conceitual do snapshot produzido por `WeeklyMetricsSnapshotJob`.
- Fields:
  - `capturedAtUtc` (Instant)
  - `eventRegistrationLeadTimeMinutesP95` (long)
  - `calendarQueryLatencyMsP95` (long)
  - `approvalExecutionLatencyMsP95` (long)
  - `administrativeReworkRate` (decimal or derived payload)
  - `organizacaoId` (UUID nullable se snapshot agregado não for por organização)
- Rules:
  - O snapshot semanal precisa preservar histórico.
  - A taxa de retrabalho do snapshot deve ser calculável pela mesma semântica do endpoint operacional.

## Relationships
- `AuditTrailQuery` projeta uma coleção de `AuditTrailView` a partir de `AuditTrailRecord`.
- `ReworkRateQuery` usa `ReworkOccurrence` e o conjunto de eventos afetados para compor `ReworkRateView`.
- `AuditTrailRecord` é a fonte primária para derivar `ReworkOccurrence`.
- `MetricsSnapshotState` consolida as métricas constitucionais e referencia o mesmo conceito de retrabalho da `ReworkRateView`.

## State Transitions
- Mutação de domínio coberta:
  - `MUTACAO_INICIADA -> AUDITORIA_PERSISTIDA -> MUTACAO_CONFIRMADA`
  - `MUTACAO_INICIADA -> FALHA_AUDITORIA -> MUTACAO_ABORTADA`
- Consulta auditável:
  - `FILTRO_VALIDO -> LEITURA_ORDENADA -> PROJECAO_DE_RESPOSTA`
- Cálculo de retrabalho:
  - `PERIODO_VALIDADO -> COLETA_OCORRENCIAS -> CALCULO_NUMERADOR_DENOMINADOR -> RESPOSTA`

## Concurrency & Atomicity
- Toda mutação coberta deve persistir estado de negócio e `AuditTrailRecord` na mesma transação lógica.
- Falha na persistência auditável implica rollback integral da mutação.
- Leituras de trilha e retrabalho são somente leitura e devem permanecer determinísticas mesmo com múltiplos registros no mesmo timestamp, usando `id` como desempate.

## Derived Rules
- `reagendamento` é uma ocorrência administrativa distinta apenas quando altera efetivamente a janela temporal do evento.
- `troca de organizacao responsavel` conta para retrabalho apenas quando a organização resultante difere da anterior.
- `cancelamento` conta para retrabalho apenas quando a operação é efetivada e auditada com sucesso.
