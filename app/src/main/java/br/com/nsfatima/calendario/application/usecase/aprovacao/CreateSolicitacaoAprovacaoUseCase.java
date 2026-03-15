package br.com.nsfatima.calendario.application.usecase.aprovacao;

import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoResponse;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoResponse;
import br.com.nsfatima.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import org.springframework.stereotype.Service;

@Service
public class CreateSolicitacaoAprovacaoUseCase {

    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;

    public CreateSolicitacaoAprovacaoUseCase(LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher) {
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
    }

    public AprovacaoResponse create(UUID eventoId, TipoSolicitacaoInput tipoSolicitacao) {
        return new AprovacaoResponse(
                UUID.randomUUID(),
                eventoId,
                TipoSolicitacaoResponse.fromStoredValue(
                        tipoSolicitacao.name(),
                        legacyEnumInconsistencyPublisher,
                        eventoId.toString()),
                "PENDENTE");
    }
}
