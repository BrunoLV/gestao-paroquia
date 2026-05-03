# Plano de Implementação: Refinamento da API de Eventos

**Branch**: `011-event-api-refinement` | **Data**: 02/05/2026 | **Spec**: [spec.md](spec.md)
**Entrada**: Especificação da funcionalidade de `/specs/011-event-api-refinement/spec.md`

## Resumo

Este plano detalha a evolução do `EventoController` para suportar visualização detalhada de eventos, listagem otimizada com paginação/filtros temporais e uma transição segura para um novo mecanismo de cancelamento resiliente a middlewares de rede. A abordagem técnica foca em auditoria de leitura, métricas de latência e manutenção de compatibilidade retroativa.

## Contexto Técnico

**Linguagem/Versão**: Java 17+ (Spring Boot 3.x)  
**Dependências Primárias**: Spring Web, Spring Data JPA, Spring Security, Micrometer, Spring AOP  
**Armazenamento**: PostgreSQL (conforme baseline do projeto)  
**Testing**: JUnit 5, MockMvc, AssertJ  
**Plataforma Alvo**: Linux Server / JVM  
**Tipo de Projeto**: Web Service / REST API  
**Metas de Performance**: Recuperação de detalhes < 500ms (p95); Redução de 50% no payload de listagem.  
**Restrições**: Auditoria obrigatória de leitura; RBAC por organização; Paginação obrigatória na listagem.  
**Escopo**: Endpoints de Eventos (`/api/v1/eventos`).

## Constitution Check

*GATE: Deve passar antes da pesquisa da Fase 0. Re-verificar após o design da Fase 1.*

- **Contrato de Domínio API-First**: [PASS] Novos contratos definidos na especificação.
- **Integridade do Calendário Litúrgico**: [PASS] Filtros de data respeitam semântica temporal.
- **Testabilidade Inegociável**: [PASS] Cenários de aceitação mapeados para histórias de usuário.
- **Operações Rastreáveis e Auditoria**: [PASS] Requisito explícito de auditoria para leitura de eventos (FR-003).
- **Arquitetura Limpa e Hexagonal**: [PASS] Lógica de negócio será isolada em Services, adaptadores no Controller/Repository.
- **Métricas Operacionais e Disciplina SLO**: [PASS] Captura de latência exigida (FR-004).

## Estrutura do Projeto

### Documentação (desta funcionalidade)

```text
specs/011-event-api-refinement/
├── plan.md              # Este arquivo
├── research.md          # Saída da Fase 0
├── data-model.md        # Saída da Fase 1
├── quickstart.md        # Saída da Fase 1
├── contracts/           # Saída da Fase 1 (OpenAPI/Swagger)
└── tasks.md             # Saída da Fase 2 (/speckit.tasks)
```

### Código Fonte

```text
app/src/main/java/br/org/paroquia/calendario/
├── api/                     # Adaptadores de Transporte (REST)
│   ├── v1/
│   │   ├── controller/      # EventoController (Refinamento)
│   │   ├── dto/             # Request/Response DTOs
│   │   └── mapper/          # Mappers para Entidades
├── domain/                  # Núcleo (Domain/Application)
│   ├── model/               # Evento, Auditoria
│   ├── service/             # EventoService (Lógica de filtragem, auditoria)
│   └── repository/          # EventoRepository (Queries paginadas)
├── infra/                   # Adaptadores de Infraestrutura
│   ├── security/            # RBAC por Organização
│   ├── audit/               # Implementação da trilha de auditoria
│   └── metrics/             # Configuração de latência (Micrometer)

app/src/test/java/br/org/paroquia/calendario/
├── api/v1/controller/       # Testes de Integração (MockMvc)
├── domain/service/          # Testes de Unidade
└── architecture/            # Testes ArchUnit (opcional)
```

**Decisão de Estrutura**: Padrão de pacotes existente no projeto, reforçando a separação entre API (Controller/DTO) e Domain (Service/Model).

## Controle de Complexidade

> **Preencher APENAS se o Constitution Check tiver violações que devem ser justificadas**

*Nenhuma violação detectada.*
