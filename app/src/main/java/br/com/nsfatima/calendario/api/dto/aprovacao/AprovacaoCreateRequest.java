package br.com.nsfatima.calendario.api.dto.aprovacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;

public record AprovacaoCreateRequest(
        @NotNull UUID eventoId,
        @NotNull TipoSolicitacaoInput tipoSolicitacao) {
}
