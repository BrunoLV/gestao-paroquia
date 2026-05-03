package br.com.nsfatima.calendario.api.dto.projeto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjetoCreateRequest(
        @NotBlank @Size(max = 160) String nome,
        @Size(max = 2000) String descricao) {
}
