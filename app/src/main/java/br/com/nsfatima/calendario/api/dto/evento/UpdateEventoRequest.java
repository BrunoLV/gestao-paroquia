package br.com.nsfatima.calendario.api.dto.evento;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;

public record UpdateEventoRequest(
        String titulo,
        String descricao,
        Instant inicio,
        Instant fim,
        EventoStatusInput status,
        String adicionadoExtraJustificativa,
        String canceladoMotivo,
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
