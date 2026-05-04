package br.com.nsfatima.calendario.api.dto.evento;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;

@Schema(description = "Solicitação para atualização parcial de um evento")
public record UpdateEventoRequest(
        @Schema(description = "Novo título do evento")
        @Size(max = 160) @Pattern(regexp = ".*\\S.*", message = "titulo must not be blank if provided") String titulo,

        @Schema(description = "Nova descrição do evento")
        @Size(max = 4000) String descricao,

        @Schema(description = "Nova data de início")
        Instant inicio,

        @Schema(description = "Nova data de término")
        Instant fim,

        @Schema(description = "Novo status")
        EventoStatusInput status,

        @Schema(description = "Nova justificativa para adição extra")
        @Size(max = 4000) String adicionadoExtraJustificativa,

        @Schema(description = "Motivo do cancelamento")
        @Size(max = 2000) String canceladoMotivo,

        @Schema(description = "Novo ID da organização responsável")
        UUID organizacaoResponsavelId,

        @Schema(description = "Lista de IDs das organizações participantes")
        List<UUID> participantes,

        @Schema(description = "Escopo da edição (para eventos recorrentes)")
        EventoEditScope editScope) {

        public boolean isEmptyPayload() {

        return titulo == null
                && descricao == null
                && inicio == null
                && fim == null
                && status == null
                && adicionadoExtraJustificativa == null
                && canceladoMotivo == null
                && organizacaoResponsavelId == null
                && participantes == null;
    }

    public boolean changesSensitiveFields() {
        return inicio != null || fim != null || canceladoMotivo != null || status == EventoStatusInput.CANCELADO;
    }
}
