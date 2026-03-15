package br.com.nsfatima.calendario.api.dto.evento;

import java.time.Instant;
import java.util.UUID;
import br.com.nsfatima.calendario.domain.type.EventoStatusResponse;

public record EventoResponse(
                UUID id,
                String titulo,
                String descricao,
                Instant inicio,
                Instant fim,
                EventoStatusResponse status) {
}
