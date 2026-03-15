package br.com.nsfatima.calendario.application.usecase.evento;

import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.EventoRecorrenciaResponse;
import br.com.nsfatima.calendario.domain.type.FrequenciaRecorrenciaInput;
import br.com.nsfatima.calendario.domain.type.FrequenciaRecorrenciaResponse;
import br.com.nsfatima.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import org.springframework.stereotype.Service;

@Service
public class CreateEventoRecorrenciaUseCase {

    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;

    public CreateEventoRecorrenciaUseCase(LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher) {
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
    }

    public EventoRecorrenciaResponse execute(UUID eventoId, FrequenciaRecorrenciaInput frequencia, int intervalo) {
        return new EventoRecorrenciaResponse(
                UUID.randomUUID(),
                eventoId,
                FrequenciaRecorrenciaResponse.fromStoredValue(
                        frequencia.name(),
                        legacyEnumInconsistencyPublisher,
                        eventoId.toString()),
                intervalo);
    }
}
