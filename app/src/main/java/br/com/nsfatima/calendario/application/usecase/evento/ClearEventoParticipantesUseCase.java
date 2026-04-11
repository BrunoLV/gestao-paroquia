package br.com.nsfatima.calendario.application.usecase.evento;

import java.util.List;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.EventoParticipantesResponse;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClearEventoParticipantesUseCase {

    private final EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository;

    public ClearEventoParticipantesUseCase(EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository) {
        this.eventoEnvolvidoJpaRepository = eventoEnvolvidoJpaRepository;
    }

    @Transactional
    public EventoParticipantesResponse execute(UUID eventoId) {
        eventoEnvolvidoJpaRepository.deleteByEventoId(eventoId);
        return new EventoParticipantesResponse(eventoId, List.of());
    }
}
