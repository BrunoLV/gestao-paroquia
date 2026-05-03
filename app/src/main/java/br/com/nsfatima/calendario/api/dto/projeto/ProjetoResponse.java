package br.com.nsfatima.calendario.api.dto.projeto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Dados de um projeto paroquial")
public record ProjetoResponse(
        @Schema(description = "ID único do projeto")
        UUID id,

        @Schema(description = "Nome do projeto")
        String nome,

        @Schema(description = "Descrição do projeto")
        String descricao,

        @Schema(description = "Indica se o projeto foi atualizado")
        boolean updated) {
}
