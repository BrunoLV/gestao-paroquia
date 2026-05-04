package br.com.nsfatima.calendario.api.dto.evento;

import br.com.nsfatima.calendario.domain.type.PapelEnvolvido;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EventoEnvolvidoInput(
        @NotNull UUID organizacaoId,
        @NotNull PapelEnvolvido papel) {
}
