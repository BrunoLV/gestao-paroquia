package br.com.nsfatima.calendario.api.dto.aprovacao;

import br.com.nsfatima.calendario.domain.type.AprovacaoStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AprovacaoDecisionRequest(
                @NotNull AprovacaoStatus status,
                @Size(max = 2000) String observacao) {
}
