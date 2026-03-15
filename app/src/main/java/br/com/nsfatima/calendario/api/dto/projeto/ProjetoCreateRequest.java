package br.com.nsfatima.calendario.api.dto.projeto;

import jakarta.validation.constraints.NotBlank;

public record ProjetoCreateRequest(
        @NotBlank String nome,
        String descricao) {
}
