package br.com.nsfatima.calendario.api.dto.evento;

import br.com.nsfatima.calendario.domain.type.PapelEnvolvido;
import java.util.List;
import java.util.UUID;

public record EventoEnvolvidosResponse(
        UUID eventoId,
        List<EventoEnvolvidoOutput> envolvidos) {

    public record EventoEnvolvidoOutput(
            UUID organizacaoId,
            PapelEnvolvido papel) {
    }
}
