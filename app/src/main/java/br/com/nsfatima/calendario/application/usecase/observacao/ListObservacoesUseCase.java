package br.com.nsfatima.calendario.application.usecase.observacao;

import java.util.List;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.calendario.domain.type.TipoObservacaoResponse;
import br.com.nsfatima.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import org.springframework.stereotype.Service;

@Service
public class ListObservacoesUseCase {

    private static final UUID LEGACY_EVENT_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;

    public ListObservacoesUseCase(LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher) {
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
    }

    public List<ObservacaoResponse> execute(UUID eventoId) {
        TipoObservacaoResponse tipo = eventoId.equals(LEGACY_EVENT_ID)
                ? TipoObservacaoResponse.fromStoredValue(
                        "ANOTACAO_LEGADA",
                        legacyEnumInconsistencyPublisher,
                        eventoId.toString())
                : TipoObservacaoResponse.NOTA;
        return List.of(new ObservacaoResponse(
                UUID.randomUUID(),
                eventoId,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                tipo,
                "Historico append-only"));
    }
}
