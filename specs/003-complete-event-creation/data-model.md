# Data Model: Criacao Completa de Evento

## 1. EventoCompleto
- Purpose: Representar a entidade principal criada em unica operacao.
- Backing table: `calendario.eventos`
- Fields:
  - `id` (UUID, PK, generated)
  - `titulo` (string, required, max 160)
  - `descricao` (string, optional, max 4000)
  - `organizacaoResponsavelId` (UUID, required)
  - `inicioUtc` (Instant, required)
  - `fimUtc` (Instant, required)
  - `status` (enum/string persistido, required)
  - `canceladoMotivo` (string, optional, max 2000)
  - `adicionadoExtraJustificativa` (string, optional, max 4000)
  - `version` (long, optimistic locking)
- Validation rules:
  - `titulo` obrigatorio e nao vazio.
  - `fimUtc` deve ser maior que `inicioUtc`.
  - `status=ADICIONADO_EXTRA` exige `adicionadoExtraJustificativa` nao vazia.
  - `organizacaoResponsavelId` obrigatorio.

## 2. EventoCreateRequestCompleto
- Purpose: Contrato de entrada para criar evento completo em uma unica requisicao.
- Fields:
  - `titulo` (string, required)
  - `descricao` (string, optional)
  - `organizacaoResponsavelId` (UUID, required)
  - `inicio` (datetime, required)
  - `fim` (datetime, required)
  - `status` (enum, optional default `RASCUNHO`)
  - `adicionadoExtraJustificativa` (string, conditional)
  - `participantes` (array<UUID>, optional)
- Validation rules:
  - Rejeitar campos desconhecidos no payload.
  - Aplicar normalizacao e validacao de status.
  - Em status `ADICIONADO_EXTRA`, justificativa obrigatoria.

## 3. EventoCreateResponse
- Purpose: Retornar representacao canonica do evento criado.
- Fields:
  - `id` (UUID)
  - `titulo` (string)
  - `descricao` (string|null)
  - `inicio` (datetime)
  - `fim` (datetime)
  - `status` (enum de resposta)
  - `organizacaoResponsavelId` (UUID)
  - `conflito` (object optional, quando `CONFLICT_PENDING`)

## 4. EventoListItem
- Purpose: Representar item de listagem persistida em `GET /api/v1/eventos`.
- Fields:
  - `id`, `titulo`, `inicio`, `fim`, `status`, `organizacaoResponsavelId`
  - `conflictState` (enum: `NONE`, `CONFLICT_PENDING`)
- Ordering rules:
  - Ordenar por `inicio` ascendente e `id` como desempate.

## 5. EventoConflito
- Purpose: Capturar estado de conflito detectado no create.
- Fields:
  - `eventoId` (UUID)
  - `estado` (enum: `CONFLICT_PENDING`, required)
  - `motivo` (string, required)
  - `criadoEm` (Instant)
- Transition rules:
  - Entrada: detectado durante create com sobreposicao.
  - Saida: resolucao administrativa posterior (fora deste escopo de implementacao).

## 6. EventoIdempotencyRecord
- Purpose: Garantir replay seguro da criacao por `Idempotency-Key`.
- Suggested storage model:
  - `idempotencyKey` (string, PK logical, required)
  - `requestHash` (string, required)
  - `eventoId` (UUID, required)
  - `responseStatus` (int, required)
  - `createdAt` (Instant, required)
- Validation rules:
  - Mesma chave + mesmo hash -> retorna resposta original.
  - Mesma chave + hash diferente -> erro de conflito de idempotencia.

## 7. EventoAuditEntry
- Purpose: Rastrear resultado operacional do create/list.
- Fields:
  - `correlationId` (string)
  - `ator` (string)
  - `acao` (string: `create`/`list`)
  - `alvo` (string: `eventoId` quando houver)
  - `resultado` (enum: `success`, `failure`)
  - `codigoErro` (string optional)
  - `timestamp` (Instant)

## Relationships
- `EventoCreateRequestCompleto` -> cria `EventoCompleto` (1:1 por operacao)
- `EventoCompleto` -> pode gerar `EventoConflito` (0..1)
- `EventoCreateRequestCompleto` + `Idempotency-Key` -> `EventoIdempotencyRecord` (1:1 por chave)
- `EventoCompleto` -> `EventoAuditEntry` (1:N, por operacao)

## State Notes
- `status` inicial default: `RASCUNHO` quando nao informado.
- Se houver sobreposicao valida de regra, persistir com marcador operacional de conflito pendente.
- Persistencia e idempotencia devem ocorrer de forma atomica na transacao de criacao.
