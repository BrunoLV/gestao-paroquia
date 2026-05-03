package br.com.nsfatima.calendario.api.dto.aprovacao;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoResponse;

@Schema(description = "Dados de uma solicitação de aprovação")
public record AprovacaoResponse(
        @Schema(description = "ID único da aprovação")
        UUID id,

        @Schema(description = "ID do evento associado")
        UUID eventoId,

        @Schema(description = "Tipo da solicitação")
        TipoSolicitacaoResponse tipoSolicitacao,

        @Schema(description = "Status atual da solicitação")
        String status) {
}
