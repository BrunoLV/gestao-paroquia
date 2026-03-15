package br.com.nsfatima.calendario.api.dto.evento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;

public record CreateEventoRequest(
                @NotBlank String titulo,
                String descricao,
                @NotNull Instant inicio,
                @NotNull Instant fim,
                EventoStatusInput status,
                String adicionadoExtraJustificativa) {
}
