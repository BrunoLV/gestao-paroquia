package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.domain.type.AprovacaoStatus;
import java.util.UUID;

public record ApprovalDecisionEvent(
        UUID aprovacaoId,
        UUID eventoId,
        String solicitanteId,
        AprovacaoStatus status,
        String decisorId,
        String erroExecucao) {
}
