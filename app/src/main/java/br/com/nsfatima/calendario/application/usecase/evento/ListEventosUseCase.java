package br.com.nsfatima.calendario.application.usecase.evento;

import java.util.List;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ListEventosUseCase {

    private final EventoJpaRepository eventoJpaRepository;
    private final EventoMapper eventoMapper;

    public ListEventosUseCase(EventoJpaRepository eventoJpaRepository, EventoMapper eventoMapper) {
        this.eventoJpaRepository = eventoJpaRepository;
        this.eventoMapper = eventoMapper;
    }

    public List<EventoResponse> execute() {
        return eventoJpaRepository.findAllByOrderByInicioUtcAscIdAsc()
                .stream()
                .map(eventoMapper::toResponse)
                .toList();
    }
}
