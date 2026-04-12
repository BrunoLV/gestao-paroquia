# Research: Execucao Automatica Pos-Aprovacao para Criacao e Edicao

## Decision 1: Criacao e edicao sensivel passam a ter modo pendente orientado a aprovacao
- Decision: Quando o ator nao tiver permissao de efetivacao imediata, `POST /eventos` e `PATCH /eventos/{id}` devem criar solicitacao pendente em vez de exigir novo envio posterior.
- Rationale: Remove retrabalho manual e alinha o comportamento ao fluxo de cancelamento ja adotado.
- Alternatives considered:
  - Manter erro `APPROVAL_REQUIRED` e exigir reenviar request apos aprovacao: rejeitado pelo requisito funcional da feature.

## Decision 2: Aprovacao deve disparar execucao automatica para `CRIACAO_EVENTO` e `EDICAO_EVENTO`
- Decision: `PATCH /aprovacoes/{id}` com decisao `APROVADA` executa automaticamente a acao snapshot sem nova chamada de negocio do cliente.
- Rationale: Garante nexo entre o que foi aprovado e o que sera efetivado.
- Alternatives considered:
  - Criar endpoint separado de "executar acao aprovada": rejeitado por aumentar acoplamento e friccao para clientes.

## Decision 3: Snapshot imutavel da acao pendente deve ser persistido no subdominio de aprovacao
- Decision: Registrar payload imutavel e suficiente para executar criacao/edicao automaticamente.
- Rationale: Evita divergencia entre requisicao original e execucao posterior.
- Alternatives considered:
  - Recalcular a acao a partir de estado atual apenas: rejeitado por nao refletir necessariamente a intencao aprovada.

## Decision 4: Validacoes de dominio devem ser reaplicadas no momento da execucao automatica
- Decision: Na efetivacao automatica, o sistema deve rodar as mesmas validacoes de calendario/status/organizacao da operacao sincrona.
- Rationale: Evita persistencia de dados invalidos quando o contexto muda entre solicitacao e decisao.
- Alternatives considered:
  - Confiar apenas na validacao no momento da solicitacao: rejeitado por risco de corrida temporal e regressao de integridade.

## Decision 5: Idempotencia de criacao precisa ser preservada na efetivacao pos-aprovacao
- Decision: Fluxo automatizado de criacao reutiliza a estrategia de idempotencia para evitar duplicidades.
- Rationale: Evita criar dois eventos quando houver retries ou decisoes processadas sob falha de rede.
- Alternatives considered:
  - Ignorar idempotencia no caminho automatizado: rejeitado por risco de duplicidade operacional.

## Decision 6: Resultado da decisao precisa informar outcome operacional da execucao
- Decision: Resposta de decisao inclui `EXECUTED`, `REJECTED` ou `FAILED` e referencia ao alvo impactado.
- Rationale: Clientes e operacao precisam de feedback deterministico do resultado final.
- Alternatives considered:
  - Retornar apenas status da aprovacao: rejeitado por ocultar estado da efetivacao automatica.

## Decision 7: Falha de execucao apos aprovacao deve ser segura e auditavel
- Decision: Em falha de efetivacao, decisao aprovada fica registrada, mas sem aplicacao parcial da mutacao.
- Rationale: Preserva consistencia e permite reprocessamento/atuacao operacional com rastreabilidade.
- Alternatives considered:
  - Rollback total incluindo decisao: rejeitado por perder trilha administrativa da aprovacao concedida.

## Decision 8: Observabilidade obrigatoria por etapa
- Decision: Auditoria e metricas devem cobrir solicitacao pendente, decisao, execucao automatica e falha.
- Rationale: Atende constituicao de rastreabilidade e suporta diagnostico.
- Alternatives considered:
  - Auditar apenas mutacoes finais: rejeitado por nao cobrir pontos de falha no workflow assincorno.

## Decision 9: Compatibilidade preservada para autorizacao imediata
- Decision: Fluxos atuais de criacao/edicao imediata para perfis autorizados permanecem sincronos e inalterados no contrato principal.
- Rationale: Evita breaking change desnecessaria para atores ja autorizados.
- Alternatives considered:
  - Tornar tudo pendente por padrao: rejeitado por impacto funcional e de UX.

## Decision 10: Reuso de componentes existentes da camada de aplicacao
- Decision: Evoluir use cases de evento e aprovacao existentes, evitando motor externo de workflow.
- Rationale: Menor risco, menor custo de manutencao e aderencia a arquitetura atual.
- Alternatives considered:
  - Introduzir fila externa/orquestrador dedicado nesta fase: rejeitado por sobrecomplexidade para o escopo atual.

## Implementation-Ready Findings
- Stack e arquitetura atuais suportam extensao sem novas dependencias.
- Nao ha `NEEDS CLARIFICATION` remanescente.
- Feature pronta para detalhamento de tarefas de implementacao em `/speckit.tasks`.
