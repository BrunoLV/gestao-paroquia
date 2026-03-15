# Feature Specification: Criacao Completa de Evento

**Feature Branch**: `003-complete-event-creation`  
**Created**: 2026-03-15  
**Status**: Draft  
**Input**: User description: "Implementar o fluxo de criacao de evento sem mock, com integracao com base de dados e execucao de regras. O servico deve receber o Evento completo em uma unica operacao, mesmo que operacoes separadas existam."

## Clarifications

### Session 2026-03-15

- Q: Como tratar criacao quando houver sobreposicao de horario/recurso? -> A: Permitir criacao e marcar `CONFLICT_PENDING` para resolucao posterior.
- Q: Quem pode criar evento completo? -> A: Admin Geral e Admin de Pastoral (somente no proprio escopo) podem criar.
- Q: Como tratar campos desconhecidos no payload de criacao? -> A: Rejeitar qualquer campo desconhecido com erro de validacao.
- Q: Como deve funcionar a visibilidade na listagem de eventos? -> A: `GET /api/v1/eventos` sempre exige autenticacao.
- Q: Como garantir idempotencia na criacao de eventos? -> A: Exigir `Idempotency-Key` no `POST /eventos` e reutilizar a resposta da primeira criacao equivalente.
- Q: `GET /api/v1/eventos` deve permanecer autenticado mesmo com gate de visibilidade publica por status? -> A: Sim. `GET /api/v1/eventos` sempre deve ser autenticado.

## User Scenarios & Testing *(mandatory)*


### User Story 1 - Cadastrar Evento Completo (Priority: P1)

Como secretaria paroquial, quero cadastrar um evento completo em uma unica requisicao para reduzir retrabalho e evitar inconsistencias entre etapas separadas.

**Why this priority**: O cadastro de evento e o fluxo principal do calendario. Sem isso, o sistema permanece dependente de mock e nao entrega valor operacional real.

**Independent Test**: Pode ser testada independentemente enviando uma requisicao de criacao com todos os dados obrigatorios e opcionais esperados do evento e validando persistencia, retorno e auditoria.

**Acceptance Scenarios**:

1. **Given** um usuario autorizado para criacao, **When** envia um evento completo valido em uma unica operacao, **Then** o sistema persiste o evento e retorna o registro criado com identificador unico.
2. **Given** um payload sem campos obrigatorios do evento completo, **When** a criacao e solicitada, **Then** o sistema rejeita a requisicao com erros de validacao por campo e sem gravar dados.
3. **Given** um evento criado com sucesso, **When** o usuario consulta a lista de eventos, **Then** o evento aparece na consulta sem depender de dados mockados.

---

### User Story 2 - Aplicar Regras de Dominio na Criacao (Priority: P2)

Como coordenador pastoral, quero que as regras de negocio sejam executadas no momento da criacao para garantir consistencia de agenda e ciclo de vida do evento desde o primeiro registro.

**Why this priority**: Persistir sem regras gera dados invalidos e custo operacional posterior para correcao.

**Independent Test**: Pode ser testada enviando combinacoes validas e invalidas de datas, status, conflitos e justificativas, verificando aceitacao/rejeicao deterministica.

**Acceptance Scenarios**:

1. **Given** um evento com horario final anterior ao inicial, **When** a criacao e solicitada, **Then** o sistema rejeita a requisicao com codigo de validacao temporal.
2. **Given** um evento marcado como ADICIONADO_EXTRA sem justificativa, **When** a criacao e solicitada, **Then** o sistema rejeita a requisicao com erro de regra de negocio.
3. **Given** um evento que sobrepoe outro no mesmo recurso e periodo, **When** a criacao e solicitada, **Then** o sistema aplica a regra definida para conflito e retorna resultado auditable.

---

### User Story 3 - Rastrear Operacao de Criacao (Priority: P3)

Como administrador paroquial, quero rastrear todas as tentativas de criacao para ter auditoria confiavel sobre sucesso e falha do fluxo.

**Why this priority**: Rastreabilidade reduz risco operacional e facilita diagnostico de incidente.

**Independent Test**: Pode ser testada executando criacoes bem-sucedidas e malsucedidas e verificando registro de trilha operacional com correlacao e resultado.

**Acceptance Scenarios**:

1. **Given** uma criacao bem-sucedida, **When** a operacao termina, **Then** a trilha de auditoria registra ator, acao, alvo, momento e resultado.
2. **Given** uma criacao rejeitada por validacao, **When** a operacao termina, **Then** a trilha de auditoria registra falha com contexto seguro para diagnostico.

---

### Edge Cases


- Requisicao com identificador externo repetido para o mesmo evento.
- Requisicao com horario no limite da virada de dia e timezone canonico da paroquia.
- Requisicao com lista vazia de participantes quando o evento permite colaboracao opcional.
- Requisicao com campos desconhecidos que nao fazem parte do contrato de criacao.
- Tentativa de criacao por usuario sem permissao de escrita.
- Tentativa de criar evento em status publico sem cumprir pre-condicoes de confirmacao.

## API Contract & Validation *(mandatory)*

- `POST /api/v1/eventos`: changed. Deve receber o evento completo em um unico payload (sem dependencia de operacoes complementares para concluir o cadastro).
- Autorizacao de criacao: apenas perfis Admin Geral e Admin de Pastoral, com Admin de Pastoral restrito ao proprio escopo organizacional.
- Idempotencia de criacao: `POST /api/v1/eventos` deve exigir cabecalho `Idempotency-Key`; repeticoes equivalentes com a mesma chave devem retornar a resposta da primeira criacao sem duplicar registro.
- `GET /api/v1/eventos`: changed. Deve retornar dados persistidos em base, removendo comportamento mockado, e exigir autenticacao em todas as chamadas.
- `PATCH /api/v1/eventos/{eventoId}`: unchanged neste escopo funcional, exceto necessidade de manter compatibilidade com evento criado de forma completa.
- `DELETE /api/v1/eventos/{eventoId}`: unchanged neste escopo funcional.
- Regras de contrato para criacao:
- Campos obrigatorios: titulo, inicio, fim, status inicial, organizacao responsavel.
- Campos opcionais: descricao, participantes.
- Campos desconhecidos nao documentados no contrato devem ser rejeitados com erro de validacao, sem tolerancia silenciosa.
- Campo de status deve aceitar somente valores permitidos no ciclo de vida.
- Campo de data/hora deve obedecer normalizacao canonica definida pelo calendario.
- Quando houver sobreposicao de horario/recurso na criacao, a API deve permitir persistencia com marcacao `CONFLICT_PENDING` para resolucao posterior, sem bloquear a operacao.
- Erros devem ser machine-readable e deterministas por categoria: `VALIDATION_ERROR`, `BUSINESS_RULE_VIOLATION`, `FORBIDDEN`, `CONFLICT`, `NOT_FOUND`.
- Compatibilidade:
- Consumidores que hoje enviam payload parcial para criacao devem migrar para payload completo.
- O contrato antigo de composicao por operacoes separadas permanece opcional por no maximo 2 releases minor ou 90 dias apos a liberacao da feature (o que ocorrer primeiro).
- O encerramento da janela de transicao exige: publicacao de compatibility notice, ausencia de trafego no contrato antigo por 2 semanas consecutivas e aprovacao do owner da API.
- `GET /api/v1/eventos` permanece autenticado em todos os cenarios desta feature.
- A regressao de visibilidade publica por status deve permanecer coberta em testes dedicados para contratos/rotas publicas do dominio (`RASCUNHO` nao publico, `CONFIRMADO` publico, `CANCELADO` com historico), sem relaxar a autenticacao deste endpoint.

## Calendar Integrity Rules *(mandatory for calendar/event features)*

- Estrategia canonica: armazenar instantes em UTC e apresentar no timezone oficial do calendario paroquial nas consultas.
- Normalizacao temporal: toda entrada de data/hora deve ser convertida para o formato canonico antes da validacao de regras.
- Regra de conflito: sobreposicao no mesmo recurso e faixa de tempo nao bloqueia criacao; o evento deve ser persistido com estado `CONFLICT_PENDING` para resolucao posterior, com resposta deterministica.
- Regra anti-duplicidade: criacoes idempotentes por mesma referencia externa e mesma janela temporal devem evitar registros duplicados.
- Ordenacao de consulta: resultados devem ser retornados por inicio ascendente e identificador como criterio de desempate para determinismo.

## Operational Observability *(mandatory)*

- O fluxo de criacao deve registrar logs estruturados com identificador de correlacao, ator, operacao, alvo e resultado.
- Operacoes de sucesso e falha de criacao MUST ser auditaveis, incluindo motivo de falha de negocio/validacao.
- O diagnostico de erro MUST expor detalhes suficientes para correcao do cliente sem revelar dados sensiveis internos.
- A medicao operacional deve separar falha por validacao de falha por regra de negocio e falha inesperada.

## Architecture and Code Standards *(mandatory)*

- O dominio define regras de consistencia da criacao de evento completo e invariantes de ciclo de vida.
- A camada de aplicacao orquestra o caso de uso de criacao com validacao de autorizacao e persistencia atomica.
- A camada de infraestrutura implementa adaptadores de entrada/saida, mapeamento de contrato e acesso a base de dados.
- Portas e adaptadores devem impedir vazamento de detalhes de transporte para regras de dominio.
- Componentes alterados devem manter complexidade controlada, nomenclatura explicita e mapeamento deterministico de erros.
- Devem ser respeitadas praticas do projeto para injecao de dependencias, validacao de entrada, fronteiras transacionais e tratamento de excecoes.

## Requirements *(mandatory)*


### Functional Requirements

- **FR-001**: O sistema MUST permitir a criacao de evento completo em unica operacao de escrita.
- **FR-002**: O sistema MUST persistir o evento criado em base de dados transacional do calendario.
- **FR-003**: O sistema MUST substituir qualquer resposta mockada de listagem por dados reais persistidos.
- **FR-003a**: O sistema MUST exigir autenticacao para `GET /api/v1/eventos` em qualquer status de evento.
- **FR-004**: O sistema MUST validar obrigatoriedade e formato de todos os campos do payload de criacao.
- **FR-005**: O sistema MUST validar consistencia temporal (inicio antes de fim e janela valida no timezone canonico).
- **FR-006**: O sistema MUST aplicar regras de ciclo de vida de status durante a criacao.
- **FR-007**: O sistema MUST rejeitar criacao em status ADICIONADO_EXTRA sem justificativa valida.
- **FR-008**: O sistema MUST aplicar regras de conflito de agenda no momento da criacao e, quando houver sobreposicao, persistir o evento com estado `CONFLICT_PENDING` para resolucao posterior, retornando resultado deterministico.
- **FR-009**: O sistema MUST exigir `Idempotency-Key` no `POST /api/v1/eventos` e garantir que repeticoes equivalentes retornem a resposta original sem duplicar registros.
- **FR-010**: O sistema MUST permitir criacao apenas para Admin Geral e Admin de Pastoral, restringindo Admin de Pastoral ao proprio escopo organizacional.
- **FR-011**: O sistema MUST retornar erros machine-readable por campo e por categoria de negocio.
- **FR-012**: O sistema MUST registrar trilha de auditoria para toda tentativa de criacao, com sucesso ou falha.
- **FR-013**: O sistema MUST manter compatibilidade de leitura para eventos existentes criados por fluxos anteriores.
- **FR-014**: O sistema MUST permitir que campos que antes eram preenchidos em operacoes separadas sejam recebidos no mesmo payload completo.
- **FR-015**: O sistema MUST preservar integridade entre organizacao responsavel e participantes informados no cadastro.
- **FR-016**: O sistema MUST rejeitar campos desconhecidos no payload de criacao com erro de validacao deterministico, sem ignora-los silenciosamente.

### Key Entities *(include if feature involves data)*

- **Evento Completo**: Unidade principal do calendario contendo identidade, dados basicos, janela temporal, status e relacoes de contexto necessarias para existir sem operacoes complementares obrigatorias.
- **Regra de Criacao de Evento**: Conjunto de validacoes de obrigatoriedade, consistencia temporal, status e conflito aplicadas no momento da criacao.
- **Registro de Auditoria de Operacao**: Evidencia operacional da tentativa de criacao com ator, acao, alvo, correlacao, momento e resultado.
- **Conflito de Agenda**: Resultado de sobreposicao ou choque de agenda detectado durante criacao, com classificacao e tratamento definido pela politica de calendario.

## Assumptions

- O modelo de permissao existente permanece valido e sera reutilizado no fluxo de criacao completa.
- O calendario ja possui politica oficial para resolucao/registro de conflitos de agenda.
- A janela de transicao para consumidores do payload antigo e limitada a 2 releases minor ou 90 dias apos a liberacao da feature (o que ocorrer primeiro).
- O fluxo de alteracao e cancelamento segue independente desta entrega, desde que continue funcional sobre eventos criados no novo formato.

## Success Criteria *(mandatory)*


### Measurable Outcomes

- **SC-001**: 100% das criacoes de evento validas sao persistidas e retornadas com identificador unico.
- **SC-002**: 100% das criacoes invalidas sao rejeitadas com erro deterministico e sem escrita parcial.
- **SC-003**: O tempo mediano para concluir cadastro de evento completo pelo cliente reduz em pelo menos 30% em relacao ao fluxo multioperacao atual.
- **SC-004**: 95% das consultas de listagem refletem o evento recem-criado em ate 2 segundos apos confirmacao de criacao, em condicoes operacionais normais.

## Measurement and Evidence Plan *(mandatory)*

- Evidencia para SC-001: suite de testes de integracao cobrindo criacao valida e verificacao de persistencia/retorno.
- Evidencia para SC-002: suite de testes de contrato e integracao para validacoes de campo e regras de negocio sem escrita parcial.
- Evidencia para SC-003: comparativo de telemetry de fluxo de cadastro antes/depois da entrega, com mesma janela de observacao.
- Evidencia para SC-004: medicao operacional de consistencia de leitura apos escrita em ambiente de validacao.
- Cadencia de medicao: baseline pre-merge e acompanhamento semanal nas duas primeiras semanas apos liberacao.
- Evidencias obrigatorias no PR: cenarios de aceite P1/P2/P3, resultados de testes, exemplos de payload completo aceito/rejeitado e amostras de auditoria operacional.
