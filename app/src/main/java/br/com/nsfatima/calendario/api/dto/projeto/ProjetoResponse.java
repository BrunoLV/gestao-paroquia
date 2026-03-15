package br.com.nsfatima.calendario.api.dto.projeto;

import java.util.UUID;

public record ProjetoResponse(
        UUID id,
        String nome,
        String descricao,
        boolean updated) {
}
