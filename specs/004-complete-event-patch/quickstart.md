# Quickstart: PATCH Completo de Evento

## 1. Prerequisites
- Java 21
- Gradle Wrapper (`./gradlew`)
- Banco configurado para profile local (`app/src/main/resources/application.yml`)

## 2. Build Baseline
1. Validar baseline da branch:
```bash
./gradlew :app:build
```
2. Rodar suites relevantes atuais para mapear regressao inicial:
```bash
./gradlew :app:test --tests '*EventosContractTest' --tests '*CreateEventoPersistenciaIntegrationTest' --tests '*CreateEventoValidationIntegrationTest'
```

## 3. Implementation Checklist (Feature 004)
1. Completar `UpdateEventoUseCase` para persistencia real com transacao.
2. Aplicar autorizacao de PATCH alinhada ao create.
3. Implementar regras especificas:
   - coordenador/vice da organizacao responsavel pode adicionar participantes;
   - coordenador/vice da organizacao responsavel nao pode alterar organizacao responsavel;
   - coordenador do conselho ou parroco pode alterar organizacao responsavel.
4. Exigir aprovacao valida para alteracao de data/horario e cancelamento.
5. Garantir resposta de PATCH refletindo estado persistido apos commit.
6. Garantir falhas sem persistencia parcial.

## 4. Focused Validation
1. PATCH bem-sucedido com persistencia real:
```bash
./gradlew :app:test --tests '*UpdateEventoPersistenciaIntegrationTest'
```
2. Validacoes de dominio e payload parcial:
```bash
./gradlew :app:test --tests '*UpdateEventoValidationIntegrationTest' --tests '*UnknownFieldRejectionIntegrationTest'
```
3. Autorizacao e permissao por papeis:
```bash
./gradlew :app:test --tests '*UpdateEventoAuthorizationIntegrationTest'
```
4. Aprovacao obrigatoria para data/cancelamento:
```bash
./gradlew :app:test --tests '*UpdateEventoApprovalIntegrationTest'
```
5. Concorrencia e conflito otimista:
```bash
./gradlew :app:test --tests '*UpdateEventoConcurrencyIntegrationTest'
```

## 5. Manual API Smoke
1. Criar evento de referencia (se ainda nao existir).
2. Executar PATCH alterando campo geral permitido por coordenador/vice da org responsavel -> `200`.
3. Executar PATCH adicionando participantes por coordenador/vice da org responsavel -> `200`.
4. Executar PATCH tentando trocar organizacao responsavel por coordenador/vice da org responsavel -> `403` (`FORBIDDEN`).
5. Executar PATCH trocando organizacao responsavel com papel de coordenador do conselho ou parroco -> `200`.
6. Executar PATCH alterando data sem aprovacao -> `400/403` conforme mapeamento final com `APPROVAL_REQUIRED`.
7. Reconsultar evento e validar que resposta do PATCH corresponde ao estado persistido.

## 6. Contract Checklist
- `PATCH /api/v1/eventos/{eventoId}` permanece mesma rota/metodo.
- Payload parcial com `additionalProperties: false`.
- `EVENT_NOT_FOUND`, `VALIDATION_ERROR`, `DOMAIN_RULE_VIOLATION`, `FORBIDDEN`, `APPROVAL_REQUIRED`, `CONFLICT` documentados e testaveis.
- Regras de permissionamento e aprovacao refletidas no comportamento observavel.

## 7. Observability Checklist
- Toda tentativa de PATCH gera trilha com correlation id, ator, acao, alvo e resultado.
- Falhas por permissao e aprovacao ficam distinguiveis por codigo/categoria.
- Quando houver aprovacao no fluxo, identificador do aprovador/aprovacao e registrado sem expor dados sensiveis.

## 8. Measurement Checklist (SC-001..SC-004)
- SC-001: comparar resposta PATCH com leitura subsequente do mesmo evento em testes.
- SC-002: validar cenarios negativos sem alteracao de estado persistido.
- SC-003: registrar tempo de execucao de PATCH valido e verificar meta de 95% <= 2s.
- SC-004: auditar amostra de sucesso/falha e confirmar cobertura de logs pesquisaveis.
- Registrar evidencias da execucao no PR da feature 004.

## 9. Final Validation Run (2026-03-15)
1. Executar a bateria focada final:
```bash
./gradlew :app:test --tests '*EventosPatchContractTest' --tests '*EventoMutacaoContractTest' --tests '*UpdateEvento*IntegrationTest' --tests '*AtomicUpdateValidationIntegrationTest' --tests '*EnumNormalizationIntegrationTest' --tests '*PublicStatusVisibilityIntegrationTest'
```
2. Resultado esperado para fechamento da feature: `BUILD SUCCESSFUL`.
