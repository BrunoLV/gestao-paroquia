# Feature Specification: Ajuste de Autenticacao e Permissionamento

**Feature Branch**: `005-spring-security-rbac`  
**Created**: 2026-03-15  
**Status**: Ready-for-Implementation  
**Input**: User description: "ajustar a implementação de autenticação e permissionamento da api para usar o padrão do spring security baseado basea e usuarios e papeis; ajustar para usar a estrutura baseada em banco dados baseado no que é especificado na documentação; ajustar o permissionamento nos endpoints"

## Clarifications

### Session 2026-03-15

- Q: Qual estratégia de autenticação a API deve adotar para validar o usuário antes do RBAC? → A: Autenticação por usuário e senha; a API valida credenciais diretamente e aplica RBAC com base no banco.
- Q: Quais endpoints devem permanecer públicos (acesso anônimo) após o ajuste de segurança? → A: Nenhum endpoint de negócio público; apenas o endpoint de controle de sessão de login (`POST /api/v1/auth/login`) é público.
- Q: Para decisão de autorização, qual deve ser a fonte oficial dos papéis/permissões do usuário? → A: Banco como fonte oficial; credenciais apenas autenticam, papéis e vínculos são derivados do banco.
- Q: Quando houver indisponibilidade temporária do banco (fonte oficial de papéis/vínculos), qual comportamento de autorização a API deve adotar? → A: Fail-closed; negar requisições protegidas com erro temporário de autorização.
- Q: Com autenticação por usuário e senha, como a sessão deve ser gerenciada nas requisições subsequentes? → A: Sessão stateful com cookie — web app faz login uma vez, Spring mantém sessão e envia cookie nas demais chamadas.
- Q: Quando a sessão expirar durante chamadas de API, qual resposta deve ser retornada? → A: Responder HTTP 401 com código `SESSION_EXPIRED` em payload JSON, sem redirecionamento HTTP.

### Session 2026-03-21

- Q: Como deve ser propagado e gerado o `correlationId` nos logs estruturados de segurança? → A: UUID por requisição via MDC (`correlationId`), gerado no servidor se ausente no header `X-Correlation-ID`.
- Q: Qual é o escopo de cobertura de lifecycle transitions inválidas nesta feature? → A: Smoke test de regressão — um cenário negativo por transição de status crítica já existente no domínio (ex.: cancelar evento já cancelado → 422), validado como contrato existente sem criar novo escopo de regras de negócio.
- Q: Qual deve ser o alvo mensurável de latência para chamadas autenticadas nesta API? → A: p95 ≤ 500ms para endpoints de leitura; sem alvo novo para mutações.
- Q: Como resolver sobreposição entre FR-001 e FR-002 (ambos declaram que apenas login é público)? → A: Manter os dois FRs separados; FR-002 declara explicitamente que tem FR-001 como pré-condição.
- Q: Qual deve ser o status da spec após as clarificações desta sessão? → A: Ready-for-Implementation.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Bloquear acesso indevido por perfil (Priority: P1)

Como gestor da API, quero que toda chamada protegida valide o usuário autenticado e seus papéis no banco compartilhado para impedir mutações indevidas no calendário.

**Why this priority**: Segurança de escrita é o risco mais crítico; falhas aqui afetam integridade e governança dos dados.

**Independent Test**: Pode ser testada isoladamente executando operações de escrita com perfis autorizados e não autorizados, confirmando respostas e trilha de auditoria.

**Acceptance Scenarios**:

1. **Given** um usuário autenticado com papel válido para a organização responsável do evento, **When** envia uma mutação de evento, **Then** a operação é autorizada e concluída.
2. **Given** um usuário autenticado sem papel compatível com o escopo do evento, **When** envia uma mutação de evento, **Then** a operação é bloqueada com erro de autorização.
3. **Given** uma requisição sem credenciais para endpoint interno de mutação, **When** a chamada é processada, **Then** a API retorna erro de autenticação.

---

### User Story 2 - Aplicar matriz de permissões por endpoint (Priority: P2)

Como mantenedor do produto, quero que cada endpoint respeite uma matriz de acesso consistente (autenticado e papel por escopo) para evitar comportamentos divergentes entre recursos.

**Why this priority**: Permissões inconsistentes entre endpoints geram brechas de segurança e regressões funcionais difíceis de detectar.

**Independent Test**: Pode ser testada mapeando endpoint por endpoint e validando status de resposta para não autenticado, autenticado sem papel e autenticado com papel válido.

**Acceptance Scenarios**:

1. **Given** qualquer endpoint de negócio da API (exceto `POST /api/v1/auth/login`), **When** um usuário não autenticado realiza chamada, **Then** a API bloqueia a requisição por falta de autenticação.
2. **Given** um endpoint interno de leitura, **When** um usuário autenticado sem papel compatível realiza consulta, **Then** a API retorna acesso negado.
3. **Given** um endpoint de mutação, **When** um usuário autenticado com papel fora do escopo organizacional tenta alterar dados, **Then** a API retorna acesso negado.

---

### User Story 3 - Manter compatibilidade com o modelo documental de dados (Priority: P3)

Como responsável por compliance funcional, quero que autenticação e autorização consumam exclusivamente as estruturas de usuários, organizações e vínculos já documentadas, sem criar responsabilidades indevidas nesta API.

**Why this priority**: Preserva o contrato entre sistemas e evita acoplamento indevido com dados mestres gerenciados por outras aplicações.

**Independent Test**: Pode ser testada verificando que as decisões de acesso usam dados de referência documentados e que não há operações de escrita nas tabelas externas.

**Acceptance Scenarios**:

1. **Given** um usuário autenticado com vínculo organizacional válido no banco compartilhado, **When** a autorização é avaliada, **Then** a decisão considera usuário, organização e papel conforme estrutura documentada.
2. **Given** uma operação da API que exige autorização, **When** o processamento consulta dados externos de identidade e vínculo, **Then** o acesso às tabelas externas ocorre somente em modo leitura.

### Edge Cases

- Usuário autenticado sem vínculo organizacional ativo deve ser tratado como sem permissão para operações internas.
- Usuário com múltiplos papéis em organizações diferentes deve ser autorizado apenas no escopo da organização alvo da operação.
- Papel exclusivo de tipo organizacional restrito (ex.: secretário fora de conselho) deve ser rejeitado explicitamente.
- Endpoint interno recém-criado sem regra explícita na matriz de acesso não pode ficar aberto por padrão.
- Credenciais válidas para usuário inativo ou inexistente no banco devem retornar erro de autenticação.
- Indisponibilidade temporária da fonte de autorização em banco deve bloquear endpoints protegidos em modo fail-closed.
- Sessão expirada ou invalidada deve resultar em HTTP 401 com `SESSION_EXPIRED` em JSON, sem acesso residual a endpoints protegidos.
- Requisição sem cookie de sessão válido deve ser bloqueada com erro de autenticação (equivalente a não autenticado).
- Transições de ciclo de vida inválidas de eventos (ex.: cancelar evento já cancelado) devem continuar retornando o erro de domínio existente (4xx); a camada de segurança não altera esse comportamento — um smoke test de regressão deve confirmar que esses cenários não foram afetados pela mudança de autenticação.

## API Contract & Validation *(mandatory)*

- Endpoints afetados:
  - `GET /eventos`: contrato funcional **inalterado**; acesso passa a exigir autenticação.
  - Endpoints internos de leitura de detalhe e histórico de eventos: contrato funcional **inalterado**, regra de autenticação/autorização **endurecida e padronizada**.
  - Endpoints de criação, alteração, cancelamento, aprovação, vínculo de projeto e observações: contrato funcional **inalterado**, validação de permissão por papel/escopo **padronizada**.
- Estratégia de autenticação e gerenciamento de sessão: a API autentica o usuário por credenciais de usuário e senha; após login bem-sucedido, o Spring Security mantém sessão stateful server-side e faz o transporte via cookie seguro nas requisições subsequentes.
- Formato de payload de sucesso e erro deve permanecer estável; erros de segurança devem ser padronizados com códigos de máquina:
  - `AUTH_REQUIRED` (credencial ausente)
  - `AUTH_INVALID` (credencial inválida)
  - `ACCESS_DENIED` (usuário autenticado sem permissão)
  - `ROLE_SCOPE_INVALID` (papel incompatível com organização/escopo)
  - `AUTHZ_SOURCE_UNAVAILABLE` (fonte oficial de autorização indisponível; requisição protegida negada em fail-closed)
  - `SESSION_EXPIRED` (sessão expirada ou inválida; requer novo login)
- Em sessão expirada ou inválida, a API MUST responder HTTP 401 com `SESSION_EXPIRED` em JSON, sem redirecionamento HTTP.
- Regras de validação funcional existentes de data, hora, status e campos obrigatórios permanecem vigentes; segurança não pode relaxar validações já existentes.
- Compatibilidade retroativa:
  - Sem novos campos obrigatórios em requests/responses de domínio.
  - Mudança esperada apenas em cenários antes indevidamente autorizados, que passarão a ser bloqueados.
  - Documentar matriz final de acesso por endpoint para consumidores internos.

## Calendar Integrity Rules *(mandatory for calendar/event features)*

- Estratégia canônica de timezone e normalização de data/hora permanece inalterada.
- Regras de conflito de agenda e restrições de status permanecem inalteradas; autorização apenas controla quem pode executar cada ação.
- Ordenação e critérios de recuperação de eventos não podem variar em função do perfil, exceto por regras de escopo autorizadas por papel e organização.

## Operational Observability *(mandatory)*

- Cada decisão de segurança em mutações deve registrar: `correlationId`, endpoint, operação, identificador do usuário autenticado, organização de escopo, resultado (`ALLOW`/`DENY`) e motivo de negação.
- O `correlationId` é propagado via MDC; gerado como UUID no servidor se o header de entrada `X-Correlation-ID` estiver ausente.
- Todas as negações por permissão e autenticação devem ser auditáveis com correlação de requisição usando o `correlationId` do MDC.
- Negações por indisponibilidade da fonte de autorização devem registrar motivo técnico sanitizado e código de erro estável, sem vazar detalhes internos.
- Logs de diagnóstico devem evitar exposição de segredos e conteúdo sensível de credenciais.
- Métricas operacionais mínimas:
  - taxa de negação por endpoint
  - taxa de falha de autenticação
  - volume de mutações autorizadas por tipo de operação
  - event registration lead time (tempo entre criação do evento e data/hora de início)
  - calendar query latency (latência de consultas de calendário por endpoint de leitura; **alvo: p95 ≤ 500ms**)
  - administrative rework indicator (percentual de retrabalho administrativo por alterações/reagendamentos)

## Architecture and Code Standards *(mandatory)*

- Regras de autenticação e autorização devem permanecer centralizadas na camada de entrada da API e aplicadas de forma consistente antes da lógica de domínio.
- Sessão stateful deve ser gerenciada pelo Spring Security com cookie seguro; configuração de criação, expiração, invalidação e proteção CSRF deve ser explícita e revisada.
- Decisão de negócio por escopo organizacional deve permanecer em política de domínio reutilizável e independente do transporte.
- Leitura de identidade, usuário e vínculo organizacional deve ocorrer por portas/adaptadores dedicados, respeitando separação entre domínio, aplicação e infraestrutura.
- Mudanças devem preservar clareza de naming, baixa complexidade ciclomática e mapeamento explícito de erros de segurança para respostas padronizadas.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST exigir autenticação para todos os endpoints internos de leitura e para todos os endpoints de mutação de negócio (`/api/v1/**`), exceto o endpoint de controle de sessão de login.
- **FR-002** *(Pré-condição: FR-001)*: O sistema MUST manter apenas `POST /api/v1/auth/login` como endpoint público de controle de sessão; todas as demais chamadas da API MUST exigir autenticação. *(Delimita o endpoint público explicitamente; FR-001 é a regra geral de bloqueio.)*
- **FR-002A**: O sistema MUST autenticar o usuário por credenciais de usuário e senha, validadas contra a estrutura de identidade disponível no banco de dados compartilhado.
- **FR-002B**: O sistema MUST gerenciar sessão stateful após login bem-sucedido, transportando o identificador de sessão via cookie seguro nas requisições subsequentes.
- **FR-002C**: O sistema MUST invalidar a sessão e exigir novo login após expiração ou logout explícito, sem permitir reuso de sessão encerrada.
- **FR-003**: O sistema MUST derivar permissões de acesso a partir da combinação de usuário, papel e vínculo organizacional documentados no banco compartilhado, tratando o banco como fonte oficial de autorização.
- **FR-003A**: O sistema MUST carregar papéis e vínculos organizacionais do banco após autenticação bem-sucedida e MUST NOT conceder acesso sem consultar essa fonte.
- **FR-003B**: O sistema MUST operar em fail-closed quando a fonte oficial de autorização estiver indisponível, negando toda requisição protegida com erro temporário apropriado.
- **FR-004**: O sistema MUST avaliar permissão considerando o escopo organizacional do recurso alvo da operação.
- **FR-005**: O sistema MUST negar operações de escrita quando o usuário autenticado não possuir papel compatível com a ação solicitada.
- **FR-006**: O sistema MUST negar operações quando houver inconsistência entre papel e tipo de organização conforme regras documentadas.
- **FR-007**: O sistema MUST aplicar a mesma regra de autorização para endpoints equivalentes de uma mesma capacidade funcional, evitando exceções implícitas.
- **FR-008**: O sistema MUST retornar erros de segurança com códigos de máquina estáveis e semanticamente corretos para autenticação e autorização.
- **FR-009**: O sistema MUST registrar auditoria para toda operação negada por autenticação ou autorização.
- **FR-010**: O sistema MUST manter acesso somente leitura às estruturas externas de identidade e vínculo, sem criar, editar ou remover dados nelas.
- **FR-011**: O sistema MUST preservar os contratos funcionais existentes de payload para operações de calendário, exceto pelas mudanças de comportamento de bloqueio de acesso indevido.
- **FR-012**: O sistema MUST disponibilizar matriz de permissionamento por endpoint como referência de validação para regressão.

### Key Entities *(include if feature involves data)*

- **Usuario (externo)**: Identidade autenticada usada para decisão de acesso e trilha de auditoria.
- **PapelOrganizacional (externo/derivado)**: Papel efetivo do usuário no contexto de uma organização, base para autorização.
- **MembroOrganizacao (externo)**: Vínculo entre usuário e organização que define escopo e elegibilidade de permissões.
- **RegraPermissaoEndpoint (derivada)**: Matriz declarativa que relaciona endpoint/ação a requisito de autenticação e níveis de acesso.
- **EventoParoquial**: Recurso de negócio cujo escopo organizacional determina autorização para leitura interna e mutações.

## Assumptions and Dependencies

- A estrutura das tabelas externas de identidade e vínculo já está disponível e aderente ao que foi definido na documentação do produto.
- O catálogo de papéis organizacionais vigente continua sendo a fonte de verdade para decisão de autorização.
- A autenticação do usuário ocorre via credenciais de usuário e senha validadas por esta API contra o banco de dados compartilhado; sessão stateful é mantida após login.
- O banco compartilhado é a fonte oficial para identidade, papéis e vínculos organizacionais.
- Não faz parte deste escopo redesenhar regras de negócio de calendário; o foco é padronizar autenticação e permissionamento.
- Consumidores internos aceitarão atualização da documentação de matriz de acesso quando houver bloqueio de casos antes permissivos.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das requisições sem autenticação para endpoints de negócio da API (exceto `POST /api/v1/auth/login`) são bloqueadas com erro de autenticação apropriado.
- **SC-002**: 100% das tentativas de mutação por usuários autenticados sem permissão são bloqueadas com erro de autorização apropriado.
- **SC-003**: 100% dos endpoints mapeados na matriz de acesso possuem resultado consistente em testes de regressão (não autenticado, autenticado sem papel, autenticado com papel válido).
- **SC-004**: Incidentes de autorização inconsistente entre endpoints equivalentes são reduzidos em pelo menos 80% no ciclo seguinte de validação operacional.
- **SC-005**: 100% das requisições protegidas durante indisponibilidade da fonte oficial de autorização são negadas em fail-closed com código de erro padronizado.
- **SC-006**: Latência p95 ≤ 500ms para endpoints de leitura autenticados (baseline coletado após ativação do filtro de segurança).

## Measurement and Evidence Plan *(mandatory)*

- Para **SC-001**: executar suíte de integração de segurança por endpoint e consolidar percentual de bloqueios corretos de chamadas não autenticadas.
- Para **SC-002**: executar suíte de autorização por papel/escopo com cenários negativos e validar taxa de bloqueio em 100%.
- Para **SC-003**: manter checklist de cobertura da matriz de acesso por endpoint e anexar evidência de testes no pull request.
- Para **SC-004**: comparar baseline de inconsistências de autorização do ciclo anterior com ciclo atual em revisão semanal de operação.
- Para **SC-005**: executar teste de resiliência simulando indisponibilidade da fonte oficial de autorização e comprovar bloqueio integral de requisições protegidas.
- Para disciplina operacional constitucional: instrumentar e coletar baseline de `event registration lead time`, `calendar query latency` (alvo p95 ≤ 500ms) e `administrative rework indicator` com publicação periódica de tendência.
- Para **SC-006**: coletar percentil p95 de latência de leitura antes e após ativação do filtro de segurança e confirmar valor dentro do alvo.
- Cadência operacional obrigatória para **SC-004**: publicar snapshot semanal com baseline e delta de inconsistências de autorização por endpoint.
- Evidências mínimas de aceite no pull request:
  - atualização da matriz de acesso por endpoint
  - resultados de testes de segurança direcionados
  - evidência de coleta dos três indicadores constitucionais mínimos de runtime
  - nota de impacto de métricas operacionais para fluxos críticos alterados
  - amostra de logs/auditoria para cenários `ALLOW` e `DENY`
