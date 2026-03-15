# Feature Specification: PATCH Completo de Evento

**Feature Branch**: `004-complete-event-patch`  
**Created**: 2026-03-15  
**Status**: Draft  
**Input**: User description: "implementação integral da operação patch em Evento controller não mais mockando o retorno, onde as regras devem ser aplicadas e os dados devem interagir com o banco de dados."

## Clarifications

### Session 2026-03-15

- Q: Quais regras de permissionamento do PATCH devem ser aplicadas em relação ao create? → A: O PATCH deve aplicar as mesmas regras de permissionamento já existentes no create.
- Q: Quem pode editar um evento no PATCH? → A: Apenas coordenador ou vice-coordenador da organização responsável pelo evento.
- Q: Qual regra adicional vale para edição de datas ou cancelamentos? → A: Alterações de data ou cancelamento exigem aprovação do coordenador ou vice-coordenador do conselho, ou do pároco.
- Q: Como funciona o permissionamento para organizações participantes e organização responsável no PATCH? → A: Coordenador e vice-coordenador da organização podem adicionar organizações participantes, mas não podem alterar a organização responsável; essa alteração só pode ser feita pelo coordenador do conselho ou pelo pároco.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Atualizar Parcialmente Evento Persistido (Priority: P1)

Como coordenador ou vice-coordenador da organização responsável, quero atualizar apenas campos específicos de um evento existente para corrigir informações sem recriar todo o registro.

**Why this priority**: É o fluxo central da funcionalidade PATCH e elimina comportamento simulado, garantindo valor imediato com persistência real.

**Independent Test**: Pode ser testada de forma independente enviando uma atualização parcial válida para um evento existente e validando persistência e resposta refletindo os novos valores.

**Acceptance Scenarios**:

1. **Given** um evento existente com data, horário e descrição cadastrados, **When** coordenador ou vice-coordenador da organização responsável envia PATCH alterando apenas a descrição, **Then** a descrição é atualizada e os demais campos permanecem inalterados.
2. **Given** um evento existente, **When** coordenador ou vice-coordenador da organização responsável envia PATCH com múltiplos campos válidos, **Then** todas as alterações são aplicadas em uma única operação e refletidas na resposta.
3. **Given** um evento existente, **When** coordenador ou vice-coordenador da organização responsável envia PATCH adicionando organizações participantes permitidas, **Then** a atualização é aceita e persistida sem alterar a organização responsável.

---

### User Story 2 - Bloquear Atualizações Inválidas por Regras de Negócio (Priority: P2)

Como coordenador ou vice-coordenador da organização responsável, quero receber erro claro quando a atualização parcial violar regras do calendário ou permissionamento para evitar inconsistências operacionais.

**Why this priority**: Preserva integridade de dados e evita corrupção funcional do calendário.

**Independent Test**: Pode ser testada enviando atualizações que violem regras de domínio e confirmando rejeição sem alteração no registro persistido.

**Acceptance Scenarios**:

1. **Given** um evento existente, **When** coordenador ou vice-coordenador da organização responsável envia PATCH com combinação de dados proibida pelas regras de calendário, **Then** o sistema rejeita a solicitação com erro de validação e não persiste alterações.
2. **Given** um evento existente, **When** coordenador ou vice-coordenador da organização responsável envia PATCH com alteração de data ou cancelamento sem aprovação exigida, **Then** o sistema retorna erro de aprovação obrigatória e mantém os dados originais.
3. **Given** um evento existente, **When** coordenador ou vice-coordenador da organização responsável envia PATCH tentando alterar a organização responsável, **Then** o sistema rejeita a solicitação por permissão insuficiente e mantém os dados originais.

---

### User Story 3 - Resposta e Auditoria Confiáveis (Priority: P3)

Como equipe de operação, quero que cada PATCH registre evidências operacionais e retorne dados reais do banco para rastreabilidade e confiança.

**Why this priority**: Garante observabilidade da mudança e elimina retorno fictício, reduzindo ambiguidade em suporte e auditoria.

**Independent Test**: Pode ser testada validando que um PATCH bem-sucedido produz evento de auditoria e que a resposta coincide com o estado persistido após recarga do registro.

**Acceptance Scenarios**:

1. **Given** uma atualização parcial aplicada com sucesso, **When** a resposta é retornada ao cliente, **Then** os dados correspondem exatamente ao estado armazenado no banco após a transação.
2. **Given** uma atualização rejeitada por regra de negócio, **When** o processo é concluído, **Then** o sistema registra o resultado da tentativa sem expor informações sensíveis.

### Edge Cases

- PATCH recebido sem campos atualizáveis deve retornar erro de requisição inválida.
- PATCH para identificador inexistente deve retornar recurso não encontrado.
- PATCH com campos desconhecidos deve retornar erro de validação explícito.
- PATCH com valor nulo em campo obrigatório de negócio deve retornar erro de validação.
- PATCH concorrente sobre mesmo evento deve preservar consistência, evitando estado parcial não determinístico.
- PATCH por usuário sem perfil de coordenador ou vice-coordenador da organização responsável deve retornar erro de permissão.
- PATCH de data ou cancelamento sem aprovação por coordenador/vice do conselho ou pároco deve retornar erro de aprovação obrigatória.
- PATCH tentando alterar organização responsável por usuário diferente de coordenador do conselho ou pároco deve retornar erro de permissão.

## API Contract & Validation *(mandatory)*

- Endpoint afetado: operação PATCH de atualização parcial de evento existente (contrato existente com comportamento alterado de mock para persistência real).
- Forma de requisição: aceita apenas subconjunto permitido de campos de evento; campos ausentes não são alterados.
- Forma de resposta em sucesso: representação completa do evento após persistência.
- Códigos de erro esperados (machine-readable):
  - `EVENT_NOT_FOUND` para identificador inexistente.
  - `VALIDATION_ERROR` para formato inválido, campos desconhecidos ou payload vazio.
  - `BUSINESS_RULE_VIOLATION` para violações de regras de calendário.
  - `FORBIDDEN` para ausência de permissionamento equivalente ao create.
  - `APPROVAL_REQUIRED` para alteração de data ou cancelamento sem aprovação exigida.
  - `CONFLICT` para concorrência ou conflito de atualização.
- Compatibilidade retroativa: endpoint e método permanecem os mesmos; clientes passam a receber dados reais persistidos e erros determinísticos em vez de retorno simulado.
- Nota de migração: consumidores devem tratar códigos de erro explícitos e confiar na resposta como fonte de verdade persistida.

## Calendar Integrity Rules *(mandatory for calendar/event features)*

- Estratégia de fuso horário canônico: todas as comparações e validações temporais devem seguir um único fuso canônico da aplicação para evitar divergência de agenda.
- Normalização temporal: entradas parciais devem ser normalizadas antes da validação de regra para garantir comparação consistente.
- Regras de conflito: atualização não pode gerar sobreposição inválida de compromissos no mesmo escopo de calendário.
- Regras de duplicidade: atualização não pode criar duplicidade lógica de evento no mesmo intervalo e contexto.
- Ordenação determinística: consultas subsequentes devem refletir estado atualizado com ordenação temporal estável e previsível.

## Operational Observability *(mandatory)*

- Toda tentativa de PATCH deve gerar registro operacional com: identificador do evento, resultado (sucesso/falha), categoria de erro quando aplicável e identificação do solicitante.
- Quando houver fluxo de aprovação para alteração de data ou cancelamento, o registro operacional deve conter identificador do aprovador e resultado da aprovação.
- Quando houver tentativa de alteração de organização responsável, o registro operacional deve indicar perfil do solicitante e motivo de autorização ou rejeição.
- Operações bem-sucedidas devem registrar evidência auditável de campos alterados.
- Operações rejeitadas devem registrar motivo de negação em nível de domínio sem expor dados sensíveis.
- Erros devem fornecer diagnóstico suficiente para suporte (correlation id, categoria e contexto mínimo) sem vazamento de detalhes internos.

## Architecture and Code Standards *(mandatory)*

- A atualização parcial deve respeitar separação por camadas: controller apenas orquestra entrada/saída, regras de negócio em camada de domínio/aplicação e persistência em infraestrutura.
- A funcionalidade deve preservar limites de portas e adaptadores, mantendo regras desacopladas da tecnologia de armazenamento.
- O PATCH deve reutilizar a mesma política de permissionamento já estabelecida para o create, evitando divergência de regras entre operações do mesmo domínio.
- Tratamento de erro deve ser padronizado e mapeado para respostas consistentes.
- A operação deve ser transacional para impedir persistência parcial quando houver falha de validação ou conflito.
- Mudanças devem manter legibilidade, nomenclatura orientada ao domínio e baixa complexidade ciclomática.

## Dependencies and Assumptions

- A operação PATCH de evento já existe e permanece como ponto oficial de atualização parcial.
- Existe repositório persistente ativo para leitura e escrita de eventos.
- Regras de negócio de evento já definidas no domínio devem ser reutilizadas e aplicadas na atualização.
- Regras de permissionamento existentes no create são fonte canônica para autorização do PATCH.
- O escopo desta feature é operação individual de PATCH por evento; não inclui atualização em lote.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST aceitar atualização parcial somente para eventos existentes identificados por chave única.
- **FR-002**: O sistema MUST aplicar regras de validação de entrada para payload PATCH, rejeitando requisição vazia, campos inválidos e tipos incompatíveis.
- **FR-003**: O sistema MUST aplicar regras de negócio de calendário antes da persistência de qualquer alteração parcial.
- **FR-004**: O sistema MUST persistir no banco apenas os campos enviados e válidos, preservando os campos não enviados.
- **FR-005**: O sistema MUST retornar, em sucesso, o estado completo do evento após gravação, sem uso de resposta mockada.
- **FR-006**: O sistema MUST retornar erro determinístico e legível para recurso inexistente, validação inválida, violação de regra de negócio e conflito de concorrência.
- **FR-007**: O sistema MUST garantir que nenhuma alteração parcial seja persistida quando ocorrer erro durante validação, regra de negócio ou conflito.
- **FR-008**: O sistema MUST registrar trilha auditável para todas as tentativas de atualização parcial, incluindo resultado e contexto mínimo.
- **FR-009**: Usuários autorizados MUST ser capazes de confirmar a atualização consultando novamente o evento e obtendo os mesmos dados retornados pelo PATCH.
- **FR-010**: O sistema MUST autorizar edições gerais de evento no PATCH apenas para coordenador ou vice-coordenador da organização responsável, usando a mesma política de permissionamento do create.
- **FR-011**: O sistema MUST exigir aprovação do coordenador ou vice-coordenador do conselho, ou do pároco, para alterações relacionadas a data ou cancelamento.
- **FR-012**: O sistema MUST rejeitar alterações de data ou cancelamento sem aprovação exigida, sem persistir qualquer alteração parcial.
- **FR-013**: O sistema MUST permitir que coordenador ou vice-coordenador da organização responsável adicione organizações participantes quando o payload estiver válido.
- **FR-014**: O sistema MUST impedir que coordenador ou vice-coordenador da organização responsável altere a organização responsável do evento.
- **FR-015**: O sistema MUST permitir alteração da organização responsável somente quando a operação for realizada por coordenador do conselho ou pároco.

### Key Entities *(include if feature involves data)*

- **Evento**: compromisso do calendário paroquial com atributos de identificação, temporalidade, descrição, status e metadados operacionais.
- **Patch de Evento**: conjunto parcial de alterações solicitadas para um Evento, contendo apenas campos atualizáveis.
- **Resultado de Atualização**: representação do estado final persistido do Evento mais metadados de sucesso ou falha.
- **Registro de Auditoria de Atualização**: evidência operacional de cada tentativa de PATCH com identificação, resultado e motivo em caso de rejeição.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em ambiente de homologação, 100% dos PATCH válidos em eventos existentes retornam dados coerentes com o estado persistido após consulta de confirmação.
- **SC-002**: 100% dos PATCH inválidos por validação ou regra de negócio são rejeitados sem alteração de dados previamente persistidos.
- **SC-003**: Pelo menos 95% das atualizações parciais válidas são concluídas em até 2 segundos sob carga operacional normal da aplicação.
- **SC-004**: 100% das tentativas de PATCH (sucesso e falha) geram registro auditável pesquisável pela equipe de operação.

## Measurement and Evidence Plan *(mandatory)*

- Evidência de SC-001: suíte de cenários de aceitação com comparação entre resposta PATCH e leitura subsequente do mesmo evento.
- Evidência de SC-002: cenários negativos automatizados validando ausência de mudança de estado após rejeição.
- Evidência de SC-003: relatório periódico de tempo de conclusão do fluxo PATCH em ambiente de homologação, com amostragem contínua.
- Evidência de SC-004: verificação de logs/auditoria com amostra de operações de sucesso e falha contendo identificador correlacionável.
- Cadência de baseline: coleta semanal de métricas operacionais de PATCH e revisão em ritos de acompanhamento.
- Evidência esperada em pull request: cenários de teste do fluxo principal e dos principais erros, mais prova de rastreabilidade operacional.
