package br.com.nsfatima.calendario.api.dto.evento;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record EventoEnvolvidosRequest(
        @NotNull List<EventoEnvolvidoInput> envolvidos) {
}
