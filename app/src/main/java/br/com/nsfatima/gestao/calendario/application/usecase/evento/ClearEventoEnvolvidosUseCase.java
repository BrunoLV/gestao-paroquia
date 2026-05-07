package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import java.util.Collections;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.api.dto.evento.EventoEnvolvidosResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClearEventoEnvolvidosUseCase {

    private final EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository;

    public ClearEventoEnvolvidosUseCase(EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository) {
        this.eventoEnvolvidoJpaRepository = eventoEnvolvidoJpaRepository;
    }

    @Transactional
    public EventoEnvolvidosResponse execute(UUID eventoId) {
        eventoEnvolvidoJpaRepository.deleteByEventoId(eventoId);
        return new EventoEnvolvidosResponse(eventoId, Collections.emptyList());
    }
}
