package br.com.nsfatima.calendario.api.dto.observacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import br.com.nsfatima.calendario.domain.type.TipoObservacaoInput;

public record ObservacaoCreateRequest(
                @NotNull TipoObservacaoInput tipo,
                @NotBlank String conteudo) {
}
