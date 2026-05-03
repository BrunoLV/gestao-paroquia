# Quickstart: Testando o Refinamento da API de Eventos

Este guia orienta como validar as novas capacidades da API.

## Pré-requisitos

- Servidor rodando localmente (Porta 8080).
- Usuário autenticado com permissão para ao menos uma organização.

## 1. Buscar Detalhes de um Evento

Substitua `{id}` pelo UUID de um evento existente:

```bash
curl -X GET http://localhost:8080/api/v1/eventos/{id} \
     -H "Authorization: Bearer <TOKEN>"
```

**O que observar:**
- Resposta 200 com DTO completo.
- Log de auditoria gerado no banco (`READ`).
- Métrica de latência registrada nos logs do servidor.

## 2. Listagem Paginada e Filtros

```bash
curl -X GET "http://localhost:8080/api/v1/eventos?start_date=2026-05-01T00:00:00Z&end_date=2026-05-31T23:59:59Z&page=0&size=10" \
     -H "Authorization: Bearer <TOKEN>"
```

**O que observar:**
- Retorno formatado com metadados de página (`totalPages`, `totalElements`).
- Apenas eventos dentro do mês de Maio de 2026 devem aparecer.

## 3. Cancelamento via POST (Novo)

```bash
curl -X POST http://localhost:8080/api/v1/eventos/{id}/cancel \
     -H "Authorization: Bearer <TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"motivo": "Alteração no cronograma paroquial"}'
```

**O que observar:**
- Mudança do status para `CANCELADO`.
- Persistência do motivo na trilha de auditoria.

## 4. Cancelamento via DELETE (Legado)

```bash
curl -X DELETE http://localhost:8080/api/v1/eventos/{id} \
     -H "Authorization: Bearer <TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"motivo": "Cancelamento via rota obsoleta"}'
```

**O que observar:**
- Funcionamento normal, mas com cabeçalho `Warning: 299 - "This endpoint is deprecated"` (se implementado) ou aviso nos logs.
