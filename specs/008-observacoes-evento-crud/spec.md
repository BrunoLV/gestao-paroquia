# Feature Specification: Observacoes de Evento com Controle de Tipo e Autoria

**Feature Branch**: `008-observacoes-evento-crud`  
**Created**: 2026-04-13  
**Status**: Draft  
**Input**: User description: "implementação de adição, edição, exclusao e busca de observações de um evento. Será permitido adicionar via API apenas observações do tipo NOTA, pois os demais são referentes a ações do sistema. Usuários só podem editar e excluir observações do tipo NOTA que os mesmos tenham adicionado. Deve haver serviços separados para adição de notas e adição dos demais tipos que são reservados a ações sobre os eventos e não apenas observações."

## Clarifications

### Session 2026-04-13

- Q: Qual estrategia de exclusao deve ser adotada para `NOTA`? -> A: Soft delete apenas para `NOTA`, preservando auditoria e historico.
- Q: Como `NOTA` removida logicamente deve aparecer nas listagens? -> A: Deve ser ocultada das listagens normais e permanecer disponivel apenas para auditoria interna/consultas especificas.
- Q: Quem deve ser registrado como criador das observacoes sistêmicas? -> A: O usuario responsavel pela acao quando houver ator humano identificado; fallback para usuario tecnico do sistema quando a execucao for automatica sem ator humano.
- Q: Como a edicao de `NOTA` deve preservar rastreabilidade? -> A: A edicao atualiza a `NOTA`, mas preserva historico de revisoes e auditoria da alteracao.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registrar e consultar notas de evento (Priority: P1)

Como usuario autenticado com permissao de colaborar no evento, quero adicionar e listar minhas notas para manter o historico de contexto operacional.

**Why this priority**: Sem cadastro e consulta de notas, a trilha de comunicacao do evento fica incompleta e perde valor para operacao diaria.

**Independent Test**: Pode ser testada ao criar uma nota `NOTA` para um evento e em seguida consultar tanto a lista das minhas observacoes quanto a lista completa do evento para verificar retorno consistente do item criado.

**Acceptance Scenarios**:

1. **Given** um evento existente e usuario autenticado com permissao, **When** envia requisicao de criacao com tipo `NOTA` e conteudo valido, **Then** a API cria a observacao e retorna identificador, autoria e timestamp.
2. **Given** um evento existente, **When** o usuario lista apenas as proprias observacoes do evento, **Then** a API retorna somente os registros cujo usuario criador corresponde ao solicitante, com ordenacao deterministica.
3. **Given** um evento existente e usuario com permissao de leitura ampla no evento, **When** solicita a listagem completa das observacoes, **Then** a API retorna o historico ordenado de forma deterministica contendo ao menos os campos de id, tipo, conteudo, usuario criador e data/hora de criacao.
4. **Given** um usuario autenticado, **When** tenta criar observacao manual com tipo diferente de `NOTA`, **Then** a API rejeita com erro de regra de negocio e nao persiste o registro.

---

### User Story 2 - Editar e excluir somente notas proprias (Priority: P2)

Como usuario autenticado, quero editar e excluir apenas minhas notas para corrigir informacoes sem alterar registros de outros autores ou observacoes sistêmicas.

**Why this priority**: Garante responsabilizacao por autoria e evita adulteracao de trilhas que representam decisoes de sistema.

**Independent Test**: Pode ser testada criando duas notas de autores diferentes e validando que cada usuario consegue alterar/excluir apenas a propria nota `NOTA`.

**Acceptance Scenarios**:

1. **Given** uma observacao `NOTA` criada pelo proprio usuario, **When** envia requisicao de edicao com conteudo valido, **Then** a API atualiza a observacao, retorna o novo conteudo e preserva historico de revisoes da alteracao.
2. **Given** uma observacao `NOTA` criada pelo proprio usuario, **When** solicita exclusao, **Then** a API executa soft delete, preserva a rastreabilidade do registro removido e oculta a nota das listagens normais.
3. **Given** uma observacao `NOTA` criada por outro usuario, **When** um usuario sem autoria tenta editar ou excluir, **Then** a API rejeita a operacao por violacao de permissao.
4. **Given** uma observacao de tipo sistêmico (`JUSTIFICATIVA`, `APROVACAO`, `REPROVACAO`, `CANCELAMENTO`, `AJUSTE_HORARIO`), **When** qualquer usuario tenta editar ou excluir por endpoint manual, **Then** a API rejeita a operacao por regra de imutabilidade do tipo.

---

### User Story 3 - Registrar observacoes sistêmicas por fluxo de acao (Priority: P3)

Como sistema de calendario, quero registrar observacoes de tipos sistêmicos por servicos dedicados de acao para manter consistencia entre evento e trilha de auditoria.

**Why this priority**: Se tipos sistêmicos puderem ser criados manualmente, o historico perde confiabilidade e pode divergir da execucao real das acoes.

**Independent Test**: Pode ser testada executando uma acao de evento que gera observacao sistêmica e verificando criacao por servico reservado, sem expor esse caminho para criacao manual.

**Acceptance Scenarios**:

1. **Given** uma acao de cancelamento efetivada com justificativa informada, **When** o fluxo sistêmico conclui a acao, **Then** uma observacao `CANCELAMENTO` e registrada automaticamente pelo servico reservado usando o texto da justificativa do cancelamento.
2. **Given** um endpoint publico/manual de criacao de observacao, **When** recebe tipo sistêmico, **Then** nao delega para servico sistêmico e retorna erro de validacao de regra.
3. **Given** uma acao sistêmica de edicao sensivel com justificativa informada, **When** o fluxo correspondente conclui a acao, **Then** uma observacao do tipo sistêmico aplicavel e registrada automaticamente usando o texto justificativo da propria acao.
4. **Given** servicos separados para nota manual e observacao sistêmica, **When** uma requisicao percorre cada fluxo, **Then** cada servico aplica exclusivamente seu conjunto permitido de tipos e o conteudo textual coerente com a acao executada.

### Edge Cases

- Tentativa de criar nota com conteudo em branco, apenas espacos ou acima do limite de tamanho.
- Tentativa de editar ou excluir observacao inexistente.
- Tentativa de editar/excluir observacao que nao e `NOTA` mesmo sendo do proprio autor.
- Tentativa de editar `NOTA` sem registrar trilha de revisao da alteracao.
- Tentativa de excluir uma `NOTA` ja removida logicamente.
- Tentativa de recuperar `NOTA` removida por endpoint/listagem normal sem escopo explicito de auditoria.
- Concorrencia de edicao: duas alteracoes quase simultaneas sobre a mesma `NOTA` devem resultar em comportamento deterministico (aceitar uma e rejeitar/conciliar a outra segundo politica de concorrencia adotada).
- Tentativa de listar todas as observacoes do evento sem permissao de leitura interna.
- Tentativa de acessar a listagem "minhas observacoes" sem identidade autenticada resolvida no contexto da requisicao.

## API Contract & Validation *(mandatory)*

- `POST /api/v1/eventos/{eventoId}/observacoes` (changed): passa a aceitar apenas tipo `NOTA` para criacao manual.
- `GET /api/v1/eventos/{eventoId}/observacoes` (changed): deve retornar todas as observacoes do evento com ordenacao deterministica e metadados de usuario criador e data/hora de criacao para usuarios com permissao de leitura correspondente.
- `GET /api/v1/eventos/{eventoId}/observacoes/minhas` (new) ou filtro funcional equivalente no endpoint de listagem (changed): deve retornar apenas observacoes cujo usuario criador seja o solicitante autenticado.
- No fluxo manual, `usuarioId` do criador deve ser resolvido exclusivamente do contexto autenticado e nao aceito no payload de requisicao.
- Listagens normais (`todas` e `minhas`) devem excluir `NOTA` removida logicamente; recuperacao desses registros fica restrita a trilhas de auditoria/consultas internas especificas.
- `PATCH /api/v1/eventos/{eventoId}/observacoes/{observacaoId}` (new): edita somente `NOTA` de autoria do solicitante, preservando historico de revisoes e auditoria.
- `DELETE /api/v1/eventos/{eventoId}/observacoes/{observacaoId}` (new): executa soft delete somente para `NOTA` de autoria do solicitante.
- Endpoints sistêmicos de evento (unchanged contract externally): continuam registrando observacoes de tipos reservados por servicos internos dedicados.
- Campos obrigatorios de resposta para cada observacao: `id`, `eventoId`, `usuarioId` (criador), `tipo`, `conteudo`, `criadoEmUtc`.
- Regras de geracao sistêmica:
  - `CANCELAMENTO`: criada automaticamente no fluxo de cancelamento com o texto da justificativa de cancelamento e autoria atribuida ao ator humano responsavel pela acao, quando houver, ou ao usuario tecnico do sistema em execucao automatica sem ator humano.
  - `JUSTIFICATIVA`: criada automaticamente no fluxo de acao que exigir fundamentacao textual, reutilizando o texto da justificativa informada naquela acao e a autoria do ator responsavel ou do usuario tecnico do sistema, conforme o contexto.
  - `APROVACAO`: criada automaticamente no fluxo de aprovacao com o texto da decisao ou observacao associada a aprovacao e autoria do aprovador humano ou do usuario tecnico do sistema em execucao sem ator humano.
  - `REPROVACAO`: criada automaticamente no fluxo de reprovacao com o texto da decisao ou motivo da reprovacao e autoria do reprovador humano ou do usuario tecnico do sistema em execucao sem ator humano.
  - `AJUSTE_HORARIO`: criada automaticamente no fluxo de alteracao de horario com o texto justificativo da mudanca e autoria do ator responsavel pela alteracao ou do usuario tecnico do sistema em execucao automatica.
- Regras de validacao:
  - `tipo` em criacao manual deve ser exatamente `NOTA`.
  - Payload manual de criacao nao deve aceitar `usuarioId` informado pelo cliente.
  - `conteudo` obrigatorio, nao vazio e dentro do limite configurado.
  - `eventoId` e `observacaoId` devem ser UUID validos.
  - O modo "minhas observacoes" deve filtrar estritamente por `usuarioId` do solicitante autenticado.
  - O modo "todas as observacoes do evento" deve exigir permissao de leitura correspondente ao escopo do evento.
  - Edicao/exclusao exigem autoria igual ao usuario autenticado.
  - Edicao de `NOTA` deve preservar historico de revisoes e auditoria da alteracao.
  - Exclusao de `NOTA` deve ser logica, sem remocao fisica do registro.
  - `NOTA` removida logicamente nao deve aparecer nas listagens normais.
  - Edicao/exclusao proibidas para tipos reservados.
- Codigos de erro de negocio esperados:
  - `OBSERVACAO_TIPO_MANUAL_INVALIDO`
  - `OBSERVACAO_NAO_ENCONTRADA`
  - `OBSERVACAO_AUTOR_INVALIDO`
  - `OBSERVACAO_TIPO_IMUTAVEL`
  - `ACCESS_DENIED`
  - `VALIDATION_REQUIRED_FIELD`
- Compatibilidade:
  - Consumidores que hoje enviam tipos diferentes de `NOTA` no endpoint manual passam a receber erro deterministico de negocio.
  - Fluxos sistêmicos de tipos reservados permanecem disponiveis por acoes de evento, sem novo payload publico para esses tipos.

## Calendar Integrity Rules *(mandatory for calendar/event features)*

- Timezone canonico permanece UTC para persistencia de timestamps de observacao.
- Leitura de observacoes segue mesma estrategia de apresentacao temporal adotada pela API para consumidores internos.
- Ordenacao da listagem de observacoes deve ser deterministica por `criadoEmUtc` ascendente e criterio secundario estavel por `id`.
- O servico de listagem deve suportar dois modos funcionais: "somente minhas observacoes" e "todas as observacoes do evento", ambos com a mesma semantica de ordenacao.
- Edicao de `NOTA` deve manter integridade historica por meio de trilha de revisoes auditavel.
- Exclusao de `NOTA` deve preservar integridade historica da trilha de observacoes por meio de soft delete.
- Registros removidos logicamente devem permanecer fora da projecao funcional padrão e acessiveis apenas por mecanismos internos de auditoria.
- Observacoes sistêmicas devem manter coerencia com o estado do evento e com o texto de negocio que as originou (ex.: `CANCELAMENTO` somente quando acao correspondente ocorrer e com conteudo igual a justificativa informada no fluxo).

## Operational Observability *(mandatory)*

- Criacao, edicao, exclusao e listagem devem registrar ator, acao, evento alvo, observacao alvo (quando aplicavel), resultado e correlation id.
- Edicao de `NOTA` deve registrar metadados suficientes para reconstruir o conteudo anterior e o novo conteudo em trilha de auditoria apropriada.
- Operacoes de soft delete devem registrar explicitamente o marcador de remocao logica e manter evidencias para auditoria posterior.
- Consultas internas de auditoria devem permitir rastrear registros removidos logicamente sem reintroduzi-los nas listagens funcionais.
- Toda rejeicao de regra (tipo invalido, autor diferente, tipo imutavel) deve gerar evento auditavel com codigo de erro.
- Criacao de observacoes sistêmicas deve registrar origem da acao (fluxo de aprovacao, cancelamento ou ajuste) e tipo gerado.
- Criacao de observacoes sistêmicas deve registrar tambem a correspondencia entre o conteudo textual persistido e a justificativa ou decisao recebida no fluxo de origem.
- Criacao de observacoes sistêmicas deve registrar explicitamente se a autoria veio de ator humano identificado ou de usuario tecnico do sistema.
- Logs de erro devem conter contexto de diagnostico sem expor conteudo sensivel alem do necessario para auditoria.

## Architecture and Code Standards *(mandatory)*

- Separar casos de uso/servicos de aplicacao para:
  - criacao manual de `NOTA`;
  - criacao de observacoes sistêmicas (tipos reservados);
  - edicao/exclusao de `NOTA` por autoria.
- Manter fronteiras de camadas:
  - `domain`: regras de tipo permitido e autoria;
  - `application`: orquestracao dos fluxos;
  - `api`: validacao de contrato e mapeamento HTTP;
  - `infrastructure`: persistencia e auditoria.
- Fluxos sistêmicos nao devem depender de endpoint manual para registrar observacao reservada.
- Excecoes de negocio devem ser mapeadas para respostas deterministicas e consistentes.
- Componentes alterados devem manter baixa complexidade, nomes explicitos e testes de contrato/integracao cobrindo cenarios positivos e negativos.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST permitir criacao manual de observacao apenas com tipo `NOTA`.
- **FR-002**: O sistema MUST rejeitar criacao manual com qualquer tipo reservado (`JUSTIFICATIVA`, `APROVACAO`, `REPROVACAO`, `CANCELAMENTO`, `AJUSTE_HORARIO`).
- **FR-003**: O sistema MUST disponibilizar listagem de observacoes por evento com resultado deterministico e, para cada item, incluir usuario criador e data/hora de criacao.
- **FR-004**: O sistema MUST disponibilizar um modo de listagem restrito a "minhas observacoes" para retornar apenas observacoes do usuario autenticado naquele evento.
- **FR-005**: O sistema MUST disponibilizar um modo de listagem de "todas as observacoes do evento" para usuarios com permissao de leitura correspondente.
- **FR-006**: O sistema MUST permitir editar somente observacao do tipo `NOTA` criada pelo proprio usuario autenticado, preservando historico de revisoes e auditoria da alteracao.
- **FR-007**: O sistema MUST permitir excluir somente observacao do tipo `NOTA` criada pelo proprio usuario autenticado por meio de soft delete.
- **FR-008**: O sistema MUST rejeitar edicao/exclusao de observacoes de tipos reservados, independentemente da autoria.
- **FR-009**: O sistema MUST separar o servico de criacao de nota manual do servico de criacao de observacoes sistêmicas.
- **FR-010**: O sistema MUST registrar observacoes de tipos reservados exclusivamente por fluxos sistêmicos de acao de evento.
- **FR-011**: O sistema MUST criar automaticamente observacao `CANCELAMENTO` com conteudo igual a justificativa informada no fluxo de cancelamento.
- **FR-012**: O sistema MUST criar automaticamente observacao do tipo sistêmico aplicavel com conteudo textual derivado da justificativa, motivo ou decisao registrada no fluxo de origem.
- **FR-013**: O sistema MUST validar conteudo obrigatorio e limite de tamanho para criacao e edicao de `NOTA`.
- **FR-014**: O sistema MUST emitir codigos de erro de negocio deterministicos para violacoes de tipo permitido, autoria e imutabilidade de tipo.
- **FR-015**: O sistema MUST manter rastreabilidade operacional para toda operacao de observacao (sucesso e falha).
- **FR-016**: O sistema MUST persistir e expor, para toda observacao, o identificador do usuario criador e a data/hora de criacao.
- **FR-017**: O sistema MUST preservar auditoria e historico de `NOTA` excluida logicamente, sem remocao fisica do registro.
- **FR-018**: O sistema MUST ocultar `NOTA` excluida logicamente das listagens funcionais de "minhas observacoes" e "todas as observacoes do evento", mantendo acesso apenas por trilhas internas de auditoria.
- **FR-019**: O sistema MUST atribuir observacao sistêmica ao usuario humano responsavel pela acao quando ele existir no fluxo, usando usuario tecnico do sistema apenas como fallback para execucoes automáticas sem ator humano.
- **FR-020**: O sistema MUST manter trilha auditavel de revisoes para cada edicao de `NOTA`, preservando conteudo anterior, conteudo atualizado, autor da alteracao e timestamp da revisao.

### Key Entities *(include if feature involves data)*

- **ObservacaoEvento**: Registro de historico vinculado a evento com atributos de id, evento, usuario criador, tipo, conteudo e data/hora de criacao.
- **EstadoRemocaoObservacao**: Regra de exclusao logica aplicavel apenas a `NOTA`, preservando o registro para auditoria.
- **PoliticaTipoObservacao**: Regra de classificacao entre tipo manual permitido (`NOTA`) e tipos reservados de sistema.
- **PoliticaGeracaoObservacaoSistemica**: Regra que associa cada fluxo sistêmico ao tipo de observacao gerado automaticamente e ao texto de negocio que deve ser persistido como conteudo.
- **PoliticaAutoriaObservacaoSistemica**: Regra que define autoria humana preferencial e fallback para usuario tecnico do sistema em fluxos automáticos sem ator humano.
- **PoliticaAutoriaObservacao**: Regra que determina se o usuario autenticado pode editar/excluir uma observacao conforme autoria e tipo.
- **HistoricoRevisaoNota**: Trilha de revisoes que registra cada alteracao de conteudo em `NOTA`, com autor da revisao e timestamp.

### Assumptions and Dependencies

- Identidade do usuario autenticado e disponibilizada no contexto de seguranca da requisicao.
- Fluxos de evento que ja geram observacoes sistêmicas (como cancelamento) continuam ativos e passam a delegar para servico sistêmico dedicado quando necessario.
- Estrutura de persistencia de observacoes permanece no agregado de evento existente, sem mudar o conceito append-only para registros sistêmicos.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das tentativas de criacao manual com tipo diferente de `NOTA` sao rejeitadas com codigo de erro deterministico.
- **SC-002**: 100% das tentativas de edicao/exclusao de observacao por usuario nao autor sao rejeitadas com codigo de permissao esperado.
- **SC-003**: 100% das tentativas de edicao/exclusao de tipos reservados sao rejeitadas independentemente da autoria.
- **SC-004**: Em suite de testes de contrato e integracao, 100% dos cenarios criticos de observacao (criar/listar/editar/excluir/restricoes) passam sem regressao.
- **SC-005**: Percentil p95 de resposta para criar, editar e listar `NOTA` permanece <= 2000ms por operacao em ambiente Tier 1, sem degradacao perceptivel para usuario interno.
- **SC-006**: 100% das respostas de criacao e listagem de observacoes incluem usuario criador e data/hora de criacao com formato consistente.
- **SC-007**: 100% dos fluxos sistêmicos cobertos em teste persistem observacao do tipo esperado com conteudo textual igual ao motivo, justificativa ou decisao fornecida na acao de origem.
- **SC-008**: 100% dos testes de listagem validam corretamente a separacao entre o modo "minhas observacoes" e o modo "todas as observacoes do evento".
- **SC-009**: 100% das exclusoes de `NOTA` cobertas em teste preservam o registro para auditoria sem remocao fisica indevida.
- **SC-010**: 100% dos testes de listagem comprovam que `NOTA` removida logicamente nao aparece nas consultas funcionais e permanece disponivel apenas para auditoria interna.
- **SC-011**: 100% dos fluxos sistêmicos cobertos em teste atribuem `usuarioId` ao ator humano da acao quando presente e usam o usuario tecnico apenas nos cenarios sem ator humano.
- **SC-012**: 100% das edicoes de `NOTA` cobertas em teste preservam historico de revisao auditavel sem perda do conteudo anterior.

## Measurement and Evidence Plan *(mandatory)*

- **SC-001, SC-002, SC-003**: evidenciar por testes de contrato com asserts de codigo de erro e ausencia de persistencia indevida.
- **SC-004**: evidenciar por execucao de testes de integracao cobrindo fluxos de autoria, tipo manual e tipo sistêmico.
- **SC-005**: evidenciar por `ObservacaoTier1PerformanceTest` validando p95 <= 2000ms por operacao (create/edit/list) e por comparacao de baseline operacional semanal anterior vs posterior dos endpoints de observacao.
- **SC-006**: evidenciar por testes de contrato validando presenca e formato de `usuarioId` e `criadoEmUtc` nos payloads de criacao/listagem.
- **SC-007**: evidenciar por testes de integracao validando correspondencia exata entre texto de justificativa/decisao no fluxo de origem e conteudo da observacao sistêmica persistida.
- **SC-008**: evidenciar por testes de contrato/integracao cobrindo os dois modos de listagem e a filtragem correta por usuario autenticado.
- **SC-009**: evidenciar por testes de integracao/repositorio validando que a exclusao de `NOTA` altera o estado logico sem remover a linha persistida.
- **SC-010**: evidenciar por testes de integracao/listagem e auditoria validando ocultacao nas consultas funcionais e preservacao do acesso interno ao registro removido.
- **SC-011**: evidenciar por testes de integracao dos fluxos sistêmicos cobrindo tanto autoria humana quanto fallback tecnico.
- **SC-012**: evidenciar por testes de integracao/repositorio validando persistencia do historico de revisoes em cada edicao de `NOTA`.
- Pull requests desta feature devem anexar evidencias de:
  - matriz de autorizacao de edicao/exclusao por autoria;
  - prova de separacao de servicos manual vs sistêmico;
  - validacao de compatibilidade para clientes que enviam tipo invalido no endpoint manual.
