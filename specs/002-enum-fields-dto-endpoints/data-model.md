# Data Model: Enum Mapping and Endpoint DTO Contracts

## 1. CampoCategorico
- Purpose: Define each business/API field restricted to a finite catalog.
- Fields:
  - `nomeCampo` (string, unique, required)
  - `contexto` (enum: EVENTO, PROJETO, OBSERVACAO, RECORRENCIA, APROVACAO, required)
  - `enumEntrada` (string, optional)
  - `enumResposta` (string, required)
  - `valoresCanonicos` (list<string>, required, non-empty)
  - `normalizacaoEntrada` (enum: TRIM_CASE_INSENSITIVE, required)
  - `aceitaSentinelaLegadoEmRequest` (boolean, fixed false for this feature)
- Validation rules:
  - `valoresCanonicos` must not contain duplicates.
  - `enumResposta` may include `UNKNOWN_LEGACY` only when the field can originate from persisted legacy data.
  - `enumEntrada` must never include `UNKNOWN_LEGACY`.

## 2. LegacyEnumSentinel
- Purpose: Represent the standard response-only marker for invalid persisted legacy categorical values.
- Fields:
  - `nome` (fixed string: `UNKNOWN_LEGACY`)
  - `aplicavelA` (list<string>, required)
  - `motivo` (string, required)
  - `requerAuditoria` (boolean, fixed true)
- Validation rules:
  - Must never be accepted in any changed request payload.
  - Must be emitted only for read flows backed by invalid stored data.

## 3. EndpointDtoContrato
- Purpose: Represent explicit request/response structures for endpoints previously using dynamic maps.
- Fields:
  - `endpointId` (string, unique, required)
  - `metodoHttp` (enum: GET, POST, PUT, PATCH, DELETE, required)
  - `requestDto` (string, optional for GET/DELETE)
  - `responseDto` (string, required)
  - `rejeitaCamposExtras` (boolean, required, true for changed requests)
  - `campos` (list<CampoContrato>, required)
- Validation rules:
  - Mutating endpoints with body must define `requestDto`.
  - All changed requests must set `rejeitaCamposExtras = true`.

## 4. CampoContrato
- Purpose: Define field-level schema metadata for DTOs.
- Fields:
  - `nome` (string, required)
  - `tipo` (enum: UUID, STRING, ENUM, INTEGER, BOOLEAN, ARRAY, OBJECT, DATETIME, required)
  - `obrigatorio` (boolean, required)
  - `fonteEnum` (string, optional when `tipo != ENUM`)
  - `aceitaNormalizacaoEntrada` (boolean, required for request enum fields)
  - `descricao` (string, optional)
- Validation rules:
  - If `tipo = ENUM`, `fonteEnum` must be defined.
  - Request enum fields must normalize only via `trim` + case-insensitive.

## 5. ErroValidacaoContrato
- Purpose: Machine-readable error item for DTO and enum validation failures.
- Fields:
  - `codigo` (string, required, deterministic)
  - `campo` (string, required)
  - `mensagem` (string, required)
  - `valorRejeitado` (string, optional)
  - `correlationId` (string, required)
- Candidate codes:
  - `VALIDATION_ENUM_VALUE_INVALID`
  - `VALIDATION_UNKNOWN_FIELD`
  - `VALIDATION_REQUIRED_FIELD`
- Validation rules:
  - `codigo` must be stable across releases for the same error class.
  - `campo` must map to DTO field names, not internal field aliases.

## 6. MapaMigracaoContrato
- Purpose: Map legacy dynamic payload keys to explicit DTO fields for client migration.
- Fields:
  - `endpointId` (string, required)
  - `chaveLegada` (string, required)
  - `campoNovo` (string, required)
  - `statusMigracao` (enum: MANTIDO, RENOMEADO, REMOVIDO, required)
  - `notaCompatibilidade` (string, optional)
- Validation rules:
  - One legacy key maps to one canonical field in the same endpoint.
  - Removed keys must include replacement guidance.

## 7. Candidate Enum Catalog for This Feature
- Input enums:
  - `TipoSolicitacaoInputEnum`: `ALTERACAO_HORARIO`, `CANCELAMENTO`, `RECLASSIFICACAO`, `OUTRO`
  - `TipoObservacaoInputEnum`: `NOTA`, `JUSTIFICATIVA`, `APROVACAO`, `REPROVACAO`, `CANCELAMENTO`, `AJUSTE_HORARIO`
  - `FrequenciaRecorrenciaInputEnum`: `DIARIA`, `SEMANAL`, `MENSAL`
- Response enums (legacy-aware):
  - `TipoSolicitacaoResponseEnum`: input values + `UNKNOWN_LEGACY`
  - `TipoObservacaoResponseEnum`: input values + `UNKNOWN_LEGACY`
  - `FrequenciaRecorrenciaResponseEnum`: input values + `UNKNOWN_LEGACY`

## 8. Implemented Response Sentinel Notes
- `UNKNOWN_LEGACY` is emitted only on response projection paths and never accepted on request DTO binding.
- Observation read flow includes a legacy-safe projection path with inconsistency auditing through `LegacyEnumInconsistencyPublisher`.
- Recurrence and approval response DTOs serialize response enums even when the current path receives canonical input enums.
- Unknown-field rejection remains orthogonal to sentinel handling: malformed payloads fail before any legacy projection path runs.

## 9. Relationships
- `EndpointDtoContrato (1) -> (N) CampoContrato`
- `CampoContrato (N) -> (0..1) CampoCategorico` when field is enum-based
- `CampoCategorico (0..1) -> (1) LegacyEnumSentinel`
- `EndpointDtoContrato (1) -> (N) MapaMigracaoContrato`
- `ErroValidacaoContrato` is emitted by validation of `EndpointDtoContrato` and enum parsing rules

## 10. State and Transition Notes
- Contract state evolves from `MAP_DYNAMICO` to `DTO_EXPLICITO` per endpoint.
- Categorical field state evolves from `STRING_LIVRE` to `ENUM_VALIDADO` at input boundaries.
- Legacy stored categorical state evolves from `VALOR_INVALIDO_PERSISTIDO` to response projection `UNKNOWN_LEGACY` with audit signal.
- Calendar domain lifecycle states remain unchanged in this feature.
