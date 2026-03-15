package br.com.nsfatima.calendario.application.usecase.evento;

import java.util.List;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.EventoParticipantesResponse;
import org.springframework.stereotype.Service;

@Service
public class ClearEventoParticipantesUseCase {

    public EventoParticipantesResponse execute(UUID eventoId) {
        return new EventoParticipantesResponse(eventoId, List.of());
    }
}
