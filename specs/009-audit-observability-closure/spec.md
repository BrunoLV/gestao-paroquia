# Feature Specification: Fechamento de Auditoria e Retrabalho

**Feature Branch**: `[009-audit-observability-closure]`  
**Created**: 2026-04-18  
**Status**: Draft  
**Input**: User description: "O projeto já atende a maior parte da constituição no que diz respeito a contratos de API, regras de domínio, cobertura automatizada, autenticação com RBAC, rastreabilidade básica e instrumentação de métricas operacionais. No entanto, ainda existem lacunas para conformidade plena com os requisitos constitucionais de auditoria e observabilidade operacional."

## Clarifications

### Session 2026-04-18

- Q: Como o indicador de retrabalho administrativo deve ser exposto ao cliente? → A: Como taxa de retrabalho, calculada pelas ocorrências elegíveis divididas pelo total de eventos afetados no período.
- Q: Como a consulta da trilha auditável deve tratar `organizacaoId`? → A: `organizacaoId` é obrigatório em toda consulta da trilha auditável.
- Q: O que deve acontecer quando a persistência auditável obrigatória falhar? → A: A mutação falha e nada é confirmado se a gravação auditável obrigatória falhar.
- Q: Como o período deve ser informado nos novos endpoints operacionais? → A: Aceitar granularidade predefinida ou intervalo explícito `inicio/fim`, mas nunca ambos na mesma requisição.
- Q: O endpoint de retrabalho deve exigir `organizacaoId`? → A: `organizacaoId` é obrigatório também no endpoint de retrabalho.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar trilha auditável (Priority: P1)

Como gestor administrativo da paróquia, quero consultar a trilha auditável das mutações de eventos e observações por período e organização para investigar decisões operacionais, validar responsabilidades e responder a incidentes sem depender de leitura manual de logs.

**Why this priority**: Este é o maior gap constitucional identificado. Sem consulta auditável por API, a rastreabilidade operacional existe apenas de forma parcial e não atende a governança exigida pelo projeto.

**Independent Test**: Pode ser testada integralmente ao registrar mutações em eventos e observações e, em seguida, consultar a trilha por período e organização, validando presença, ordenação e imutabilidade dos registros retornados.

**Acceptance Scenarios**:

1. **Given** que houve mutações de criação, edição e exclusão lógica em uma organização durante um período conhecido, **When** um usuário autorizado consulta a trilha auditável com período e `organizacaoId` explícitos, **Then** o sistema retorna apenas os registros correspondentes, com ator, ação, alvo, resultado, correlation id e marca temporal.
2. **Given** que um registro auditável já foi gravado, **When** novas mutações do mesmo recurso ocorrem posteriormente, **Then** o registro anterior permanece inalterado e a consulta retorna ambos os eventos de forma determinística.
3. **Given** que o filtro informado não possui ocorrências, **When** a trilha auditável é consultada, **Then** o sistema retorna resposta vazia consistente, sem erro e sem expor dados de outras organizações.

---

### User Story 2 - Consultar indicador de retrabalho (Priority: P2)

Como responsável pelo acompanhamento operacional do calendário, quero consultar a taxa de retrabalho administrativo por período e organização para medir, de forma comparável entre janelas diferentes e sem agregação fora do meu escopo, o impacto de reagendamentos, cancelamentos e trocas de organização responsável, substituindo o valor placeholder atual por um resultado confiável.

**Why this priority**: O indicador já é exigido pela constituição e já possui instrumentação parcial, mas ainda não produz valor operacional real porque o cálculo consolidado não existe.

**Independent Test**: Pode ser testada ao provocar ocorrências que componham retrabalho administrativo, solicitar o indicador por período e verificar que o valor retornado corresponde às ocorrências registradas para o intervalo consultado.

**Acceptance Scenarios**:

1. **Given** que ocorreram cancelamentos, reagendamentos e trocas de organização responsável em uma organização durante um período, **When** o indicador de retrabalho é consultado com período e `organizacaoId`, **Then** o sistema retorna uma taxa calculada a partir das ocorrências elegíveis divididas pelo total de eventos afetados naquela organização e identifica a janela consultada.
2. **Given** que não houve ocorrências de retrabalho na organização e no período informados, **When** o indicador é consultado, **Then** o sistema retorna valor zero de forma explícita e consistente.

---

### User Story 3 - Preservar baseline e evidências operacionais (Priority: P3)

Como revisor de mudanças críticas do calendário, quero receber evidências verificáveis de impacto operacional nas mudanças de auditoria e métricas para confirmar aderência à constituição antes da liberação.

**Why this priority**: A feature só fecha o requisito constitucional se a coleta e a comprovação das métricas forem repetíveis e auditáveis em revisões futuras.

**Independent Test**: Pode ser testada executando o snapshot semanal, consultando os endpoints novos e verificando que a mudança gera evidências reproduzíveis para critérios de sucesso e revisão de PR.

**Acceptance Scenarios**:

1. **Given** que a solução de auditoria e métricas está ativa, **When** o snapshot operacional periódico é executado, **Then** a baseline preserva histórico suficiente para comparação de tendências.
2. **Given** uma alteração em fluxo crítico de mutação, **When** a evidência de validação é produzida, **Then** ela inclui resultado de testes, impacto contratual e impacto nas métricas constitucionais.

### Edge Cases

- Consulta de trilha auditável sem `organizacaoId` deve ser rejeitada com erro determinístico de validação, sem inferir escopo implicitamente.
- Consulta com `organizacaoId` fora do escopo do usuário autenticado deve resultar em erro determinístico de acesso, sem revelar existência de registros.
- Requisição que informe simultaneamente granularidade predefinida e intervalo explícito `inicio/fim` deve ser rejeitada com erro determinístico de validação.
- Consulta do indicador de retrabalho sem `organizacaoId` deve ser rejeitada com erro determinístico de validação, sem agregar dados de múltiplas organizações implicitamente.
- Eventos auditáveis gravados no mesmo instante devem possuir ordenação estável e reprodutível.
- Falha temporária na persistência da trilha auditável deve fazer a mutação falhar integralmente, sem confirmação parcial do estado de negócio.
- O cálculo do indicador de retrabalho deve evitar dupla contagem quando uma mesma operação gerar múltiplos efeitos técnicos para uma única ocorrência administrativa.
- Períodos inválidos ou não suportados devem retornar erro determinístico e orientação diagnóstica sem expor detalhes internos.

## API Contract & Validation *(mandatory)*

- Endpoints afetados:
- `GET /api/v1/auditoria/eventos/extras`: contrato existente, mantido, com comportamento inalterado.
- `GET /api/v1/auditoria/eventos/trilha`: novo endpoint para consulta auditável por período e organização.
- `GET /api/v1/auditoria/eventos/retrabalho`: novo endpoint para taxa de retrabalho administrativo por período e organização.
- Endpoints de mutação de eventos, aprovações e observações: contratos de request/response mantidos; a mudança é de persistência auditável obrigatória e evidência operacional.
- `GET /api/v1/auditoria/eventos/trilha` MUST exigir definição explícita de período via granularidade predefinida ou intervalo `inicio/fim`, além de `organizacaoId`, podendo aceitar adicionalmente ator, ação, resultado e correlation id. A resposta MUST retornar coleção ordenada de registros auditáveis com identificador estável, marca temporal, ator, ação, alvo, organização, resultado e metadados auditáveis permitidos.
- `GET /api/v1/auditoria/eventos/retrabalho` MUST exigir `organizacaoId` e aceitar definição explícita de período via granularidade predefinida ou intervalo `inicio/fim`, mas nunca ambos na mesma requisição, e retornar o período consultado, a taxa consolidada de retrabalho administrativo e metadados suficientes para interpretação operacional do resultado, incluindo numerador e denominador.
- Erros machine-readable MUST reutilizar o catálogo determinístico existente sempre que possível e ampliar o catálogo apenas para cenários novos de filtro inválido, consulta fora do escopo e indisponibilidade da fonte auditável.
- Regras de validação MUST cobrir definição obrigatória de período, exclusão mútua entre granularidade predefinida e intervalo `inicio/fim`, `organizacaoId` obrigatório na trilha auditável e no indicador de retrabalho, enumeração permitida de granularidade temporal, consistência entre filtros e escopo organizacional autorizado.
- Impacto de compatibilidade: aditivo para clientes existentes nos endpoints novos, sem quebra de contratos atuais de mutação. Caso o catálogo de erros seja ampliado para falhas de persistência auditável obrigatória, notas de migração MUST documentar os novos códigos e seus cenários.

## Calendar Integrity Rules *(mandatory for calendar/event features)*

- A estratégia canônica de timezone permanece inalterada: datas e horas operacionais devem continuar normalizadas com persistência em UTC e apresentação conforme a zona de saída canônica já definida pelo projeto.
- O recurso não altera regras de conflito do calendário nem transições de lifecycle dos eventos; ele apenas torna auditáveis as mutações já autorizadas pelo domínio.
- Registros auditáveis MUST refletir o instante real da mutação em timestamp canônico e preservar correlação com o recurso de calendário afetado.
- Consultas da trilha MUST usar ordenação determinística. Quando mais de um registro compartilhar o mesmo timestamp, um identificador estável adicional MUST garantir desempate reprodutível.

## Operational Observability *(mandatory)*

- Toda mutação relevante de eventos, aprovações e observações MUST gerar registro auditável persistido com ator, ação, alvo, resultado, correlation id, organização relacionada e marca temporal.
- O recurso MUST distinguir pelo menos os outcomes `SUCCESS`, `FAILURE`, `DENY`, `PENDING`, `APPROVED`, `REJECTED` e `EXECUTED` quando aplicáveis ao fluxo.
- A trilha auditável MUST ser consultável sem depender de leitura de logs de infraestrutura.
- O indicador de retrabalho MUST ser derivado de ocorrências auditáveis ou de outra fonte operacional consolidada equivalente, com regra de cálculo declarada na documentação da feature como taxa: ocorrências elegíveis divididas pelo total de eventos afetados no período.
- Respostas de erro MUST incluir código determinístico, mensagem diagnóstica segura e correlation id, sem expor stack trace, estrutura interna de persistência ou dados de organizações fora do escopo autorizado.
- Quando a persistência auditável obrigatória falhar, o sistema MUST adotar comportamento fail-closed para a mutação correspondente.
- A baseline operacional MUST continuar com snapshot recorrente para comparação histórica de métricas críticas.

## Architecture and Code Standards *(mandatory)*

- Regras de cálculo do indicador e políticas de auditabilidade pertencem ao domínio e à aplicação; persistência de trilha auditável, mapeamentos HTTP e agendamento de snapshots pertencem à infraestrutura e à borda de API.
- A feature MUST preservar a separação entre controladores REST, casos de uso, serviços de domínio e adaptadores de persistência/observabilidade.
- Portas de leitura e escrita da trilha auditável SHOULD isolar a aplicação da tecnologia concreta de armazenamento, permitindo evolução sem acoplar a regra de negócio ao mecanismo de persistência.
- Componentes alterados MUST manter complexidade controlada, nomes explícitos, tratamento determinístico de erros e ausência de lógica de negócio nos controllers.
- Em Java e Spring Boot, a implementação MUST usar injeção de dependências, validação declarativa de inputs, fronteiras transacionais explícitas para gravação auditável e mapeamento centralizado de exceções.

## Assumptions

- O escopo inicial da consulta auditável cobre mutações de eventos, aprovações e observações já existentes no sistema.
- O período consultado pode ser representado pelas granularidades operacionais já usadas pelo projeto, como diário, semanal, mensal e anual, ou por intervalo explícito `inicio/fim`, desde que os dois modos não sejam usados simultaneamente.
- O indicador de retrabalho administrativo será calculado como taxa sobre ocorrências de cancelamento, reagendamento e troca de organização responsável, divididas pelo total de eventos afetados no período, salvo refinamento posterior documentado no plano.
- O acesso aos endpoints de auditoria seguirá o modelo de autenticação e RBAC já vigente, sem criar um modelo paralelo de autorização, com `organizacaoId` explícito para as leituras operacionais.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST persistir um registro auditável imutável para cada operação mutável de evento, aprovação e observação coberta pelo domínio atual.
- **FR-002**: Cada registro auditável MUST conter, no mínimo, identificador estável, timestamp canônico, ator, organização relacionada quando aplicável, ação, alvo, resultado e correlation id.
- **FR-003**: O sistema MUST disponibilizar consulta da trilha auditável por período e `organizacaoId` obrigatórios, com ordenação determinística e sem depender de leitura de logs de aplicação.
- **FR-004**: O sistema MUST aplicar controle de acesso à consulta auditável conforme o escopo organizacional do usuário autenticado.
- **FR-005**: O sistema MUST rejeitar ausência de `organizacaoId` na trilha auditável, definição ambígua de período, filtros inválidos ou incompatíveis com erro determinístico e mensagem diagnóstica segura.
- **FR-006**: O sistema MUST garantir que falhas na gravação obrigatória da trilha auditável não sejam mascaradas como sucesso operacional e impeçam a confirmação da mutação correspondente.
- **FR-007**: O sistema MUST calcular a taxa de retrabalho administrativo por período a partir de ocorrências operacionais definidas e documentadas pela feature, divididas pelo total de eventos afetados no período.
- **FR-008**: O sistema MUST disponibilizar endpoint para consulta da taxa de retrabalho por período e `organizacaoId` obrigatórios, com resposta estável e interpretável por clientes, incluindo numerador e denominador.
- **FR-009**: O cálculo do indicador de retrabalho MUST evitar dupla contagem da mesma ocorrência administrativa.
- **FR-010**: O sistema MUST manter o snapshot periódico das métricas constitucionais com histórico suficiente para análise de tendência.
- **FR-011**: A introdução dos novos endpoints e persistências MUST preservar compatibilidade com os contratos existentes de mutação e consulta já publicados e MUST ser validada por regressões automatizadas explícitas nos fluxos existentes impactados, incluindo visibilidade pública por status e rejeição de transições inválidas de lifecycle.
- **FR-012**: Pull requests que alterem esses fluxos MUST incluir evidências de impacto contratual, impacto arquitetural e impacto nas métricas constitucionais.

### Key Entities *(include if feature involves data)*

- **RegistroAuditavel**: Representa um evento operacional imutável associado a uma mutação de domínio, contendo identidade do registro, momento da ocorrência, ator, organização, ação, alvo, resultado, correlation id e metadados auditáveis permitidos.
- **FiltroConsultaAuditoria**: Representa o conjunto de critérios aceitos para consulta da trilha, incluindo período via granularidade ou `inicio/fim`, `organizacaoId` obrigatório, ator, ação, resultado e correlation id.
- **FiltroPeriodoOperacional**: Representa a janela temporal usada nos endpoints operacionais, definida por granularidade predefinida ou intervalo explícito, com exclusão mútua entre os modos.
- **IndicadorRetrabalho**: Representa a taxa consolidada do retrabalho administrativo para um período, incluindo a janela consultada, o resultado calculado, o numerador de ocorrências elegíveis e o denominador de eventos afetados.
- **OcorrenciaRetrabalho**: Representa uma ocorrência administrativa elegível para compor o indicador, como cancelamento, reagendamento ou troca de organização responsável.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das mutações cobertas por eventos, aprovações e observações geram registro consultável na trilha auditável durante a validação da feature.
- **SC-002**: 100% das consultas autorizadas à trilha retornam somente registros do período e do escopo organizacional solicitados nos cenários validados.
- **SC-003**: 100% dos cenários de consulta do indicador de retrabalho retornam taxa, numerador e denominador consistentes com as ocorrências operacionais preparadas para o período de teste.
- **SC-004**: 95% das consultas dos novos endpoints de auditoria e retrabalho retornam resultado em até 2 segundos no ambiente de validação da feature.
- **SC-005**: 100% dos cenários de erro relevantes para filtros inválidos, acesso fora do escopo e indisponibilidade da persistência auditável retornam código determinístico, correlation id e ausência de confirmação parcial da mutação.

## Measurement and Evidence Plan *(mandatory)*

- **SC-001**: Medir por testes de integração e contrato que executem mutações reais e consultem a trilha auditável resultante, comparando a quantidade esperada de registros com a quantidade retornada.
- **SC-002**: Medir por cenários de integração com múltiplas organizações e filtros temporais distintos, validando ausência de vazamento entre escopos.
- **SC-003**: Medir por testes determinísticos do cálculo da taxa e por cenários integrados que criem ocorrências elegíveis, estabeleçam o total de eventos afetados e consultem o endpoint consolidado.
- **SC-004**: Medir com testes de performance Tier 1 ou equivalente sobre os novos endpoints de leitura operacional.
- **SC-005**: Medir por suíte de contrato e integração focada em erros determinísticos, incluindo validação explícita de `errorCode`, `correlationId` e rollback integral quando a persistência auditável obrigatória estiver indisponível.
- Regressões mandatórias de compatibilidade MUST incluir cobertura explícita de visibilidade pública por status, rejeição de transições inválidas de lifecycle e preservação dos contratos existentes impactados pela feature.
- A baseline operacional MUST continuar em cadência semanal, preservando histórico de snapshots para comparação de tendência das métricas constitucionais.
- A validação da baseline MUST demonstrar retenção histórica suficiente para comparar, no mínimo, snapshots consecutivos ou evidência equivalente de tendência preservada durante a validação da feature.
- Pull requests MUST anexar evidências mínimas de: testes executados, impacto no contrato da API, impacto na arquitetura em camadas/portas-adaptadores e impacto nas métricas constitucionais `event_registration_lead_time_minutes`, `calendar_query_latency_ms` e `administrative_rework_indicator`.
