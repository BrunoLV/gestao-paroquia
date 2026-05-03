package br.com.nsfatima.calendario.api.dto.evento;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;

public record UpdateEventoRequest(
        @Size(max = 160) @Pattern(regexp = ".*\\S.*", message = "titulo must not be blank if provided") String titulo,
        @Size(max = 4000) String descricao,
        Instant inicio,
        Instant fim,
        EventoStatusInput status,
        @Size(max = 4000) String adicionadoExtraJustificativa,
        @Size(max = 2000) String canceladoMotivo,
        UUID organizacaoResponsavelId,
        List<UUID> participantes) {

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
