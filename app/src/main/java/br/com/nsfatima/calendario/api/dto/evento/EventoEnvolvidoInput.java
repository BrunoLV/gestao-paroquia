package br.com.nsfatima.calendario.api.dto.evento;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EventoEnvolvidoInput(
        @NotNull UUID organizacaoId,
        String papel) {
}
