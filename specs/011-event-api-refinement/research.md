# Pesquisa Técnica: Refinamento da API de Eventos

## Auditoria de Leitura (RF-003)

**Decisão**: Utilizar a infraestrutura existente de `EventoAuditPublisher` e `AuditLogService`.

**Racional**: O projeto já possui uma trilha de auditoria estruturada que persiste dados na tabela `calendario.auditoria_operacoes`. 
Para atender ao requisito de auditoria de leitura, será necessário:
1. Incluir a ação `READ` (ou `GET`) na lista permitida em `AuditLogService.isAuditableAction`.
2. Disparar o log de auditoria no `EventoController` ao buscar detalhes de um evento.

**Alternativas Consideradas**:
- **AOP/Interceptors**: Rejeitado por não se alinhar ao padrão atual de chamadas explícitas nos Controllers/Use Cases que garantem o contexto correto do ator.

## Captura de Latência (RF-004 / SC-001)

**Decisão**: Medição manual no Controller via `System.nanoTime()` e publicação via `CadastroEventoMetricsPublisher`.

**Racional**: Segue o padrão estabelecido no projeto para capturar latência de consultas ao calendário e outras operações críticas. A infraestrutura de snapshots de performance (P95) já consome esses logs.

**Alternativas Consideradas**:
- **Micrometer @Timed**: Rejeitado pois a infraestrutura Prometheus/Micrometer ainda não está totalmente configurada para exposição externa, e o projeto prefere snapshots baseados em logs SLF4J com prefixo `metric`.

## Paginação e Filtros (RF-005 / RF-006)

**Decisão**: Introduzir o uso de `org.springframework.data.domain.Pageable` e `Page` no repositório `EventoRepository`.

**Racional**: Atualmente o projeto não utiliza paginação oficial do Spring Data. Como o requisito de calendário exige alto volume de dados, a introdução desse padrão é necessária para evitar sobrecarga de memória e rede.
Os filtros (`start_date`, `end_date`, `organizacao_id`) serão passados via `@RequestParam` e processados por queries customizadas ou `Specifications`.

## Novo Endpoint de Cancelamento (RF-008 / SC-004)

**Decisão**: Implementar `POST /api/v1/eventos/{id}/cancel`.

**Racional**: Resolve o problema de middlewares que removem o corpo (body) de requisições `DELETE`. O uso de `POST` para ações que alteram estado e exigem dados (como o motivo do cancelamento) é uma prática recomendada para maior resiliência. O endpoint legado `DELETE` será mantido com `@Deprecated` conforme solicitado.
