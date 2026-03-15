# Compatibility Notice: Enum and DTO Contract Hardening

## Scope
- Endpoints affected:
  - `POST /api/v1/projetos`
  - `PATCH /api/v1/projetos/{projetoId}`
  - `POST /api/v1/eventos/{eventoId}/observacoes`
  - `GET /api/v1/eventos/{eventoId}/observacoes`
  - `PUT /api/v1/eventos/{eventoId}/participantes`
  - `DELETE /api/v1/eventos/{eventoId}/participantes`
  - `PUT /api/v1/eventos/{eventoId}/recorrencia`
  - `POST /api/v1/aprovacoes`

## What Changed
- Dynamic `Map` payloads were replaced with explicit request and response DTOs.
- Categorical request fields now accept canonical enum values only, with `trim` + case-insensitive normalization.
- Undocumented extra request fields are rejected with deterministic validation code `VALIDATION_UNKNOWN_FIELD`.
- Legacy invalid stored categorical values remain readable through response-only sentinel `UNKNOWN_LEGACY`.

## Migration Mapping Examples
- `POST /api/v1/projetos`
  - Before: `{ "nome": "Semana Santa", "descricao": "...", "qualquerCampo": "..." }`
  - After: `{ "nome": "Semana Santa", "descricao": "..." }`
- `POST /api/v1/eventos/{eventoId}/observacoes`
  - Before: `{ "usuarioId": "...", "tipo": "NOTA", "conteudo": "...", "metadata": {...} }`
  - After: `{ "usuarioId": "...", "tipo": "NOTA", "conteudo": "..." }`
- `PUT /api/v1/eventos/{eventoId}/recorrencia`
  - Before: `{ "frequencia": "SEMANAL", "intervalo": 2, "extra": true }`
  - After: `{ "frequencia": "SEMANAL", "intervalo": 2 }`
- `POST /api/v1/aprovacoes`
  - Before: `{ "eventoId": "...", "tipoSolicitacao": "ALTERACAO_HORARIO", "observacaoInterna": "..." }`
  - After: `{ "eventoId": "...", "tipoSolicitacao": "ALTERACAO_HORARIO" }`

## Enum Catalogs
- Observation input: `NOTA`, `JUSTIFICATIVA`, `APROVACAO`, `REPROVACAO`, `CANCELAMENTO`, `AJUSTE_HORARIO`
- Recurrence input: `DIARIA`, `SEMANAL`, `MENSAL`
- Approval input: `ALTERACAO_HORARIO`, `CANCELAMENTO`, `RECLASSIFICACAO`, `OUTRO`
- Response-only sentinel for legacy invalid stored values: `UNKNOWN_LEGACY`

## Client Action Required
- Stop sending undocumented keys in changed request payloads.
- Treat enum request fields as canonical catalogs, not open text.
- Do not send `UNKNOWN_LEGACY` in any request.
- Update client-side schema validation to align with the explicit DTOs documented in the OpenAPI delta.

## Validation Behavior
- Unknown field: `VALIDATION_UNKNOWN_FIELD`
- Unsupported enum value: `VALIDATION_ENUM_VALUE_INVALID`
- Missing required field: `VALIDATION_REQUIRED_FIELD`

## Evidence Summary
- Shared validation foundation implemented and validated.
- User Story 1 focused Gradle suite passed.
- User Story 2 focused Gradle suite passed.
- User Story 3 focused Gradle suite passed.
