# Research: PATCH Completo de Evento

## Decision 1: Substituir PATCH mockado por fluxo persistente transacional
- Decision: `UpdateEventoUseCase` deve carregar o evento por id, aplicar merge parcial dos campos enviados, validar regras de dominio e persistir via JPA em transacao unica.
- Rationale: Elimina retorno ficticio, garante consistencia de estado e atende FR-001/FR-004/FR-005/FR-007.
- Alternatives considered:
  - Manter resposta mock com validacao parcial: rejeitado por nao interagir com banco.
  - Implementar patch direto no controller: rejeitado por violar separacao de camadas.

## Decision 2: Reutilizar politicas de dominio existentes para integridade temporal
- Decision: Continuar usando `EventoDomainService` + `CalendarIntegrityPolicy` para validar intervalo (`fim > inicio`) e regras de status sensiveis.
- Rationale: Preserva centralizacao da regra de negocio e evita duplicacao.
- Alternatives considered:
  - Reimplementar validacao no use case: rejeitado por risco de divergencia.

## Decision 3: Aplicar no PATCH o mesmo eixo de permissionamento do create
- Decision: Edicoes gerais em PATCH so podem ser realizadas por coordenador/vice-coordenador da organizacao responsavel.
- Rationale: Alinhamento com clarificacoes e com fonte canonica de permissionamento.
- Alternatives considered:
  - Regras diferentes por operacao: rejeitado por inconsistencia de governanca.

## Decision 4: Regra especifica para organizacoes participantes e responsavel
- Decision: Coordenador e vice da organizacao responsavel podem adicionar participantes; alteracao da organizacao responsavel e permitida somente para coordenador do conselho ou parroco.
- Rationale: Atende FR-013/FR-014/FR-015 e reduz risco de mutacao administrativa indevida.
- Alternatives considered:
  - Permitir troca de organizacao responsavel por coordenador local: rejeitado por violar segregacao de autoridade.

## Decision 5: Exigir aprovacao para alteracoes de data e cancelamento
- Decision: Mudancas em data/horario ou cancelamento exigem aprovacao valida de coordenador/vice do conselho ou parroco; sem aprovacao, resposta deterministica `APPROVAL_REQUIRED` e nenhuma persistencia parcial.
- Rationale: Atende FR-011/FR-012 e reforca trilha de governanca.
- Alternatives considered:
  - Aprovar implicitamente no proprio PATCH sem trilha: rejeitado por baixa auditabilidade.

## Decision 6: Consolidar matriz de erros deterministica no handler global
- Decision: Mapear cenarios de patch para `EVENT_NOT_FOUND`, `VALIDATION_ERROR`, `BUSINESS_RULE_VIOLATION`, `FORBIDDEN`, `APPROVAL_REQUIRED` e `CONFLICT` com payload padrao de erro.
- Rationale: Contrato previsivel para clientes e suporte operacional.
- Alternatives considered:
  - Reusar apenas `Exception` generica: rejeitado por baixa diagnosabilidade.

## Decision 7: Completar infraestrutura de aprovacao com persistencia real
- Decision: Formalizar persistencia de aprovacao (entity/repository/migration) para que o PATCH possa validar aprovacao de forma rastreavel.
- Rationale: Sem armazenamento de aprovacao nao ha evidencia auditavel confiavel.
- Alternatives considered:
  - Guardar aprovacao em memoria: rejeitado por perda de consistencia em ambiente distribuido.

## Decision 8: Preservar observabilidade obrigatoria em cada tentativa de PATCH
- Decision: Toda tentativa deve gerar trilha com actor, action, target, resultado, correlation id e metadados de aprovacao quando aplicavel.
- Rationale: Atende principio constitucional de rastreabilidade operacional.
- Alternatives considered:
  - Auditar apenas sucesso: rejeitado por dificultar investigacao de negacoes.

## Decision 9: Tratar concorrencia com lock otimista ja existente
- Decision: Usar `@Version` de `BaseVersionedEntity` para detectar atualizacoes concorrentes e retornar `CONFLICT` deterministico.
- Rationale: Evita sobrescrita silenciosa e preserva consistencia.
- Alternatives considered:
  - Last-write-wins sem sinalizar conflito: rejeitado por risco de perda de dados.

## Decision 10: Cobertura de testes orientada a historias e cenarios negativos
- Decision: Implementar testes de integracao/contrato para sucesso persistente, negacao por permissao, negacao por aprovacao, evento inexistente, conflito concorrente e ausencia de persistencia parcial em falha.
- Rationale: Materializa Testability Gate e protege regressao.
- Alternatives considered:
  - Cobrir apenas happy path: rejeitado por nao atender requisitos de governanca e seguranca.

## Implementation-Ready Findings
- Stack confirmado: Java 21 + Spring Boot 3.3.5 + Spring Data JPA + Flyway + PostgreSQL.
- Fluxo atual de PATCH esta mockado e precisa de persistencia real.
- Politica de autorizacao existe no dominio, mas ainda nao esta aplicada ao fluxo de update.
- Ha lacuna de persistencia de aprovacao que precisa ser fechada para validar regras de data/cancelamento.
- Nao ha `NEEDS CLARIFICATION` remanescente para esta fase.
