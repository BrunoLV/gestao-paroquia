package br.com.nsfatima.calendario.api.dto.observacao;

import java.time.Instant;
import java.util.UUID;
import br.com.nsfatima.calendario.domain.type.TipoObservacaoResponse;

public record ObservacaoResponse(
                UUID id,
                UUID eventoId,
                UUID usuarioId,
                TipoObservacaoResponse tipo,
                String conteudo,
                Instant criadoEmUtc) {
}
