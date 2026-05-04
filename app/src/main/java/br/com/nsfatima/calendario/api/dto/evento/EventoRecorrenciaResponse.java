package br.com.nsfatima.calendario.api.dto.evento;

import java.util.UUID;

public record EventoRecorrenciaResponse(
        UUID id,
        UUID eventoBaseId,
        String frequencia,
        int intervalo) {
}
