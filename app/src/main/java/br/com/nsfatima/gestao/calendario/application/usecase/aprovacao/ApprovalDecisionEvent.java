package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import br.com.nsfatima.gestao.calendario.domain.type.AprovacaoStatus;
import java.util.UUID;

public record ApprovalDecisionEvent(
        UUID aprovacaoId,
        UUID eventoId,
        String solicitanteId,
        AprovacaoStatus status,
        String decisorId,
        String erroExecucao) {
}
