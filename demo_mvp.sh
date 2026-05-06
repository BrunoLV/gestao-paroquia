#!/bin/bash

# demo_mvp.sh - A complete lifecycle demonstration of the NS Fatima Calendar MVP
# This script interacts with the API running at localhost:8080.
# Requires: curl, jq

set -e

BASE_URL="http://localhost:8080/api/v1"
LOG_FILE="demo_results.log"

# Standard Pastorals/Org IDs from init-data.sql
ORG_CATEQUESE="00000000-0000-0000-0000-0000000000aa"
ORG_LITURGIA="00000000-0000-0000-0000-0000000000bb"
ORG_CLERO="00000000-0000-0000-0000-0000000000dd"

echo "--- NS Fatima Calendar MVP Demo ---" | tee $LOG_FILE
echo "Starting lifecycle demonstration at $(date)" | tee -a $LOG_FILE

# Function to perform a request and log it
do_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local role=$4
    local org_type=$5
    local description=$6

    echo -e "\n>> Step: $description" | tee -a $LOG_FILE
    echo "Request: $method $endpoint" | tee -a $LOG_FILE

    response=$(curl -s -X $method "$BASE_URL$endpoint" \
        -H "Content-Type: application/json" \
        -H "X-Actor-Role: $role" \
        -H "X-Actor-Org-Type: $org_type" \
        -d "$data")

    echo "Response:" | tee -a $LOG_FILE
    echo "$response" | jq '.' | tee -a $LOG_FILE
    echo "$response"
}

# 1. Create a Project (Coordenador da Catequese)
PROJETO_PAYLOAD=$(cat <<EOF
{
  "nome": "Catequese 2027",
  "descricao": "Ciclo anual da catequese infantil",
  "organizacaoResponsavelId": "$ORG_CATEQUESE",
  "inicio": "$(date -u -d "+1 month" +"%Y-%m-%dT%H:%M:%SZ")",
  "fim": "$(date -u -d "+10 months" +"%Y-%m-%dT%H:%M:%SZ")"
}
EOF
)
RESPONSE=$(do_request "POST" "/projetos" "$PROJETO_PAYLOAD" "coordenador" "PASTORAL" "Creating a new Project for Catequese")
PROJETO_ID=$(echo "$RESPONSE" | jq -r '.id')

# 2. Create a standard Event (Coordenador da Catequese)
EVENT_PAYLOAD=$(cat <<EOF
{
  "titulo": "Aula Inaugural",
  "descricao": "Boas vindas aos alunos",
  "categoria": "FORMATIVO",
  "organizacaoResponsavelId": "$ORG_CATEQUESE",
  "projetoId": "$PROJETO_ID",
  "inicio": "$(date -u -d "+1 month + 2 days" +"%Y-%m-%dT09:00:00Z")",
  "fim": "$(date -u -d "+1 month + 2 days" +"%Y-%m-%dT11:00:00Z")",
  "status": "CONFIRMADO"
}
EOF
)
do_request "POST" "/eventos" "$EVENT_PAYLOAD" "coordenador" "PASTORAL" "Creating a confirmed Event in the project"

# 3. Create an Event requiring Approval (sensitive status ADICIONADO_EXTRA)
EVENT_APPROVAL_PAYLOAD=$(cat <<EOF
{
  "titulo": "Retiro de Coordenadores",
  "descricao": "Retiro espiritual fora do cronograma padrao",
  "categoria": "PASTORAL",
  "organizacaoResponsavelId": "$ORG_CATEQUESE",
  "projetoId": "$PROJETO_ID",
  "inicio": "$(date -u -d "+2 months" +"%Y-%m-%dT08:00:00Z")",
  "fim": "$(date -u -d "+2 months" +"%Y-%m-%dT18:00:00Z")",
  "status": "ADICIONADO_EXTRA",
  "adicionadoExtraJustificativa": "Necessidade de alinhamento urgente"
}
EOF
)
RESPONSE=$(do_request "POST" "/eventos" "$EVENT_APPROVAL_PAYLOAD" "coordenador" "PASTORAL" "Creating an Event that requires Approval (ADICIONADO_EXTRA)")
APROVACAO_ID=$(echo "$RESPONSE" | jq -r '.solicitacaoAprovacaoId')

# 4. List Pending Approvals (Pároco)
do_request "GET" "/aprovacoes?status=PENDENTE" "" "paroco" "CLERO" "Listing pending Approvals (as Paroco)"

# 5. Approve the request (Pároco)
DECISION_PAYLOAD='{"status": "APROVADA", "observacao": "Importante para o alinhamento paroquial."}'
do_request "PATCH" "/aprovacoes/$APROVACAO_ID" "$DECISION_PAYLOAD" "paroco" "CLERO" "Approving the event creation"

# 6. Query Project Aggregation (Check health and collaboration)
do_request "GET" "/projetos/$PROJETO_ID/resumo" "" "membro" "PASTORAL" "Fetching Project Aggregation (Resumo)"

# 7. List Audit Trail for the project
do_request "GET" "/audit/eventos" "" "paroco" "CLERO" "Querying the Audit Trail"

echo -e "\n--- Demo Completed Successfully ---" | tee -a $LOG_FILE
echo "Detailed logs saved to $LOG_FILE"
