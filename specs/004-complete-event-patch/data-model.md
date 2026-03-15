# Data Model: PATCH Completo de Evento

## 1. EventoPatchRequest
- Purpose: Contrato de entrada para atualizacao parcial de evento.
- Fields (todos opcionais no patch, exceto quando regras condicionais exigirem):
  - `titulo` (string)
  - `descricao` (string)
  - `inicio` (datetime)
  - `fim` (datetime)
  - `status` (enum)
  - `adicionadoExtraJustificativa` (string)
  - `canceladoMotivo` (string)
  - `organizacaoResponsavelId` (UUID, mutacao restrita por papel)
  - `participantes` (array<UUID>, mutacao restrita por papel)
  - `aprovacaoId` (UUID opcional para provar aprovacao de mudanca sensivel)
- Validation rules:
  - Payload vazio deve ser rejeitado.
  - Campos desconhecidos devem ser rejeitados deterministicamente.
  - `fim > inicio` quando ambos estiverem presentes ou apos merge com estado atual.
  - `status=ADICIONADO_EXTRA` exige justificativa.
  - Mudanca de data/cancelamento exige aprovacao valida.

## 2. EventoPatchState
- Purpose: Estado de trabalho apos merge parcial, antes da persistencia.
- Backing entity: `EventoEntity`
- Core fields:
  - `id` (UUID)
  - `titulo`, `descricao`
  - `inicioUtc`, `fimUtc`
  - `status`
  - `organizacaoResponsavelId`
  - `canceladoMotivo`
  - `adicionadoExtraJustificativa`
  - `version` (optimistic lock)
- Invariants:
  - Identidade do evento e imutavel.
  - Estado final deve respeitar regras de calendario e autorizacao.
  - Atualizacao falha nao pode deixar estado parcialmente persistido.

## 3. EventoParticipantesPatch
- Purpose: Representar mutacao parcial de organizacoes participantes vinculadas ao evento.
- Backing entity: `EventoEnvolvidoEntity` (N:M com evento)
- Fields:
  - `eventoId` (UUID)
  - `organizacaoId` (UUID)
  - `papelParticipacao` (enum, quando aplicavel)
- Validation rules:
  - Coordenador/vice da organizacao responsavel podem adicionar/remover participantes.
  - Organizacao responsavel nao pode aparecer na lista de participantes.

## 4. EventoOwnershipChangeRule
- Purpose: Regra de dominio para mutacao de `organizacaoResponsavelId`.
- Actors and permissions:
  - Coordenador/vice da organizacao responsavel: proibido alterar organizacao responsavel.
  - Coordenador do conselho ou parroco: permitido alterar organizacao responsavel.
- Validation rules:
  - Sem permissao -> `FORBIDDEN`.
  - Mudanca valida deve manter consistencia com participantes.

## 5. EventoApprovalRecord
- Purpose: Evidencia de aprovacao para alteracoes de data/horario e cancelamento.
- Backing entity: `AprovacaoEntity` (complementada por migration/repository)
- Fields:
  - `id` (UUID)
  - `eventoId` (UUID)
  - `tipoSolicitacao` (enum: AJUSTE_HORARIO, CANCELAMENTO)
  - `aprovadorId` (UUID)
  - `aprovadorPapel` (string)
  - `status` (enum: APROVADA, REJEITADA, PENDENTE)
  - `criadoEmUtc`, `decididoEmUtc`
- Validation rules:
  - Patch sensivel so segue quando existir aprovacao valida e aderente ao tipo de mudanca.
  - Aprovacao ausente/invalida -> `APPROVAL_REQUIRED`.

## 6. EventoPatchResponse
- Purpose: Representacao final do evento apos persistencia bem-sucedida.
- Fields:
  - `id`, `titulo`, `descricao`
  - `inicio`, `fim`
  - `status`
  - `organizacaoResponsavelId`
  - `participantes` (quando contrato expuser)
  - `version` (opcional no contrato publico; obrigatoria internamente para concorrencia)
- Guarantees:
  - Deve refletir estado persistido no banco apos commit.

## 7. EventoPatchAuditEntry
- Purpose: Trilha operacional para toda tentativa de PATCH.
- Fields:
  - `correlationId`
  - `atorId`/`atorPapel`
  - `acao` (`patch`)
  - `alvo` (`eventoId`)
  - `resultado` (`success`/`failure`)
  - `codigoErro` (quando falha)
  - `aprovacaoId` (quando aplicavel)
  - `timestamp`
- Rules:
  - Registro obrigatorio para sucesso e falha.
  - Nao expor dados sensiveis em payload de auditoria.

## Relationships
- `EventoPatchRequest` + `EventoEntity` atual -> `EventoPatchState` (merge parcial).
- `EventoPatchState` pode mutar `EventoEnvolvidoEntity` (0..N) conforme regras de participantes.
- `EventoPatchState` pode requerer `EventoApprovalRecord` valido (0..1) para data/cancelamento.
- Persistencia de `EventoPatchState` gera `EventoPatchResponse` e `EventoPatchAuditEntry`.

## State Transitions (high level)
- `RASCUNHO -> CONFIRMADO` permitido se regras de dominio e permissao satisfeitas.
- `* -> CANCELADO` exige aprovacao valida e motivo quando aplicavel.
- Alteracao de `inicio/fim` exige aprovacao valida antes de persistir.
- Falhas de validacao/permissao/aprovacao mantem estado anterior inalterado.

## Concurrency & Atomicity
- Atualizacao deve ocorrer dentro de transacao unica.
- `@Version` detecta conflito concorrente; conflito resulta em erro deterministico `CONFLICT`.
- Mudancas de evento + participantes + auditoria devem respeitar atomicidade de escrita.
