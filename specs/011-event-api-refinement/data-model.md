# Modelo de Dados: Refinamento da API de Eventos

Este documento descreve as entidades e estruturas de dados impactadas por esta funcionalidade.

## Entidades Principais

### Evento (`EventoEntity`)

Representa um registro no calendário paroquial.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | UUID | Identificador único do evento. |
| `titulo` | String | Título sumário da atividade. |
| `descricao` | String | Detalhamento opcional. |
| `inicioUtc` | Instant | Data/hora de início em UTC. |
| `fimUtc` | Instant | Data/hora de término em UTC. |
| `status` | Enum | Estado atual: `AGENDADO`, `CANCELADO`, `CONCLUIDO`. |
| `organizacaoResponsavelId` | Long | ID da organização dona do evento (usado para RBAC). |

### Auditoria de Operação (`AuditoriaOperacaoEntity`)

Registra todas as ações significativas no domínio de eventos.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | UUID | Identificador único do log. |
| `acao` | String | Tipo da operação: `CREATE`, `PATCH`, `CANCEL`, `READ`, `LIST`. |
| `resultado` | String | Resultado: `SUCESSO`, `FALHA`, `NEGADO`. |
| `atorUsuarioId` | String | Identificação do usuário logado. |
| `detalhesAuditaveisJson` | JSONB | Metadados da operação (ex: ID do evento consultado). |
| `dataHoraUtc` | Instant | Timestamp da ocorrência. |

## Estruturas de Transferência (DTOs)

### EventoResponse

Objeto retornado em consultas detalhadas (`GET /api/v1/eventos/{id}`) e listagens.

- `id`: UUID
- `titulo`: String
- `descricao`: String
- `inicio`: Instant
- `fim`: Instant
- `status`: String
- `organizacaoId`: Long

### CancelarEventoRequest

Corpo da requisição para o novo endpoint de cancelamento.

- `motivo`: String (Obrigatório)
