package br.com.nsfatima.calendario.api.dto.aprovacao;

import com.fasterxml.jackson.annotation.JsonProperty;
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

                @JsonProperty("targetResourceId")
                public UUID targetResourceId() {
                        return eventoId;
                }

                @JsonProperty("targetStatus")
                public String targetStatus() {
                        return eventStatus;
                }
        }
}
