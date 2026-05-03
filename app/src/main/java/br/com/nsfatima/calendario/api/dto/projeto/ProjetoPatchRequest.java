package br.com.nsfatima.calendario.api.dto.projeto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProjetoPatchRequest(
        @Size(max = 160) @Pattern(regexp = ".*\\S.*", message = "nome must not be blank if provided") String nome,
        @Size(max = 2000) String descricao) {
}
