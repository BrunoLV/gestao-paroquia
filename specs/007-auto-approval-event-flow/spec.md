# Feature Specification: Execucao Automatica Pos-Aprovacao de Evento

**Feature Branch**: `007-auto-approval-event-flow`  
**Created**: 2026-04-12  
**Status**: Draft  
**Input**: User description: "Ajuste de fluxo de criacao e edicao de eventos que precisam de autorizacao. Para criacao e edicao deve funcionar como no cancelamento. Apos a aprovacao nao e cliente que deve reexecutar a acao, mas o sistema deve aplicar o que e necessario de forma automatica apos a autorizacao."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Criar Evento Com Aprovacao Assincrona (Priority: P1)

Como agente pastoral que depende de autorizacao, quero solicitar criacao de evento sem falha imediata, para que a criacao seja executada automaticamente quando a aprovacao for concedida.

**Why this priority**: Resolve a principal friccao operacional: hoje a pessoa solicitante precisa repetir uma acao manual apos aprovacao.

**Independent Test**: Enviar criacao com perfil que exige aprovacao, aprovar a solicitacao, e verificar que o evento foi criado sem novo POST do cliente.

**Acceptance Scenarios**:

1. **Given** uma solicitacao de criacao pendente, **When** um aprovador autorizado decide `APROVADA`, **Then** o sistema cria automaticamente o evento com os dados aprovados e retorna resultado de execucao no fluxo de aprovacao.
2. **Given** uma solicitacao de criacao pendente, **When** um aprovador autorizado decide `REPROVADA`, **Then** o sistema encerra a solicitacao sem criar evento e preserva trilha auditavel da decisao.

---

### User Story 2 - Editar Evento Com Aplicacao Automatica (Priority: P1)

Como agente que precisa alterar horario/campos sensiveis, quero submeter uma solicitacao unica de edicao para que, apos aprovacao, o sistema aplique automaticamente o patch sem exigir novo envio do cliente.

**Why this priority**: Impacta diretamente o fluxo cotidiano de manutencao de agenda e reduz retrabalho administrativo.

**Independent Test**: Enviar edicao sensivel sem aprovacao imediata (gera pendencia), aprovar solicitacao, e confirmar que as alteracoes foram aplicadas automaticamente no evento.

**Acceptance Scenarios**:

1. **Given** uma solicitacao de edicao pendente, **When** a decisao e `APROVADA`, **Then** o sistema aplica automaticamente as alteracoes aprovadas no evento correspondente.
2. **Given** uma solicitacao de edicao pendente, **When** a decisao e `REPROVADA`, **Then** o evento permanece inalterado e a solicitacao e finalizada com status de reprovacao.

---

### User Story 3 - Operacao Segura e Rastreavel (Priority: P2)

Como administracao paroquial, quero que aprovacao, execucao automatica e falhas fiquem auditaveis com diagnostico deterministico, para garantir governanca e suporte operacional.

**Why this priority**: Necessario para confiabilidade, auditoria e depuracao do fluxo assincorno sem interacao manual posterior.

**Independent Test**: Simular aprovacao com sucesso e aprovacao com falha de pre-condicao; validar auditoria, metricas e codigos de erro esperados.

**Acceptance Scenarios**:

1. **Given** uma solicitacao aprovada que falha na execucao automatica, **When** a execucao e tentada, **Then** a API retorna erro deterministico, registra auditoria de falha, e mantem consistencia de dados sem aplicar mudanca parcial.

### Edge Cases

- Solicitacao aprovada tenta criar evento duplicado por idempotencia ja consumida: sistema deve manter resultado deterministico e nao duplicar registros.
- Solicitacao aprovada de edicao referencia evento removido logicamente ou inexistente: sistema deve falhar com erro rastreavel e sem efeitos colaterais em outras entidades.
- Decisao repetida para a mesma solicitacao (ja decidida): sistema deve rejeitar tentativa com codigo de erro de conflito.
- Payload pendente com dados invalidos por mudanca de regra entre solicitacao e execucao: sistema deve falhar de forma explicita e auditada.
- Aprovacao concorrente de duas solicitacoes que alteram o mesmo evento: sistema deve garantir aplicacao consistente com controle de concorrencia existente.

## API Contract & Validation *(mandatory)*

- Endpoint afetado `POST /api/v1/eventos`: **changed** para suportar modo `APPROVAL_PENDING` quando o ator exigir autorizacao previa para criacao.
- Endpoint afetado `PATCH /api/v1/eventos/{eventoId}`: **changed** para permitir submissao de alteracao sensivel sem `aprovacaoId` imediato, retornando solicitacao pendente para execucao posterior automatica.
- Endpoint afetado `PATCH /api/v1/aprovacoes/{id}`: **changed** para executar automaticamente a acao pendente de `CRIACAO_EVENTO` e `EDICAO_EVENTO` apos decisao `APROVADA`, com resultado de execucao no corpo da resposta.
- Endpoint `POST /api/v1/aprovacoes`: **unchanged** para criacao explicita de solicitacoes quando aplicavel a fluxos existentes.
- Erros de maquina obrigatorios: `APPROVAL_REQUIRED`, `APPROVAL_NOT_FOUND`, `APPROVAL_ALREADY_DECIDED`, `APPROVAL_EXECUTION_FAILED`, `CONFLICT`, `INVALID_STATUS_TRANSITION`.
- Regra explicita de `APPROVAL_REQUIRED`: aplica-se apenas ao caminho de compatibilidade em que o cliente tenta forcar execucao imediata de mutacao sensivel sem autorizacao valida; no novo caminho pendente padrao (`APPROVAL_PENDING`), a ausencia de `aprovacaoId` nao e erro.
- Validacoes obrigatorias: consistencia de intervalo de data/hora, validacao de status de evento, obrigatoriedade de justificativa para `ADICIONADO_EXTRA`, e integridade de escopo organizacional.
- Backward compatibility:
- Chamadas antigas de criacao/edicao para atores com permissao imediata permanecem com comportamento atual de execucao sincrona.
- Consumidores que hoje reenviam PATCH apos aprovacao continuam funcionais, mas esse reenvio passa a ser opcional para fluxos cobertos por execucao automatica.

## Calendar Integrity Rules *(mandatory for calendar/event features)*

- Estrategia canonica de tempo: persistencia em UTC com semantica funcional da paroquia preservada para comparacoes e exibicao.
- Regras de conflito permanecem nao bloqueantes para cadastro quando politicas existentes permitirem, mas devem ser reavaliadas no momento de execucao automatica para evitar gravacao inconsistente.
- Ordenacao e consulta de eventos permanecem deterministicas por faixa temporal e status.
- Execucao automatica pos-aprovacao nao pode violar transicoes validas de ciclo de vida (`RASCUNHO`, `CONFIRMADO`, `CANCELADO`, `ADICIONADO_EXTRA`).

## Operational Observability *(mandatory)*

- Cada etapa do fluxo deve gerar trilha auditavel: solicitacao recebida, pendencia criada, decisao recebida, execucao automatica iniciada, execucao concluida ou falha.
- Correlation id deve conectar solicitacao original, aprovacao e acao aplicada no evento.
- Metricas obrigatorias:
- volume de solicitacoes pendentes por tipo (`CRIACAO_EVENTO`, `EDICAO_EVENTO`, `CANCELAMENTO`),
- taxa de execucao automatica com sucesso,
- taxa de falha de execucao pos-aprovacao,
- tempo entre criacao de solicitacao e efetivacao.
- baseline constitucional minima tambem deve cobrir `event_registration_lead_time_minutes`, `calendar_query_latency_ms` e `administrative_rework_indicator`.
- Diagnostico de erro deve expor codigo deterministico e contexto operacional minimo sem dados sensiveis.

## Architecture and Code Standards *(mandatory)*

- Camadas:
- dominio define regras de integridade e autorizacao,
- aplicacao orquestra solicitacao/decisao/execucao,
- infraestrutura persiste snapshots e publica auditoria/metricas.
- Em abordagem hexagonal, ports de aprovacao e de mutacao de evento devem permitir reutilizacao do mesmo fluxo para criacao, edicao e cancelamento.
- Acoes pendentes devem ser representadas de forma explicita e versionada, com payload imutavel apos criacao da solicitacao.
- Tratamento de erro deve manter semanticamente a diferenca entre: negacao de autorizacao, solicitacao nao encontrada/ja decidida e falha de execucao automatica.
- Padroes de transacao devem evitar aplicacao parcial: ou a acao automatica e concluida com sucesso, ou falha com estado rastreavel e sem mutacao indevida.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST suportar solicitacao de criacao de evento em modo pendente quando o ator nao possuir permissao de efetivacao imediata.
- **FR-002**: O sistema MUST armazenar snapshot completo e imutavel da acao pendente para criacao de evento.
- **FR-003**: O sistema MUST suportar solicitacao de edicao sensivel em modo pendente sem exigir novo envio manual apos aprovacao.
- **FR-004**: O sistema MUST armazenar snapshot completo e imutavel das alteracoes solicitadas para edicao de evento.
- **FR-005**: O sistema MUST executar automaticamente a criacao pendente quando a solicitacao correspondente for decidida como `APROVADA`.
- **FR-006**: O sistema MUST executar automaticamente a edicao pendente quando a solicitacao correspondente for decidida como `APROVADA`.
- **FR-007**: O sistema MUST finalizar a solicitacao como reprovada e nao aplicar mutacao de evento quando a decisao for `REPROVADA`.
- **FR-008**: O sistema MUST retornar no resultado da decisao um resumo padronizado da execucao automatica (`EXECUTED`, `REJECTED`, `FAILED`) com referencia ao alvo afetado.
- **FR-009**: O sistema MUST impedir nova decisao para solicitacao ja decidida.
- **FR-010**: O sistema MUST aplicar controles de autorizacao para decidir solicitacoes conforme papeis e escopo organizacional vigentes.
- **FR-011**: O sistema MUST aplicar as mesmas validacoes de dominio da operacao original no momento da execucao automatica pos-aprovacao.
- **FR-012**: O sistema MUST manter consistencia transacional, sem aplicacao parcial da acao automatica.
- **FR-013**: O sistema MUST registrar auditoria estruturada para solicitacao, decisao, execucao e falha de execucao.
- **FR-014**: O sistema MUST emitir metricas operacionais de throughput, sucesso/falha e tempo de processamento do fluxo de autorizacao.
- **FR-015**: O sistema MUST preservar compatibilidade do comportamento atual para atores com permissao de execucao imediata.
- **FR-016**: O sistema MUST manter a estrategia de idempotencia de criacao durante execucao automatica para evitar duplicidade.
- **FR-017**: O sistema MUST devolver erro deterministico `APPROVAL_EXECUTION_FAILED` quando a solicitacao estiver aprovada, mas a execucao automatica nao puder ser concluida.
- **FR-018**: O sistema MUST permitir rastrear cada solicitacao por correlation id e vinculo ao evento resultante (quando houver).

### Key Entities *(include if feature involves data)*

- **SolicitacaoAprovacao**: registro de autorizacao com identificador, tipo de acao, status de decisao, solicitante/aprovador, timestamps, correlation id e metadados de execucao.
- **AcaoPendenteEvento**: snapshot imutavel da operacao solicitada (criacao ou edicao), incluindo dados necessarios para execucao automatica posterior.
- **ResultadoExecucaoAprovacao**: resultado operacional da tentativa automatica (`EXECUTED`, `REJECTED`, `FAILED`) com referencia ao recurso alvo e codigo de erro quando aplicavel.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das solicitacoes aprovadas de criacao no escopo da feature sao efetivadas sem necessidade de nova chamada manual do cliente.
- **SC-002**: 100% das solicitacoes aprovadas de edicao no escopo da feature sao efetivadas sem necessidade de nova chamada manual do cliente.
- **SC-003**: Pelo menos 95% das execucoes automatizadas pos-aprovacao concluem em ate 60 segundos apos a decisao.
- **SC-004**: 100% das decisoes (`APROVADA` ou `REPROVADA`) possuem trilha auditavel completa da solicitacao ao resultado final.
- **SC-005**: 100% das falhas de execucao pos-aprovacao retornam codigo de erro deterministico e nao aplicam mutacao parcial.

## Measurement and Evidence Plan *(mandatory)*

- SC-001 e SC-002: validar por testes de integracao de ponta a ponta com evidencias de que nao houve segunda chamada de criacao/edicao apos aprovacao.
- SC-003: medir diferenca de tempo entre timestamp de decisao e timestamp de execucao efetiva em amostra semanal operacional.
- SC-004: evidenciar logs de auditoria correlacionados por correlation id cobrindo solicitacao, decisao e resultado.
- SC-005: evidenciar testes de falha controlada com verificacao de codigo de erro, estado final consistente e ausencia de alteracao parcial.
- Cada PR que altere o fluxo deve incluir evidencias de: cenarios aprovados, reprovados e falha de execucao automatica.

## Assumptions and Dependencies

- A politica de autorizacao por papel/escopo organizacional permanece a referencia para decidir quando uma acao exige aprovacao previa.
- O modulo de aprovacoes permanece o orquestrador unico de decisao e de disparo de execucao pos-aprovacao.
- A instrumentacao de auditoria e metricas atual sera estendida sem remover eventos operacionais ja existentes.
