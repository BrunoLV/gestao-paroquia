package br.com.nsfatima.calendario.api.dto.evento;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import br.com.nsfatima.calendario.domain.type.FrequenciaRecorrenciaInput;

public record EventoRecorrenciaRequest(
        @NotNull FrequenciaRecorrenciaInput frequencia,
        @Min(1) int intervalo) {
}
