package br.com.nsfatima.calendario.application.usecase.evento;

import br.com.nsfatima.calendario.api.dto.evento.EventoFiltroRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListEventosUseCase {

    private final EventoJpaRepository eventoJpaRepository;
    private final EventoMapper eventoMapper;

    public ListEventosUseCase(EventoJpaRepository eventoJpaRepository, EventoMapper eventoMapper) {
        this.eventoJpaRepository = eventoJpaRepository;
        this.eventoMapper = eventoMapper;
    }

    public Page<EventoResponse> execute(EventoFiltroRequest filters, Pageable pageable) {
        return eventoJpaRepository.findAllWithFilters(
                filters.start_date(),
                filters.end_date(),
                filters.organizacao_id(),
                pageable)
                .map(eventoMapper::toResponse);
    }
}
