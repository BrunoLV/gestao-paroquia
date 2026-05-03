package br.com.nsfatima.calendario.api.dto.projeto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitação para criação de um novo projeto")
public record ProjetoCreateRequest(
        @Schema(description = "Nome do projeto", example = "Reforma da Capela")
        @NotBlank @Size(max = 160) String nome,

        @Schema(description = "Descrição detalhada do projeto", example = "Projeto para arrecadação de fundos e execução da reforma")
        @Size(max = 2000) String descricao) {
}
