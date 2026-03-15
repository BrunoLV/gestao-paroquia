package br.com.nsfatima.calendario.api.dto.evento;

import java.time.Instant;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;

public record UpdateEventoRequest(
                String titulo,
                String descricao,
                Instant inicio,
                Instant fim,
                EventoStatusInput status,
                String adicionadoExtraJustificativa,
                String canceladoMotivo) {
}
