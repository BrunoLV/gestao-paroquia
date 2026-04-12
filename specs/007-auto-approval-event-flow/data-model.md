# Data Model: Execucao Automatica Pos-Aprovacao para Criacao e Edicao

## 1. SolicitacaoAprovacao
- Purpose: Registro administrativo de solicitacao e decisao.
- Backing entity: `AprovacaoEntity` (estendida para suportar snapshots de criacao/edicao).
- Fields:
  - `id` (UUID)
  - `eventoId` (UUID nullable para criacao ate efetivacao)
  - `tipoSolicitacao` (`CRIACAO_EVENTO`, `EDICAO_EVENTO`, `CANCELAMENTO`)
  - `status` (`PENDENTE`, `APROVADA`, `REPROVADA`)
  - `solicitanteId`, `solicitantePapel`, `solicitanteTipoOrganizacao`
  - `aprovadorId`, `aprovadorPapel`
  - `criadoEmUtc`, `decididoEmUtc`, `executadoEmUtc`
  - `decisionObservacao`
  - `correlationId`
  - `actionPayloadJson` (snapshot imutavel da acao)
- Invariants:
  - Apenas `PENDENTE` pode transitar para `APROVADA` ou `REPROVADA`.
  - `APROVADA` e `REPROVADA` sao estados terminais.
  - Snapshot da acao deve ser imutavel apos criacao da solicitacao.

## 2. AcaoPendenteCriacaoEvento
- Purpose: Snapshot da criacao aprovada a ser executada automaticamente.
- Stored in: `actionPayloadJson` quando `tipoSolicitacao=CRIACAO_EVENTO`.
- Fields:
  - `titulo`, `descricao`
  - `organizacaoResponsavelId`
  - `inicio`, `fim`
  - `status`
  - `adicionadoExtraJustificativa`
  - `participantes`
  - `idempotencyKey` (quando aplicavel)
- Validation:
  - Mesmo conjunto de validacoes de create sincrono.
  - Regras de calendario e status devem ser reavaliadas no momento da efetivacao.

## 3. AcaoPendenteEdicaoEvento
- Purpose: Snapshot da edicao sensivel aprovada a ser aplicada automaticamente.
- Stored in: `actionPayloadJson` quando `tipoSolicitacao=EDICAO_EVENTO`.
- Fields:
  - `eventoId`
  - Campos mutados (`inicio`, `fim`, `status`, `canceladoMotivo`, `organizacaoResponsavelId`, `participantes`, etc.)
  - `requestedAtVersion` (opcional para rastrear concorrencia)
- Validation:
  - Reaplicar politicas de autorizacao e validacoes de dominio no instante da execucao.
  - Nao permitir aplicacao parcial.

## 4. ResultadoExecucaoAprovacao
- Purpose: Representar resultado funcional da decisao + tentativa de execucao automatica.
- Response model:
  - `outcome`: `EXECUTED`, `REJECTED`, `FAILED`
  - `targetResourceId`: UUID do evento (quando houver)
  - `targetStatus`: status final do evento (quando houver)
  - `errorCode`: nullable (`APPROVAL_EXECUTION_FAILED`, etc.)
- Guarantees:
  - `REJECTED` nao altera o recurso alvo.
  - `EXECUTED` implica mutacao persistida e auditada.
  - `FAILED` implica falha segura sem mutacao parcial.

## 5. EventoState (reuso)
- Purpose: Estado funcional de evento no momento da efetivacao automatica.
- Backing entity: `EventoEntity`.
- Relevant fields:
  - `id`, `status`, `inicioUtc`, `fimUtc`, `organizacaoResponsavelId`, `version`
- Invariants:
  - Lifecycle e regras de integridade temporal permanecem obrigatorios.
  - Controle de concorrencia otimista deve prevenir overwrite silencioso.

## Relationships
- `SolicitacaoAprovacao` referencia exatamente um tipo de acao pendente.
- `AcaoPendenteCriacaoEvento` e `AcaoPendenteEdicaoEvento` sao especializacoes lógicas de snapshot em `actionPayloadJson`.
- `ResultadoExecucaoAprovacao` e derivado da decisao + tentativa de efetivacao.

## State Transitions
- Solicitacao:
  - `PENDENTE -> APROVADA` dispara execucao automatica da acao snapshot.
  - `PENDENTE -> REPROVADA` finaliza fluxo sem mutacao.
- Resultado operacional apos aprovacao:
  - `APROVADA + execucao ok -> EXECUTED`
  - `APROVADA + execucao falha -> FAILED`

## Concurrency and Atomicity
- Decisao e marcacao de status da solicitacao ocorrem em transacao com tentativa de execucao.
- Falha de execucao nao deve deixar estado de evento parcialmente aplicado.
- Fluxo de criacao automatica deve respeitar idempotencia para evitar duplicidade.
