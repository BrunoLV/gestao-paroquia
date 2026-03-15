# Compatibility Notice: Payload Completo de Criacao de Evento

## Resumo

O fluxo de criacao de eventos em `POST /api/v1/eventos` passa a exigir payload completo em uma unica operacao, com persistencia real, `Idempotency-Key` obrigatorio e validacao estrita de campos.

## Mudancas Obrigatorias para Consumidores

- Enviar `organizacaoResponsavelId`, `inicio` e `fim` no payload inicial de create.
- Enviar `Idempotency-Key` em toda chamada de criacao.
- Parar de depender de preenchimento posterior obrigatorio por endpoints separados para concluir o cadastro.
- Remover campos desconhecidos do payload: o contrato agora rejeita propriedades nao documentadas.

## Compatibilidade de Transicao

- A janela de transicao permanece limitada a 2 releases minor ou 90 dias apos a liberacao da feature, o que ocorrer primeiro.
- O encerramento da janela depende de ausencia de trafego no contrato antigo por 2 semanas consecutivas e aprovacao do owner da API.
- A compatibilidade preservada nesta entrega e de leitura: eventos legados continuam listados com mapeamento seguro de status.

## Impacto Esperado

- Clientes passam de um fluxo multioperacao para uma chamada obrigatoria unica.
- Retries de rede deixam de duplicar eventos quando a mesma `Idempotency-Key` e reutilizada com payload equivalente.
- Reutilizacao da mesma chave com payload divergente passa a retornar `409` com `IDEMPOTENCY_KEY_CONFLICT`.

## Referencias

- Contrato delta: `specs/003-complete-event-creation/contracts/calendar-api-complete-event-create.openapi.yaml`
- Evidencia de entrega: `specs/003-complete-event-creation/implementation-evidence.md`
