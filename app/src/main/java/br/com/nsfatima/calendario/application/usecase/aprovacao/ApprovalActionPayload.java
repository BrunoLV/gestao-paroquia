package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.domain.type.EventoStatusInput;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApprovalActionPayload(
        String idempotencyKey,
        UUID eventoId,
        String titulo,
        String descricao,
        UUID organizacaoResponsavelId,
        Instant inicio,
        Instant fim,
        EventoStatusInput status,
        String adicionadoExtraJustificativa,
        String canceladoMotivo,
        String motivo,
        List<UUID> participantes) {
}
