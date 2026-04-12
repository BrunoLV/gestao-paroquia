# Research: Cancelamento de Evento

## Decision 1: Cancelamento é soft delete exclusivo para eventos CONFIRMADO
- Decision: Apenas eventos no status `CONFIRMADO` podem transitar para `CANCELADO`; a operação atualiza `status` e `canceladoMotivo` sem remoção física.
- Rationale: Alinha a feature ao lifecycle definido na spec e evita ambiguidade sobre `RASCUNHO`, `ADICIONADO_EXTRA` e `CANCELADO`.
- Alternatives considered:
  - Permitir `RASCUNHO -> CANCELADO`: rejeitado por decisão explícita da especificação.
  - Hard delete: rejeitado por violar requisito de rastreabilidade e preservação histórica.

## Decision 2: Dois caminhos operacionais no mesmo endpoint de cancelamento
- Decision: `DELETE /eventos/{eventoId}` deve cancelar imediatamente para pároco e liderança do conselho, mas criar solicitação pendente para vigário e liderança de pastoral/laicato.
- Rationale: Preserva um único ponto de entrada para o ato de cancelar, mantendo governança por papel sem duplicar endpoint funcional.
- Alternatives considered:
  - Criar um endpoint separado para “solicitar cancelamento”: rejeitado por piorar UX e dispersar o contrato do domínio.

## Decision 3: Aprovação executa automaticamente a ação pendente
- Decision: Quando a aprovação é necessária, a própria requisição de cancelamento deve persistir uma solicitação pendente com snapshot do payload; ao decidir `APROVADA`, o sistema efetiva automaticamente o cancelamento sem ressubmissão.
- Rationale: Elimina janela de inconsistência entre “o que foi aprovado” e “o que seria reenviado depois”, além de seguir a clarificação fornecida para todas as ações do sistema que exigem autorização.
- Alternatives considered:
  - Requerer novo `DELETE` com `aprovacaoId`: rejeitado pela nova regra funcional e por introduzir risco de divergência de dados.

## Decision 4: Reusar a infraestrutura de aprovação armazenando snapshot da ação
- Decision: Estender o modelo de `Aprovacao` existente para armazenar dados suficientes da ação pendente de cancelamento (evento alvo, motivo, tipo da ação e contexto mínimo do solicitante), permitindo execução automática após aprovação.
- Rationale: Mantém a simplicidade arquitetural ao reaproveitar um subdomínio já presente e evita criar infraestrutura paralela de workflow.
- Alternatives considered:
  - Criar tabela/entidade isolada para fila de ações pendentes: rejeitado nesta fase por aumentar complexidade sem necessidade comprovada.

## Decision 5: Escopo organizacional depende do papel, não apenas do tipo de operação
- Decision: Pároco, coordenador e vice-coordenador do conselho, e vigário podem solicitar cancelamento para eventos de qualquer organização; coordenador e vice-coordenador de pastoral/laicato só podem solicitar para a própria organização.
- Rationale: Reflete a clarificação formalizada e evita sobreposição indevida entre autoridade transversal e autoridade local.
- Alternatives considered:
  - Restringir vigário à própria organização: rejeitado pela decisão do usuário.

## Decision 6: Motivo do cancelamento pertence ao pedido original e deve ser preservado até a execução
- Decision: O motivo informado na solicitação inicial deve ser o mesmo persistido no evento e na observação quando a aprovação posterior efetivar a ação.
- Rationale: Garante integridade do ato administrativo aprovado e evita mutação do conteúdo entre solicitação e execução.
- Alternatives considered:
  - Permitir editar o motivo no momento da aprovação: rejeitado por quebrar o nexo entre solicitação e decisão.

## Decision 7: Falha na efetivação automática após aprovação deve ser auditável e segura
- Decision: Se o evento deixar de estar `CONFIRMADO` ou outra pré-condição crítica falhar antes da execução automática, a aprovação permanece registrada, mas a efetivação retorna estado de falha auditável (`APPROVAL_EXECUTION_FAILED`) sem alterar o evento.
- Rationale: Mantém consistência transacional e oferece diagnóstico claro para suporte administrativo.
- Alternatives considered:
  - Forçar a efetivação apesar da mudança de estado: rejeitado por violar invariantes do lifecycle.

## Decision 8: Cancelamento bem-sucedido sempre gera observação append-only e trilha estruturada
- Decision: Todo cancelamento efetivado deve gravar observação do tipo `CANCELAMENTO` e log estruturado com `correlationId`, ator, aprovador quando aplicável e resultado final.
- Rationale: Atende o princípio constitucional de rastreabilidade operacional e o requisito específico de auditabilidade.
- Alternatives considered:
  - Auditar apenas via logs ou apenas via observações: rejeitado porque cada mecanismo cobre necessidades diferentes (diagnóstico operacional vs histórico funcional).

## Decision 9: Contrato HTTP deve diferenciar ação concluída de ação pendente
- Decision: `DELETE /eventos/{eventoId}` retorna `200 OK` com evento cancelado quando a ação é imediata, e `202 Accepted` com identificador da solicitação quando depende de aprovação.
- Rationale: Expressa claramente ao cliente se a mutação já ocorreu ou se ainda aguarda decisão.
- Alternatives considered:
  - Retornar sempre `200`: rejeitado por esconder o estado assíncrono/administrativo do fluxo pendente.

## Decision 10: Cobertura de testes deve separar solicitação, decisão e efetivação
- Decision: A suíte da feature deve ter cobertura para cancelamento direto, criação de solicitação pendente, aprovação com execução automática, reprovação, falha de execução após aprovação, negação por papel e visibilidade de evento cancelado.
- Rationale: O fluxo agora cruza dois endpoints e uma transição assíncrona administrativa, o que exige validação explícita de cada estágio.
- Alternatives considered:
  - Testar apenas o resultado final de cancelamento: rejeitado por não capturar regressões na criação e decisão da solicitação.

## Implementation-Ready Findings
- Stack confirmada: Java 21 + Spring Boot 3.3.5 + Data JPA + Flyway + PostgreSQL/H2.
- O endpoint `DELETE /eventos/{eventoId}` existe, mas hoje está em comportamento no-op e precisa assumir semântica real.
- O fluxo de aprovação já existe, porém precisa armazenar snapshot da ação e disparar execução automática na aprovação.
- Não há `NEEDS CLARIFICATION` remanescente para esta fase.
