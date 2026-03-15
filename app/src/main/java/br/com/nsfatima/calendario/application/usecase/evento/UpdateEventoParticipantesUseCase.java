package br.com.nsfatima.calendario.application.usecase.evento;

import java.util.List;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.EventoParticipantesResponse;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdateEventoParticipantesUseCase {

    private final EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository;

    public UpdateEventoParticipantesUseCase(EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository) {
        this.eventoEnvolvidoJpaRepository = eventoEnvolvidoJpaRepository;
    }

    public EventoParticipantesResponse execute(UUID eventoId, List<UUID> participantes) {
        eventoEnvolvidoJpaRepository.deleteByEventoId(eventoId);

        if (participantes != null && !participantes.isEmpty()) {
            List<EventoEnvolvidoEntity> entities = participantes.stream()
                    .distinct()
                    .map(organizacaoId -> {
                        EventoEnvolvidoEntity entity = new EventoEnvolvidoEntity();
                        entity.setEventoId(eventoId);
                        entity.setOrganizacaoId(organizacaoId);
                        return entity;
                    })
                    .toList();
            eventoEnvolvidoJpaRepository.saveAll(entities);
        }

        List<UUID> persisted = eventoEnvolvidoJpaRepository.findByEventoId(eventoId)
                .stream()
                .map(EventoEnvolvidoEntity::getOrganizacaoId)
                .toList();

        return new EventoParticipantesResponse(eventoId, persisted);
    }
}
