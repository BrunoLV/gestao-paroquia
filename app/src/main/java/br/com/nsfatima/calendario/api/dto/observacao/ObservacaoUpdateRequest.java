package br.com.nsfatima.calendario.api.dto.observacao;

import jakarta.validation.constraints.NotBlank;

public record ObservacaoUpdateRequest(@NotBlank String conteudo) {
}
