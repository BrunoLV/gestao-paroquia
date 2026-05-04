package br.com.nsfatima.calendario.api.dto.evento;

import java.util.List;
import java.util.UUID;

public record EventoEnvolvidosResponse(
        UUID eventoId,
        List<EventoEnvolvidoOutput> envolvidos) {

    public record EventoEnvolvidoOutput(
            UUID organizacaoId,
            String papel) {
    }
}
