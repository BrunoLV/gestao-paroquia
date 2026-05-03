package br.com.nsfatima.calendario.api.dto.aprovacao;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Filtros para listagem de solicitações de aprovação")
public record AprovacaoFiltroRequest(
        @Schema(description = "Filtrar por ID do evento")
        UUID eventoId,

        @Schema(description = "Filtrar por status da solicitação (PENDENTE, APROVADA, REPROVADA)", example = "PENDENTE")
        String status) {
}
