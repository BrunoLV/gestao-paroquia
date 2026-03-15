# Research: Criacao Completa de Evento

## Decision 1: Persistencia real no fluxo de criacao e listagem
- Decision: O fluxo de criacao deve sair de mock e persistir em `calendario.eventos`; a listagem deve ler do repositorio JPA em vez de retornar payload fixo.
- Rationale: Atende FR-001/FR-002/FR-003 e elimina divergencia entre comportamento de API e estado real.
- Alternatives considered:
  - Manter mock no `GET /eventos`: rejeitado por nao entregar valor operacional.
  - Persistir apenas em memoria: rejeitado por nao atender consistencia transacional.

## Decision 2: Evento completo em uma unica operacao de criacao
- Decision: `POST /api/v1/eventos` recebe payload completo e suficiente para criar um evento valido sem etapas obrigatorias subsequentes.
- Rationale: Reduz retrabalho e simplifica integracao de clientes (SC-003).
- Alternatives considered:
  - Manter composicao obrigatoria por multiplos endpoints: rejeitado por complexidade operacional.

## Decision 3: Idempotencia explicita por `Idempotency-Key`
- Decision: Exigir cabecalho `Idempotency-Key` no `POST /api/v1/eventos` e reutilizar resposta da primeira criacao equivalente.
- Rationale: Evita duplicidade em retries de rede e garante previsibilidade para clientes.
- Alternatives considered:
  - Idempotencia implicita por campos de negocio: rejeitado por risco de colisao semantica.
  - Sem idempotencia: rejeitado por risco de duplicidade.

## Decision 4: Conflito de agenda nao bloqueante no create
- Decision: Sobreposicao de horario/recurso nao bloqueia criacao; evento e persistido com estado `CONFLICT_PENDING`.
- Rationale: Preserva fluxo operacional e permite resolucao administrativa posterior com trilha auditavel.
- Alternatives considered:
  - Bloquear com erro `CONFLICT`: rejeitado por interromper operacao critica.

## Decision 5: RBAC de criacao no escopo organizacional
- Decision: Criacao permitida apenas para Admin Geral e Admin de Pastoral, sendo Admin de Pastoral limitado ao proprio escopo organizacional.
- Rationale: Balanceia autonomia local com governanca institucional.
- Alternatives considered:
  - Somente Admin Geral: rejeitado por gargalo operacional.
  - Qualquer autenticado: rejeitado por risco de permissao excessiva.

## Decision 6: Listagem autenticada
- Decision: `GET /api/v1/eventos` exige autenticacao em qualquer status de evento para este fluxo.
- Rationale: Evita exposicao indevida de estados internos durante transicao para persistencia completa.
- Alternatives considered:
  - Expor `CONFIRMADO` anonimamente neste endpoint: rejeitado para manter regra simples e segura neste escopo.

## Decision 7: Rejeicao estrita de campos desconhecidos
- Decision: Campos nao documentados no payload de criacao devem retornar erro de validacao deterministico.
- Rationale: Contrato explicito e menos erro silencioso de integracao.
- Alternatives considered:
  - Ignorar silenciosamente: rejeitado por mascarar defeitos de cliente.

## Decision 8: Timezone canonico e validacao temporal
- Decision: Persistir em UTC (`inicio_utc`, `fim_utc`) e validar `fim > inicio` antes de persistir.
- Rationale: Coerencia com politica de integridade do calendario e com schema existente.
- Alternatives considered:
  - Persistir timezone local diretamente: rejeitado por ambiguidade temporal.

## Decision 9: Estrategia de idempotencia de armazenamento
- Decision: Registrar chave de idempotencia vinculada ao resultado da criacao (evento e hash de requisicao) para suportar replay seguro e detectar reutilizacao indevida da chave com payload divergente.
- Rationale: Garante comportamento deterministico em retries e permite auditoria.
- Alternatives considered:
  - Cache em memoria por instancia: rejeitado por inconsistencias em ambiente distribuido.

## Decision 10: Observabilidade obrigatoria no create
- Decision: Toda tentativa de criacao (sucesso/falha) deve emitir log/auditoria com correlation id, ator, resultado e categoria de erro.
- Rationale: Atende principio constitucional de rastreabilidade operacional.
- Alternatives considered:
  - Auditar apenas sucesso: rejeitado por baixa capacidade de diagnostico.

## Implementation-Ready Findings
- Stack e baseline confirmados: Java 21 + Spring Boot 3.3.5 + Spring Data JPA + PostgreSQL + Flyway.
- Estrutura do repositorio suporta implementacao no modulo unico `app`.
- Politica de integridade temporal ja existe (`CalendarIntegrityPolicy`) e deve ser reutilizada no fluxo persistente.
- Nao ha `NEEDS CLARIFICATION` remanescente para planejamento.
