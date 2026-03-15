package br.com.nsfatima.calendario.api.dto.observacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import br.com.nsfatima.calendario.domain.type.TipoObservacaoInput;

public record ObservacaoCreateRequest(
        @NotNull UUID usuarioId,
        @NotNull TipoObservacaoInput tipo,
        @NotBlank String conteudo) {
}
