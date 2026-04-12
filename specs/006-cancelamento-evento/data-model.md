# Data Model: Cancelamento de Evento

## 1. CancelamentoEventoRequest
- Purpose: Contrato de entrada para `DELETE /eventos/{eventoId}`.
- Fields:
  - `motivo` (string, obrigatório, max 2000)
- Validation rules:
  - `motivo` não pode ser nulo, vazio ou apenas whitespace.
  - A requisição só é válida para evento existente em status `CONFIRMADO`.
  - O solicitante precisa estar dentro do conjunto de papéis autorizados e, quando aplicável, dentro do escopo organizacional permitido.

## 2. EventoCancelamentoState
- Purpose: Estado de trabalho do evento no momento da solicitação ou da efetivação.
- Backing entity: `EventoEntity`
- Core fields:
  - `id` (UUID)
  - `status` (`CONFIRMADO` -> `CANCELADO`)
  - `canceladoMotivo` (string)
  - `organizacaoResponsavelId` (UUID)
  - `version` (`@Version`)
- Invariants:
  - Apenas `CONFIRMADO` é elegível.
  - Identidade do evento é imutável.
  - Soft delete preserva vínculos e histórico.
  - Falha de efetivação não pode deixar `status` e observações divergentes.

## 3. CancelamentoSolicitanteContext
- Purpose: Representar o ator que solicita a operação e seu escopo.
- Source: `EventoActorContextResolver`
- Fields:
  - `atorId` (string/login ou UUID, conforme contexto existente)
  - `papel` (string: `coordenador`, `vice-coordenador`, `paroco`, `vigario`, etc.)
  - `tipoOrganizacao` (string: `CONSELHO`, `PASTORAL`, `LAICATO`, `CLERO`)
  - `organizacaoId` (UUID opcional)
- Rules:
  - `paroco`, `conselho-coordenador` e `conselho-vice-coordenador` possuem autoridade transversal e execução imediata.
  - `vigario` possui autoridade transversal apenas para solicitar; depende de aprovação.
  - `coordenador`/`vice-coordenador` de `PASTORAL` ou `LAICATO` só podem solicitar para eventos da própria organização.

## 4. SolicitacaoAprovacaoCancelamento
- Purpose: Registro persistido de uma ação pendente de cancelamento que aguarda decisão administrativa.
- Backing entity: extensão de `AprovacaoEntity` ou entidade adjacente do mesmo subdomínio.
- Core fields:
  - `id` (UUID)
  - `eventoId` (UUID)
  - `tipoSolicitacao` (`CANCELAMENTO`)
  - `status` (`PENDENTE`, `APROVADA`, `REPROVADA`)
  - `motivoCancelamentoSnapshot` (string)
  - `solicitanteId` (string/UUID)
  - `solicitantePapel` (string)
  - `solicitanteTipoOrganizacao` (string)
  - `criadoEmUtc` (Instant)
  - `decididoEmUtc` (Instant nullable)
  - `aprovadorId` (string/UUID nullable)
  - `aprovadorPapel` (string nullable)
- Validation rules:
  - Uma solicitação `REPROVADA` não pode ser reutilizada.
  - Uma solicitação `APROVADA` não pode ser decidida novamente.
  - O snapshot deve ser suficiente para efetivar a ação sem nova chamada do cliente.

## 5. CancelamentoExecutionOutcome
- Purpose: Resultado da efetivação do cancelamento, seja imediata ou após aprovação.
- Fields:
  - `modo` (`IMMEDIATE`, `PENDING_CREATED`, `EXECUTED_AFTER_APPROVAL`, `REJECTED`, `EXECUTION_FAILED`)
  - `eventoId` (UUID)
  - `solicitacaoAprovacaoId` (UUID nullable)
  - `statusFinalEvento` (`CONFIRMADO` ou `CANCELADO`)
  - `errorCode` (nullable)
- Guarantees:
  - `IMMEDIATE` e `EXECUTED_AFTER_APPROVAL` implicam evento persistido como `CANCELADO`.
  - `PENDING_CREATED` implica evento ainda inalterado.
  - `EXECUTION_FAILED` implica decisão administrativa registrada sem mutação do evento.

## 6. ObservacaoEventoCancelamento
- Purpose: Evidência funcional append-only do cancelamento efetivado.
- Backing entity: `ObservacaoEventoEntity`
- Fields:
  - `id` (UUID)
  - `eventoId` (UUID)
  - `usuarioId` (UUID/string do autor efetivo do cancelamento)
  - `tipo` (`CANCELAMENTO`)
  - `conteudo` (string contendo o motivo aprovado)
  - `criadoEmUtc` (Instant)
- Rules:
  - Só é gravada quando a ação é efetivamente executada.
  - O conteúdo deve refletir o motivo capturado na solicitação original.

## 7. CancelamentoAuditEntry
- Purpose: Trilha operacional estruturada para solicitação, decisão e efetivação.
- Fields:
  - `correlationId`
  - `eventoId`
  - `solicitacaoAprovacaoId` (nullable)
  - `atorId`
  - `atorPapel`
  - `acao` (`cancel-request`, `cancel-approve`, `cancel-reject`, `cancel-execute`)
  - `resultado` (`success`, `pending`, `rejected`, `failed`)
  - `codigoErro` (nullable)
  - `aprovadorId` (nullable)
  - `timestamp`
- Rules:
  - Deve existir para toda tentativa e toda decisão.
  - Não deve expor dados sensíveis além do necessário para diagnóstico e rastreabilidade.

## Relationships
- `CancelamentoEventoRequest` + `EventoCancelamentoState` + `CancelamentoSolicitanteContext` resultam em:
  - cancelamento imediato, ou
  - `SolicitacaoAprovacaoCancelamento` pendente.
- `SolicitacaoAprovacaoCancelamento(APROVADA)` + `EventoCancelamentoState` atualizado resultam em `CancelamentoExecutionOutcome(EXECUTED_AFTER_APPROVAL)`.
- `CancelamentoExecutionOutcome` bem-sucedido gera `ObservacaoEventoCancelamento` + `CancelamentoAuditEntry`.

## State Transitions
- Evento:
  - `CONFIRMADO -> CANCELADO` permitido.
  - Qualquer outro estado -> `INVALID_STATUS_TRANSITION`.
- Solicitação de aprovação:
  - `PENDENTE -> APROVADA` aciona tentativa de efetivação automática.
  - `PENDENTE -> REPROVADA` encerra o fluxo sem alterar o evento.
  - `APROVADA` ou `REPROVADA` são terminais.

## Concurrency & Atomicity
- Cancelamento imediato: evento + observação + auditoria em transação única.
- Aprovação com execução automática: decisão da aprovação + tentativa de efetivação + auditoria em transação coordenada, falhando de forma segura se a pré-condição do evento não se mantiver.
- `@Version` do evento deve impedir dupla efetivação concorrente ou overwrite silencioso.
