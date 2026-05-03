package br.com.nsfatima.calendario.api.dto.evento;

import br.com.nsfatima.calendario.api.dto.validation.ValidEventDates;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;

@ValidEventDates
public record CreateEventoRequest(
        @NotBlank @Size(max = 160) String titulo,
        @Size(max = 4000) String descricao,
        @NotNull UUID organizacaoResponsavelId,
        @NotNull Instant inicio,
        @NotNull Instant fim,
        EventoStatusInput status,
        String adicionadoExtraJustificativa,
        List<UUID> participantes) {
}
