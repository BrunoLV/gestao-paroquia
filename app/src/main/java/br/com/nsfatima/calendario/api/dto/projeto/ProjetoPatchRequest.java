package br.com.nsfatima.calendario.api.dto.projeto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitação para atualização parcial de um projeto")
public record ProjetoPatchRequest(
        @Schema(description = "Novo nome do projeto")
        @Size(max = 160) @Pattern(regexp = ".*\\S.*", message = "nome must not be blank if provided") String nome,

        @Schema(description = "Nova descrição do projeto")
        @Size(max = 2000) String descricao) {
}
