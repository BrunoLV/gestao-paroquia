package br.com.nsfatima.calendario.api.dto.evento;

import java.util.List;
import java.util.UUID;

public record EventoParticipantesResponse(
        UUID eventoId,
        List<UUID> organizacoesParticipantes) {
}
