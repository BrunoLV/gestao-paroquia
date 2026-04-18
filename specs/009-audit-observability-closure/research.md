# Research: Fechamento de Auditoria e Retrabalho

## Decision 1: Persistir trilha auditável em estrutura própria e imutável
- Decision: A feature deve introduzir persistência dedicada para registros auditáveis, separada do log textual produzido por `AuditLogService`, usando tabela e repositório próprios para consulta operacional.
- Rationale: O requisito constitucional exige trilha imutável e consultável por período e organização; logs estruturados em texto não oferecem garantias suficientes de consulta determinística, filtro semântico e retenção funcional.
- Alternatives considered:
  - Manter apenas logs estruturados como fonte de consulta: rejeitado por não atender de forma robusta à auditabilidade consultável.
  - Persistir auditoria como blob genérico em tabela de eventos sem índice semântico: rejeitado por dificultar filtros por período, organização e actor.

## Decision 2: A leitura operacional deve usar `organizacaoId` obrigatório
- Decision: Tanto a trilha auditável quanto a taxa de retrabalho exigem `organizacaoId` explícito em toda consulta.
- Rationale: Isso simplifica autorização, evita agregação implícita fora do escopo do solicitante e reduz o risco de vazamento entre organizações.
- Alternatives considered:
  - Inferir organização principal do usuário: rejeitado por comportamento implícito e risco de ambiguidades.
  - Agregar todas as organizações acessíveis quando ausente: rejeitado por ampliar demais o escopo de leitura operacional.

## Decision 3: Representar período por granularidade ou intervalo explícito
- Decision: Os endpoints operacionais aceitam período via granularidade predefinida (`diario`, `semanal`, `mensal`, `anual`) ou por `inicio/fim`, nunca ambos.
- Rationale: A combinação cobre relatórios recorrentes e investigações ad hoc sem introduzir semântica ambígua.
- Alternatives considered:
  - Apenas granularidade fixa: rejeitado por limitar investigações específicas.
  - Apenas `inicio/fim`: rejeitado por perder ergonomia para uso operacional recorrente.
  - Aceitar ambos simultaneamente: rejeitado por criar ambiguidade contratual desnecessária.

## Decision 4: Falha de gravação auditável adota fail-closed
- Decision: Quando a persistência auditável obrigatória falhar, a mutação de domínio correspondente deve falhar integralmente.
- Rationale: A constituição exige rastreabilidade obrigatória em mutações; confirmar estado sem trilha persistida produziria lacuna irrecuperável de auditoria.
- Alternatives considered:
  - Confirmar a mutação e reconciliar depois: rejeitado por permitir perda definitiva de nexo causal.
  - Aplicar fail-closed apenas a eventos: rejeitado por enfraquecer consistência entre eventos, aprovações e observações.

## Decision 5: Reaproveitar o stack JPA/Flyway existente
- Decision: A solução deve usar JPA para persistência, migrations Flyway para schema e Spring transactions para atomicidade entre mutação e gravação auditável.
- Rationale: O repositório já adota esse baseline para entidades e casos de uso críticos; reutilizar o stack reduz custo arquitetural e simplifica testes.
- Alternatives considered:
  - Introduzir store/event bus especializado para auditoria: rejeitado por aumentar complexidade e dependências fora do escopo.

## Decision 6: Taxa de retrabalho será derivada de ocorrências elegíveis e eventos afetados
- Decision: O indicador de retrabalho será exposto como taxa, com numerador e denominador explícitos no contrato de resposta.
- Rationale: A taxa permite comparação entre períodos e organizações com volumes diferentes, além de tornar a interpretação operacional menos sujeita a viés de escala.
- Alternatives considered:
  - Contagem absoluta apenas: rejeitado por baixa comparabilidade.
  - Score composto opaco: rejeitado por reduzir auditabilidade do cálculo.

## Decision 7: A fonte preferencial do retrabalho será a própria trilha auditável persistida
- Decision: O cálculo da taxa deve se apoiar em ocorrências auditáveis persistidas, complementadas apenas quando necessário por dados de apoio do domínio para determinar o denominador de eventos afetados.
- Rationale: Usar a mesma trilha persistida como fonte primária mantém consistência entre auditoria e métrica e reduz divergência entre o que foi operado e o que foi medido.
- Alternatives considered:
  - Calcular a taxa apenas com contadores em memória do publisher atual: rejeitado por não oferecer histórico confiável nem recorte por organização.

## Decision 8: A baseline semanal permanece obrigatória e deve evoluir para histórico consultável
- Decision: O snapshot semanal já existente deve continuar e passar a refletir a nova base de auditoria e o cálculo real da taxa de retrabalho.
- Rationale: A constituição exige disciplina de baseline e análise histórica; a feature precisa preservar isso e não apenas adicionar endpoints de leitura pontual.
- Alternatives considered:
  - Abandonar snapshot e depender só de consultas sob demanda: rejeitado por enfraquecer comparação histórica.

## Decision 9: Sem novas dependências externas
- Decision: A implementação deve evitar adicionar bibliotecas ou infraestrutura nova para auditoria, aproveitando Spring Boot, JPA, Flyway, PostgreSQL/H2 e o handler global existentes.
- Rationale: O problema é de modelagem e persistência, não de capacidade ausente do stack atual.
- Alternatives considered:
  - Introduzir framework de auditoria de terceiros: rejeitado por custo e acoplamento desnecessários.

## Decision 10: Cobertura de testes deve separar contrato, integração, segurança, falha e performance
- Decision: A validação da feature deve incluir suites dedicadas para contrato HTTP, escopo organizacional, cálculo do retrabalho, fail-closed e p95 dos novos endpoints.
- Rationale: O risco principal da feature está em autorização, consistência transacional e interpretação operacional, não apenas em payload feliz.
- Alternatives considered:
  - Cobrir apenas smoke tests funcionais: rejeitado por insuficiência para os gates constitucionais.

## Implementation-Ready Findings
- Stack confirmada: Java 21 + Spring Boot 3.3.13 + Validation + Security + Data JPA + Flyway + PostgreSQL/H2.
- O controlador de auditoria atual expõe apenas `/extras`; os endpoints de trilha e retrabalho serão extensões incrementais do mesmo agregado HTTP.
- `AuditLogService` hoje só escreve log estruturado; ele precisará ser complementado ou adaptado para gravação persistida.
- `GetIndicadorRetrabalhoUseCase` é placeholder e pode ser substituído por cálculo real sem mudança de stack.
- Não há `NEEDS CLARIFICATION` remanescente para a fase de design.
