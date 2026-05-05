package br.com.nsfatima.calendario.api.dto.projeto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo agregado do projeto")
public record ProjetoResumoDTO(
        StatusExecucaoDTO statusExecucao,
        MapaColaboracaoDTO mapaColaboracao,
        SaudeTemporalDTO saudeTemporal
) {
}
