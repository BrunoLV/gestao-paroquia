# Feature Specification: Cancelamento de Evento

**Feature Branch**: `006-cancelamento-evento`  
**Created**: 2026-04-12  
**Status**: Draft  
**Input**: User description: "Implementação da funcionalidade Cancelamento de Evento. A mecânica a ser seguida é a de Soft Delete, o motivo deve ser informado sendo obrigatório. Referente a papéis de usuários solicitantes: quando ação disparada por coordenador, vice-coordenador (CONSELHO) ou paroco (CLERO) pode ser executado sem passar por aprovação pois são usuários com plenos poderes no sistema. quando ação disparada por coordenador, vice-coordenador (PASTORAL ou LAICATO) ou vigario (CLERO) deve ser aprovado por parroco ou coordenador ou vice-coordenador do conselho. demais papéis não podem cancelar eventos. Essa ação deve ser auditada para que tenha-se rastreabilidade da ação."

## Clarifications

### Session 2026-04-12

- Q: Papéis com plenos poderes (sem aprovação) → A: Coordenador e vice-coordenador do CONSELHO, e pároco do CLERO.
- Q: Papéis que requerem aprovação prévia → A: Coordenador e vice-coordenador de PASTORAL ou LAICATO, e vigário do CLERO. Aprovação deve ser concedida por pároco, coordenador do conselho ou vice-coordenador do conselho.
- Q: Papéis que não podem cancelar → A: Todos os demais (membro, secretário, padre, e qualquer papel não relacionado acima).
- Q: Quais status podem ser cancelados? → A: Somente eventos no status `CONFIRMADO` podem ser cancelados; `RASCUNHO`, `ADICIONADO_EXTRA` e `CANCELADO` ficam fora do escopo desta operação.
- Q: Como funciona o escopo organizacional por papel no cancelamento? → A: Pároco, coordenador e vice-coordenador do CONSELHO, e vigário podem solicitar cancelamento de evento de qualquer organização; coordenador e vice-coordenador de PASTORAL ou LAICATO só podem solicitar cancelamento para eventos da própria organização.
- Q: Como funciona o fluxo de aprovação quando ela é necessária? → A: A própria requisição de cancelamento cria a solicitação pendente com os dados da ação; quando a aprovação é concedida, o cancelamento é efetivado automaticamente sem exigir ressubmissão.
- Q: A regra de execução automática após aprovação vale para toda a plataforma nesta entrega? → A: Não. Nesta feature, o escopo está restrito ao cancelamento de evento. A extensão para outras ações que exigem autorização prévia será tratada em feature específica.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cancelamento Direto por Usuário de Plenos Poderes (Priority: P1)

Como coordenador ou vice-coordenador do conselho, ou como pároco, quero cancelar um evento ativo informando o motivo para que o calendário reflita imediatamente a situação real sem necessidade de aprovação adicional.

**Why this priority**: É o fluxo crítico e mais frequente — os usuários com plenos poderes devem conseguir cancelar de forma imediata. Sem este fluxo funcionando, o status `CANCELADO` nunca é persistido.

**Independent Test**: Pode ser testado independentemente fazendo uma requisição de cancelamento autenticada como coordenador do conselho ou pároco em um evento `CONFIRMADO`, verificando que o status passa a `CANCELADO` e o motivo é persistido na consulta subsequente.

**Acceptance Scenarios**:

1. **Given** um evento no status `CONFIRMADO`, **When** coordenador do conselho envia requisição de cancelamento com motivo informado, **Then** o status do evento é alterado para `CANCELADO`, o motivo é persistido e a resposta reflete o estado atualizado.
2. **Given** um evento no status `CONFIRMADO`, **When** vice-coordenador do conselho envia requisição de cancelamento com motivo informado, **Then** o cancelamento é efetivado imediatamente sem exigir aprovação.
3. **Given** um evento no status `CONFIRMADO`, **When** pároco envia requisição de cancelamento com motivo informado, **Then** o cancelamento é efetivado imediatamente e o histórico de observação registra a ação com tipo `CANCELAMENTO`.
4. **Given** um cancelamento efetivado por usuário de plenos poderes, **When** o calendário do período é consultado, **Then** o evento não aparece como ativo, mas permanece acessível no histórico com o motivo de cancelamento exibido.

---

### User Story 2 - Cancelamento com Aprovação Prévia por Usuário de Nível Intermediário (Priority: P2)

Como coordenador ou vice-coordenador de uma pastoral ou laicato, ou como vigário, quero solicitar o cancelamento de um evento uma única vez e ter essa ação efetivada automaticamente quando for aprovada por pároco ou conselho, respeitando meu escopo de organização quando aplicável, para garantir que alterações sensíveis passem pelo fluxo de governança correto.

**Why this priority**: Define o comportamento de governança para a maioria dos solicitantes não-privilegiados. Sem este fluxo, qualquer coordenador de pastoral poderia cancelar eventos unilateralmente.

**Independent Test**: Pode ser testado independentemente criando um evento, submetendo requisição de cancelamento autenticada como coordenador de pastoral, verificando que o sistema cria uma solicitação pendente, depois aprovando via fluxo existente e confirmando que o evento é cancelado automaticamente sem novo envio da ação.

**Acceptance Scenarios**:

1. **Given** um evento ativo, **When** coordenador de uma pastoral envia requisição de cancelamento com motivo informado, **Then** o sistema cria uma solicitação de aprovação pendente para aquele cancelamento, não altera ainda o evento e retorna o identificador da solicitação criada.
2. **Given** uma solicitação pendente de cancelamento criada por vigário, **When** pároco ou coordenador/vice-coordenador do conselho aprova a solicitação, **Then** o sistema efetiva automaticamente o cancelamento, persiste o motivo original e registra o identificador do aprovador.
3. **Given** uma solicitação pendente de cancelamento criada por coordenador de laicato, **When** a solicitação é aprovada, **Then** o cancelamento é efetivado sem exigir novo envio da ação pelo solicitante e o registro operacional associa a aprovação à efetivação.
4. **Given** uma solicitação pendente de cancelamento para um evento específico, **When** a solicitação é reprovada, **Then** o evento permanece inalterado e a decisão fica auditada como rejeição.
5. **Given** um evento confirmado pertencente a qualquer organização, **When** o vigário envia requisição de cancelamento com motivo informado, **Then** o sistema aceita o escopo do solicitante, cria a solicitação pendente e aguarda a aprovação sem restringi-lo à organização responsável do evento.

---

### User Story 3 - Rejeição de Cancelamento por Papel Não Autorizado (Priority: P3)

Como sistema, quero rejeitar toda tentativa de cancelamento por papéis que não possuem permissão para esta ação, para garantir que somente atores autorizados possam encerrar eventos do calendário.

**Why this priority**: Proteção de integridade e segurança do calendário. Sem esta rejeição, qualquer membro autenticado poderia cancelar eventos.

**Independent Test**: Pode ser testado independentemente enviando requisição de cancelamento autenticada como membro, secretário ou padre, confirmando resposta de permissão insuficiente sem alteração no evento.

**Acceptance Scenarios**:

1. **Given** um evento ativo, **When** membro de qualquer organização envia requisição de cancelamento, **Then** o sistema retorna `FORBIDDEN` e o evento permanece inalterado.
2. **Given** um evento ativo, **When** secretário do conselho envia requisição de cancelamento, **Then** o sistema retorna `FORBIDDEN`.
3. **Given** um evento ativo, **When** padre do clero envia requisição de cancelamento, **Then** o sistema retorna `FORBIDDEN`.
4. **Given** um evento ativo pertencente à organização A, **When** coordenador da organização B (PASTORAL) envia requisição de cancelamento, **Then** o sistema retorna `FORBIDDEN` por ausência de escopo sobre a organização responsável pelo evento.

---

### User Story 4 - Auditoria e Rastreabilidade do Cancelamento (Priority: P4)

Como equipe de operação paroquial, quero que cada cancelamento produza registro auditável permanente com identificação do autor, motivo e fluxo de aprovação quando aplicável, para garantir rastreabilidade completa de todas as ações administrativas sobre o calendário.

**Why this priority**: Requisito transversal que dá validade administrativa e institucional ao cancelamento. Sem trilha auditável, o cancelamento não tem respaldo para prestação de contas da paróquia.

**Independent Test**: Pode ser testado verificando, após cada cancelamento (direto ou aprovado), que o registro de observação do tipo `CANCELAMENTO` existe, que contém identificador do ator, motivo e — quando aplicável — identificador do aprovador.

**Acceptance Scenarios**:

1. **Given** um cancelamento efetivado por qualquer caminho (direto ou aprovado), **When** o histórico de observações do evento é consultado, **Then** existe ao menos uma entrada do tipo `CANCELAMENTO` com motivo, identificador do ator e timestamp do cancelamento.
2. **Given** um cancelamento efetivado via fluxo de aprovação, **When** o registro operacional é consultado, **Then** ele contém o identificador do aprovador além do solicitante.
3. **Given** uma tentativa de cancelamento rejeitada por permissão insuficiente ou ausência de aprovação, **When** o registro de auditoria é consultado, **Then** a tentativa está registrada com motivo de rejeição sem expor dados sensíveis.

---

### Edge Cases

- Cancelamento de evento fora do status `CONFIRMADO` deve retornar erro de transição de status inválida sem qualquer alteração.
- Requisição de cancelamento sem campo `motivo` ou com motivo vazio deve ser rejeitada com erro de validação antes de qualquer verificação de permissão.
- Motivo com tamanho superior ao limite máximo permitido deve retornar erro de validação explícito.
- Aprovação concedida para uma solicitação cujo evento deixou de estar `CONFIRMADO` antes da execução automática deve falhar de forma segura, mantendo o evento inalterado e registrando a falha de efetivação.
- Solicitação pendente de cancelamento reprovada não pode ser reaproveitada nem convertida em execução posterior.
- Cancelamento concorrente do mesmo evento deve garantir que apenas uma operação seja efetivada — sem estado parcial ou dupla entrada de auditoria.
- Usuário autenticado sem vínculo de organização registrado deve receber `FORBIDDEN`.

## API Contract & Validation *(mandatory)*

- **Endpoints afetados**:
  - `DELETE /api/v1/eventos/{eventoId}` — contrato já existente, comportamento alterado de no-op para cancelamento direto ou criação de solicitação pendente, conforme o papel do solicitante.
  - `PATCH /api/v1/aprovacoes/{id}` — contrato de aprovação passa a efetivar automaticamente a ação pendente associada quando a decisão for `APROVADA`.
- **Forma de requisição para `DELETE /api/v1/eventos/{eventoId}`**: corpo JSON com campo `motivo` (string, obrigatório, não vazio, máximo 2000 caracteres). Não recebe `aprovacaoId`.
- **Forma de resposta em sucesso para `DELETE /api/v1/eventos/{eventoId}`**:
  - `200 OK` com representação completa do evento cancelado quando o solicitante tiver poderes para efetivação imediata.
  - `202 Accepted` com identificador da solicitação e status `PENDENTE` quando o solicitante depender de aprovação prévia.
- **Forma de resposta em sucesso para `PATCH /api/v1/aprovacoes/{id}`**: resposta de aprovação contendo a decisão registrada e, quando a decisão for `APROVADA`, evidência de que a ação pendente foi executada automaticamente.
- **Códigos de erro esperados (machine-readable)**:
  - `VALIDATION_ERROR` — motivo ausente, vazio ou acima do limite de tamanho.
  - `EVENT_NOT_FOUND` — identificador de evento inexistente.
  - `APPROVAL_NOT_FOUND` — identificador de solicitação de aprovação inexistente.
  - `FORBIDDEN` — papel do solicitante não tem permissão para cancelar eventos.
  - `APPROVAL_EXECUTION_FAILED` — a solicitação foi aprovada, mas a ação não pôde ser efetivada por violação de pré-condição no momento da execução.
  - `APPROVAL_ALREADY_DECIDED` — solicitação de aprovação já decidida, sem possibilidade de novo processamento.
  - `INVALID_STATUS_TRANSITION` — evento já está em status `CANCELADO` ou em outro status que não admite transição para `CANCELADO`.
- **Estado assíncrono não-erro**:
  - `APPROVAL_PENDING` — resultado funcional do `202 Accepted`, indicando que a solicitação foi criada e aguarda decisão de aprovador.
- **Compatibilidade retroativa**: o endpoint `DELETE /api/v1/eventos/{eventoId}` já existe; clientes que não enviavam corpo passam a receber `VALIDATION_ERROR` — breaking change esperada e documentada. Consumidores devem atualizar para enviar `{ "motivo": "..." }`.
- **Nota de migração**: requisições sem corpo que atualmente retornam `204` (no-op) passarão a retornar `400 VALIDATION_ERROR`. Documentar em changelog da API.

## Calendar Integrity Rules *(mandatory for calendar/event features)*

- Somente eventos no status `CONFIRMADO` são elegíveis para cancelamento; qualquer outra transição deve ser rejeitada com `INVALID_STATUS_TRANSITION`.
- O cancelamento é uma operação de **soft delete**: nenhum dado é removido fisicamente — o status é atualizado para `CANCELADO` e `canceladoMotivo` é persistido.
- Eventos `CANCELADO` devem permanecer visíveis no histórico interno para usuários autorizados com o motivo exibido; não devem aparecer no calendário público como eventos ativos.
- Vínculos históricos do evento (projetos associados, organizações envolvidas, observações pré-existentes) devem ser preservados após o cancelamento.
- A efetivação do cancelamento deve ser transacional — status, motivo e registro de auditoria são persistidos atomicamente ou nenhum é persistido.

## Operational Observability *(mandatory)*

- Toda tentativa de cancelamento (bem-sucedida ou rejeitada) deve gerar registro operacional estruturado contendo: `correlationId`, `eventoId`, `atorId`, `papel`, `tipoOrganizacao`, `resultado` e `motivoRejeicao` quando aplicável.
- Solicitações pendentes devem registrar, adicionalmente: `canceladoMotivo` (truncado para fins de log), `solicitacaoAprovacaoId` e status `PENDENTE`.
- Cancelamentos efetivados devem registrar, adicionalmente: `canceladoMotivo` (truncado para fins de log), `solicitacaoAprovacaoId` quando aplicável e `aprovadorId` quando presente.
- Tentativas rejeitadas por permissão devem registrar papel e organização do solicitante sem expor dados do evento.
- A observação do tipo `CANCELAMENTO` deve ser gravada no histórico append-only do evento em toda efetivação bem-sucedida, com conteúdo indicando o motivo.
- Erros operacionais devem fornecer `correlationId` e categoria de erro sem vazar dados de negócio sensíveis nas mensagens ao cliente.

## Architecture and Code Standards *(mandatory)*

- A lógica de autorização de cancelamento deve residir inteiramente na camada de domínio/aplicação — o controller apenas orquestra entrada, invoca o use case e mapeia a saída.
- Deve ser criado `CancelEventoUseCase` dedicado na camada de aplicação, separado de `UpdateEventoUseCase`, por ser uma operação de ciclo de vida distinta com semântica e autorizações próprias.
- Para papéis que dependem de autorização, `CancelEventoUseCase` deve persistir uma solicitação pendente contendo snapshot suficiente da ação para posterior execução automática após aprovação.
- A política de autorização de cancelamento deve ser encapsulada em classe de política do domínio (ex.: `EventoCancelamentoAuthorizationPolicy`) para evitar lógica de permissão espalhada.
- A validação do motivo (presença e tamanho) deve ocorrer na entrada (controller/DTO) antes de chegar ao use case.
- A operação inteira deve ser executada dentro de uma única transação — status, motivo e observação de auditoria são persistidos ou revertidos atomicamente.
- A aprovação da solicitação deve acionar a execução automática do cancelamento com base no snapshot persistido, sem depender de nova chamada do cliente.
- A política de escopos de organização para cancelamento deve reutilizar ou estender `EventoPatchAuthorizationService` ou `AuthorizationPolicy`, sem duplicar regras de RBAC.
- Complexidade ciclomática máxima de 10 nas classes de domínio e aplicação afetadas.

## Dependencies and Assumptions

- O status `CANCELADO` e o campo `cancelado_motivo` já existem no modelo de dados e no schema do banco (Flyway V002).
- O fluxo de aprovação (`CreateSolicitacaoAprovacaoUseCase`, `ValidateAprovacaoUseCase`) já está implementado e deve ser estendido para armazenar o payload da ação pendente e executar automaticamente o cancelamento na aprovação.
- A aprovação de tipo `CANCELAMENTO` já existe como valor válido em `TipoSolicitacaoInputEnum` (spec 002).
- O endpoint de aprovação/reprovação de solicitações (`PATCH /api/v1/aprovacoes/{id}`) é parte integrante desta feature para o fluxo de nível intermediário, pois a aprovação deve efetivar automaticamente a ação pendente.
- A escrita de observações append-only no histórico do evento já está mapeada na entidade `ObservacaoEventoEntity` com tipo `CANCELAMENTO`.
- A autenticação e resolução de contexto do ator (`EventoActorContextResolver`) já estão operacionais.
- Usuário de PASTORAL/LAICATO somente pode cancelar eventos cuja organização responsável seja a deles, alinhado com a política vigente no PATCH (coordenador/vice da org responsável).
- Pároco, coordenador e vice-coordenador do CONSELHO, e vigário possuem escopo transversal de solicitação sobre eventos de qualquer organização; apenas o vigário continua sujeito à exigência de aprovação prévia.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST rejeitar qualquer requisição de cancelamento que não inclua motivo com conteúdo válido (não nulo, não vazio, até 2000 caracteres).
- **FR-002**: O sistema MUST persistir o status `CANCELADO` e o `canceladoMotivo` de forma atômica — ambos são gravados juntos ou nenhum é gravado.
- **FR-003**: O sistema MUST efetivar o cancelamento imediatamente, sem aprovação prévia, quando o solicitante for coordenador ou vice-coordenador do CONSELHO, ou pároco do CLERO.
- **FR-004**: O sistema MUST criar uma solicitação pendente de aprovação de tipo `CANCELAMENTO`, contendo o motivo e os metadados da ação, quando o solicitante for coordenador ou vice-coordenador de PASTORAL ou LAICATO, ou vigário do CLERO.
- **FR-005**: O sistema MUST efetivar automaticamente o cancelamento quando a solicitação pendente for aprovada por pároco, coordenador do conselho ou vice-coordenador do conselho, sem exigir ressubmissão da ação pelo solicitante.
- **FR-006**: O sistema MUST retornar `FORBIDDEN` para qualquer papel não listado nas duas categorias acima (membro, secretário, padre e demais).
- **FR-007**: O sistema MUST retornar `INVALID_STATUS_TRANSITION` ao tentar cancelar evento que não esteja no status `CONFIRMADO`.
- **FR-008**: O sistema MUST preservar todos os vínculos históricos do evento (organizações, projetos, observações pré-existentes) após o cancelamento.
- **FR-009**: O sistema MUST registrar entrada de observação do tipo `CANCELAMENTO` no histórico append-only do evento em todo cancelamento efetivado com sucesso.
- **FR-010**: O sistema MUST registrar trilha operacional auditável para toda tentativa de cancelamento (efetivada ou rejeitada), incluindo identificadores de ator, evento e resultado.
- **FR-011**: O sistema MUST incluir o identificador do aprovador na trilha operacional quando o cancelamento for efetivado via fluxo de aprovação.
- **FR-012**: O sistema MUST retornar, em caso de sucesso, o estado completo do evento atualizado refletindo o banco de dados (sem dado mockado).
- **FR-013**: O sistema MUST rejeitar a execução automática de solicitação aprovada quando a pré-condição do cancelamento não estiver mais satisfeita no momento da efetivação, registrando falha auditável.
- **FR-014**: O sistema MUST garantir que cancelamento de evento por usuário de PASTORAL ou LAICATO seja limitado ao escopo da organização responsável pelo evento.
- **FR-015**: O sistema MUST permitir que pároco, coordenador ou vice-coordenador do CONSELHO, e vigário solicitem cancelamento de eventos de qualquer organização, respeitando as regras de aprovação aplicáveis a cada papel.
- **FR-016**: O sistema MUST registrar a reprovação da solicitação de cancelamento sem alterar o evento e sem permitir que a mesma solicitação seja reutilizada para execução futura.

### Key Entities *(include if feature involves data)*

- **Evento**: compromisso do calendário paroquial; adquire status `CANCELADO` e campo `canceladoMotivo` nesta operação. Status final é irreversível (sem saída de `CANCELADO`).
- **SolicitacaoAprovacao**: registro de autorização prévia; para `CANCELAMENTO`, deve armazenar o snapshot da ação pendente, incluindo evento alvo e motivo, para execução automática após aprovação.
- **ObservacaoEvento**: entrada append-only no histórico do evento; tipo `CANCELAMENTO` gravada a cada efetivação bem-sucedida para rastreabilidade.
- **RegistroOperacional de Cancelamento**: trilha de auditoria estruturada produzida por toda tentativa (sucesso ou falha), correlacionando ator, evento, resultado e aprovação quando presente.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos cancelamentos efetivados persistem `status = CANCELADO` e `canceladoMotivo` recuperáveis numa consulta subsequente ao evento.
- **SC-002**: 100% das tentativas de cancelamento por papéis não autorizados são rejeitadas sem qualquer alteração no evento.
- **SC-003**: 100% dos cancelamentos efetivados possuem entrada de observação do tipo `CANCELAMENTO` no histórico do evento.
- **SC-004**: 100% dos cancelamentos efetivados via fluxo de aprovação registram identificador do aprovador na trilha operacional.
- **SC-005**: 100% das tentativas de cancelamento (efetivadas e rejeitadas) produzem registro operacional auditável pesquisável por `eventoId` ou `atorId`.
- **SC-006**: Cancelamento direto (Tier 1) é completado em até 2 segundos sob carga operacional normal.
- **SC-007**: Eventos `CANCELADO` não aparecem no calendário público como ativos; o motivo é exibido no histórico interno para usuários autorizados.

## Measurement and Evidence Plan *(mandatory)*

- **SC-001 a SC-004**: evidenciados por testes de integração que verificam persistência após cancelamento e consultam o evento pelo identificador; obrigatórios no pull request.
- **SC-002**: evidenciado por testes com cada papel proibido (membro, secretário, padre, coordenador fora da org responsável) confirmando `FORBIDDEN` sem alteração de dados.
- **SC-005**: evidenciado por testes que inspecionam o registro de auditoria após cada cenário de cancelamento e rejeição.
- **SC-006**: evidenciado por teste de carga simples ou medição de tempo médio em ambiente de homologação, documentado no PR.
- **SC-007**: evidenciado por teste de contrato/visibilidade verificando que evento `CANCELADO` não retorna em listagem pública e retorna com motivo na consulta interna autorizada.
