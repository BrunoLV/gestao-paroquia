# Research: Observacoes de Evento com Controle de Tipo e Autoria

## Decision 1: Criacao manual aceita apenas `NOTA`
- Decision: O endpoint publico/manual de observacoes aceita exclusivamente o tipo `NOTA`; todos os demais tipos permanecem reservados a fluxos sistêmicos de evento.
- Rationale: Preserva a confiabilidade da trilha funcional, evitando que clientes fabriquem observacoes que representam decisoes ou mutacoes do sistema.
- Alternatives considered:
  - Permitir qualquer enum no endpoint manual: rejeitado por violar a separacao entre comentario do usuario e evidência de acao do sistema.
  - Remover o campo `tipo` do contrato manual: rejeitado nesta fase para manter compatibilidade evolutiva e erro deterministico para tipos invalidos.

## Decision 2: Separar servico de nota manual e servico de observacao sistêmica
- Decision: A feature deve introduzir um fluxo dedicado para criacao/edicao/exclusao/listagem de `NOTA` e um fluxo distinto para gravacao automatica de observacoes sistêmicas.
- Rationale: A separacao reduz acoplamento entre comportamento humano e acoes automatizadas, facilita testes e deixa as invariantes de cada grupo de tipos mais objetivas.
- Alternatives considered:
  - Um unico service com ramificacoes por tipo: rejeitado por concentrar regras heterogeneas e ampliar o risco de regressao.

## Decision 3: Listagem funcional possui dois modos explicitos
- Decision: O dominio expoe dois modos de consulta: "minhas observacoes" e "todas as observacoes do evento", ambos com ordenacao deterministica por `criadoEmUtc` e `id`.
- Rationale: A necessidade de consulta pessoal e consulta global foi explicitada na spec e afeta autorizacao, contrato e validacao de resultados.
- Alternatives considered:
  - Apenas um endpoint com filtro opcional implicito: aceitavel tecnicamente, mas rejeitado como decisao de design porque deixa a semantica menos visivel na especificacao.

## Decision 4: Exclusao de `NOTA` e soft delete com ocultacao da projecao funcional
- Decision: Exclusao e permitida apenas para `NOTA` do proprio autor, por soft delete; notas removidas saem das listagens funcionais e permanecem acessiveis apenas por trilhas internas de auditoria.
- Rationale: Mantem experiencia operacional limpa para usuarios e preserva integridade historica exigida pela governanca da API.
- Alternatives considered:
  - Hard delete: rejeitado por quebrar auditabilidade.
  - Exibir nota removida nas consultas normais: rejeitado por introduzir ruído no historico operacional do evento.

## Decision 5: Edicao de `NOTA` preserva historico de revisoes
- Decision: A edicao altera o conteudo atual da `NOTA`, mas deve persistir revisoes auditaveis contendo conteudo anterior, conteudo novo, autor da revisao e timestamp.
- Rationale: Atende a necessidade de correcao do autor sem sacrificar rastreabilidade, alinhando a feature ao principio constitucional de auditabilidade.
- Alternatives considered:
  - Sobrescrever sem historico: rejeitado por perda de rastreabilidade.
  - Criar nova nota a cada edicao: rejeitado por piorar UX e poluir a linha do tempo funcional.

## Decision 6: Observacoes sistêmicas herdam autoria humana quando houver ator identificado
- Decision: Toda observacao sistêmica e atribuida ao usuario humano responsavel pela acao; somente fluxos automáticos sem ator humano usam usuario tecnico do sistema como fallback.
- Rationale: A trilha funcional precisa representar responsabilidade real da acao administrativa ou automatizada.
- Alternatives considered:
  - Usar sempre usuario tecnico do sistema: rejeitado por esconder o ator humano real.
  - Atribuir sempre ao solicitante original: rejeitado porque pode divergir do decisor ou executor efetivo.

## Decision 7: Conteudo de observacao sistêmica deve espelhar o texto do fluxo de origem
- Decision: Tipos sistêmicos (`CANCELAMENTO`, `JUSTIFICATIVA`, `APROVACAO`, `REPROVACAO`, `AJUSTE_HORARIO`) devem gravar exatamente o texto justificativo, decisorio ou motivacional capturado na acao que os gerou.
- Rationale: Garante nexo entre o fato de negocio e a evidência persistida, reduzindo discrepancias entre aprovacao, evento e observacao.
- Alternatives considered:
  - Permitir texto independente no momento da persistencia da observacao: rejeitado por romper a rastreabilidade de origem.

## Decision 8: Evolucao do modelo de persistencia deve ser incremental sobre `observacoes_evento`
- Decision: A feature deve evoluir a persistencia existente com metadados de remocao logica e suporte a historico de revisoes, sem criar agregado paralelo para observacoes do evento.
- Rationale: O projeto ja possui entidade, repositorio e migration base para observacoes; evoluir o modelo atual preserva simplicidade arquitetural e compatibilidade operacional.
- Alternatives considered:
  - Criar tabelas independentes para notas e observacoes sistêmicas: rejeitado por duplicar regras e projeções.

## Decision 9: Contrato de resposta deve expor autoria e data de criacao em todas as consultas funcionais
- Decision: Respostas de criacao e listagem devem incluir `usuarioId` e `criadoEmUtc`, independentemente do tipo da observacao retornada.
- Rationale: Esses campos foram explicitamente exigidos e são necessários para UX, auditoria básica e validação de autoria.
- Alternatives considered:
  - Expor autoria apenas em listagem completa: rejeitado por inconsistência de contrato.

## Decision 10: Cobertura de testes deve separar manual, sistêmico, auditoria e projeção funcional
- Decision: A validacao da feature precisa cobrir criacao manual de `NOTA`, listagem em dois modos, edicao com revisoes, exclusao logica com ocultacao, rejeicoes por autoria/tipo e geracao automática de observacoes sistêmicas.
- Rationale: A feature combina regras de contrato, RBAC, persistencia e auditabilidade; uma unica suite superficial nao capturaria regressões críticas.
- Alternatives considered:
  - Testar apenas endpoints felizes: rejeitado por insuficiência para critérios de sucesso SC-001..SC-012.

## Implementation-Ready Findings
- Stack confirmada: Java 21 + Spring Boot 3.3.x + Validation + Security + Data JPA + Flyway + PostgreSQL/H2.
- O contrato atual de observacoes possui apenas `POST` e `GET`; `PATCH`, `DELETE` e o modo explicito "minhas" serao extensoes desta feature.
- `CreateObservacaoUseCase` e `ListObservacoesUseCase` ainda estao stubados e precisarao ser substituidos por persistencia e politicas reais.
- Nao ha `NEEDS CLARIFICATION` remanescente para a fase de design.
