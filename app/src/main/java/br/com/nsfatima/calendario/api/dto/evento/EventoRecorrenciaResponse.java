package br.com.nsfatima.calendario.api.dto.evento;

import java.util.UUID;
import br.com.nsfatima.calendario.domain.type.FrequenciaRecorrenciaResponse;

public record EventoRecorrenciaResponse(
        UUID id,
        UUID eventoBaseId,
        FrequenciaRecorrenciaResponse frequencia,
        int intervalo) {
}
