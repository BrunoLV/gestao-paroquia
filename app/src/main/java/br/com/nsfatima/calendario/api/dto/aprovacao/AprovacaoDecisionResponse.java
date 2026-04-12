package br.com.nsfatima.calendario.api.dto.aprovacao;

import java.util.UUID;

public record AprovacaoDecisionResponse(
        UUID id,
        String status,
        ActionExecution actionExecution) {

    public record ActionExecution(
            String outcome,
            UUID eventoId,
            String eventStatus,
            String errorCode) {
    }
}
