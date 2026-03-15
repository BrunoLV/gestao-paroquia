package br.com.nsfatima.calendario.api.dto.evento;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record EventoParticipantesRequest(
        @NotNull List<UUID> organizacoesParticipantes) {
}
