package br.com.nsfatima.gestao.calendario.api.dto.aprovacao;

import io.swagger.v3.oas.annotations.media.Schema;
import br.com.nsfatima.gestao.calendario.domain.type.AprovacaoStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitação para decisão de uma aprovação")
public record AprovacaoDecisionRequest(
                @Schema(description = "Novo status da aprovação (APROVADO ou REJEITADO)")
                @NotNull AprovacaoStatus status,

                @Schema(description = "Observação facultativa sobre a decisão", example = "Evento aprovado conforme critérios pastorais")
                @Size(max = 2000) String observacao) {
}
