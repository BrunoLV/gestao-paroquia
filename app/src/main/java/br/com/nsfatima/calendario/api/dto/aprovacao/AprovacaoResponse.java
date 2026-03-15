package br.com.nsfatima.calendario.api.dto.aprovacao;

import java.util.UUID;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoResponse;

public record AprovacaoResponse(
        UUID id,
        UUID eventoId,
        TipoSolicitacaoResponse tipoSolicitacao,
        String status) {
}
