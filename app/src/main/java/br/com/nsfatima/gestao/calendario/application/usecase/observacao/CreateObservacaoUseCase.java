package br.com.nsfatima.gestao.calendario.application.usecase.observacao;

import java.util.UUID;
import java.time.Instant;
import br.com.nsfatima.gestao.calendario.api.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoResponse;
import org.springframework.stereotype.Service;

@Service
public class CreateObservacaoUseCase {

    public ObservacaoResponse execute(UUID eventoId, UUID usuarioId, TipoObservacaoInput tipo, String conteudo) {
        return new ObservacaoResponse(
                UUID.randomUUID(),
                eventoId,
                usuarioId,
                TipoObservacaoResponse.fromInput(tipo),
                conteudo,
                Instant.now());
    }
}
