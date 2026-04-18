# Data Model: Observacoes de Evento com Controle de Tipo e Autoria

## 1. NotaManualCreateRequest
- Purpose: Contrato de entrada para criacao manual de observacao no endpoint publico.
- Fields:
  - `eventoId` (UUID, path)
  - `tipo` (`NOTA`, obrigatorio)
  - `conteudo` (string, obrigatorio, max 4000)
- Validation rules:
  - `usuarioId` nao e aceito no payload; autoria manual e sempre derivada do contexto autenticado.
  - `tipo` deve ser exatamente `NOTA`.
  - `conteudo` nao pode ser nulo, vazio ou somente whitespace.
  - O solicitante deve ter permissao de colaboracao no evento.

## 2. ObservacaoEventoState
- Purpose: Estado persistido da observacao funcional do evento.
- Backing entity: evolucao de `ObservacaoEventoEntity`.
- Core fields:
  - `id` (UUID)
  - `eventoId` (UUID)
  - `usuarioId` (UUID do criador)
  - `tipo` (`NOTA`, `JUSTIFICATIVA`, `APROVACAO`, `REPROVACAO`, `CANCELAMENTO`, `AJUSTE_HORARIO`)
  - `conteudo` (string)
  - `criadoEmUtc` (Instant)
  - `removida` (boolean, default false, aplicavel apenas a `NOTA`)
  - `removidaEmUtc` (Instant nullable)
  - `removidaPorUsuarioId` (UUID nullable)
  - `version` (`@Version`)
- Invariants:
  - Tipos sistêmicos nunca podem ser editados ou removidos por fluxo manual.
  - `removida=true` implica exclusao de projeções funcionais normais.
  - Remocao logica e permitida apenas para `NOTA` e apenas pelo proprio autor.

## 3. ObservacaoListQuery
- Purpose: Representar a semantica dos dois modos funcionais de consulta.
- Fields:
  - `eventoId` (UUID)
  - `modo` (`MINHAS`, `TODAS`)
  - `solicitanteId` (UUID)
  - `includeRemovedForAudit` (boolean, default false)
- Rules:
  - `modo=MINHAS` retorna apenas observacoes cujo `usuarioId == solicitanteId`.
  - `modo=TODAS` exige permissao de leitura ampla no evento.
  - `includeRemovedForAudit=false` exclui observacoes `NOTA` removidas logicamente.
  - Consultas de auditoria com `includeRemovedForAudit=true` nao fazem parte da projeção funcional padrão e sao restritas a uso interno.

## 4. ObservacaoView
- Purpose: Projecao funcional retornada por criacao/listagem.
- Fields:
  - `id` (UUID)
  - `eventoId` (UUID)
  - `usuarioId` (UUID)
  - `tipo` (enum de resposta)
  - `conteudo` (string)
  - `criadoEmUtc` (Instant)
- Guarantees:
  - Sempre inclui autoria e data/hora de criacao.
  - Nao inclui notas removidas logicamente nas consultas funcionais padrão.
  - Ordenacao e deterministica por `criadoEmUtc` ascendente e `id` ascendente.

## 5. NotaRevisionEntry
- Purpose: Registrar trilha de revisao de conteudo de `NOTA`.
- Fields:
  - `id` (UUID)
  - `observacaoId` (UUID)
  - `conteudoAnterior` (string)
  - `conteudoNovo` (string)
  - `revisadoPorUsuarioId` (UUID)
  - `revisadoEmUtc` (Instant)
- Rules:
  - Criada a cada edicao bem-sucedida de `NOTA`.
  - Deve preservar conteudo anterior integral para auditoria.
  - Nao se aplica a tipos sistêmicos.

## 6. ObservacaoSystemActionCommand
- Purpose: Comando interno usado por fluxos sistêmicos para registrar observacoes reservadas.
- Fields:
  - `eventoId` (UUID)
  - `usuarioId` (UUID do ator humano ou usuario tecnico fallback)
  - `tipo` (`JUSTIFICATIVA`, `APROVACAO`, `REPROVACAO`, `CANCELAMENTO`, `AJUSTE_HORARIO`)
  - `conteudoOrigem` (string)
  - `originFlow` (string: `cancelamento`, `aprovacao`, `reprovacao`, `ajuste-horario`, etc.)
  - `criadoEmUtc` (Instant)
- Rules:
  - Nao pode ser originado por endpoint manual de observacao.
  - `conteudoOrigem` deve espelhar a justificativa, motivo ou decisao do fluxo de origem.
  - `usuarioId` deve refletir ator humano quando presente; fallback tecnico apenas sem ator humano.

## 7. NotaAuthorPolicy
- Purpose: Regra derivada para autorizacao de edicao e exclusao.
- Inputs:
  - `observacao.usuarioId`
  - `observacao.tipo`
  - `observacao.removida`
  - `solicitanteId`
- Rules:
  - Editar exige `tipo=NOTA`, `removida=false` e `solicitanteId == usuarioId`.
  - Excluir exige `tipo=NOTA`, `removida=false` e `solicitanteId == usuarioId`.
  - Tipos sistêmicos sao sempre imutaveis no fluxo manual.

## Relationships
- `NotaManualCreateRequest` gera `ObservacaoEventoState(tipo=NOTA)` e `ObservacaoView`.
- `ObservacaoEventoState(tipo=NOTA)` pode gerar zero ou muitos `NotaRevisionEntry`.
- `ObservacaoListQuery` projeta `ObservacaoView` a partir de `ObservacaoEventoState` com filtros por modo e remocao logica.
- `ObservacaoSystemActionCommand` gera `ObservacaoEventoState(tipo sistêmico)` sem passar pelo fluxo manual.

## State Transitions
- Observacao manual:
  - `CRIADA -> EDITADA` (uma ou mais vezes, com revisoes auditaveis)
  - `CRIADA|EDITADA -> REMOVIDA_LOGICAMENTE`
- Observacao sistêmica:
  - `CRIADA -> IMUTAVEL`
- Consultas funcionais:
  - `ATIVA` visivel em `MINHAS` e/ou `TODAS` conforme permissao
  - `REMOVIDA_LOGICAMENTE` invisivel em consultas funcionais e visivel apenas em trilha interna de auditoria

## Concurrency & Atomicity
- Criacao manual de `NOTA`: observacao + auditoria operacional em transacao unica.
- Edicao de `NOTA`: update da observacao + insercao de `NotaRevisionEntry` + auditoria em transacao unica.
- Exclusao logica de `NOTA`: update de estado de remocao + auditoria em transacao unica.
- Criacao sistêmica: observacao + auditoria do fluxo de origem em transacao coordenada com a acao de negocio correspondente.
- `@Version` deve prevenir overwrite silencioso em edicoes concorrentes.
