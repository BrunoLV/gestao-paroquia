package br.com.nsfatima.gestao.calendario.api.dto.evento;

import java.util.UUID;

public record CancelamentoPendenteResponse(
        UUID solicitacaoAprovacaoId,
        String status,
        UUID eventoId,
        String mensagem) {
}
