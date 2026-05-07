package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.dto.evento.EventoFiltroRequest;
import br.com.nsfatima.gestao.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.util.List;
import java.util.Optional;
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
        List<String> categories = Optional.ofNullable(filters.categoria())
                .map(list -> list.stream().map(Enum::name).toList())
                .orElse(null);
                
        List<String> statuses = Optional.ofNullable(filters.status())
                .map(list -> list.stream().map(Enum::name).toList())
                .orElse(null);

        return eventoJpaRepository.findAllWithFilters(
                filters.dataInicio(),
                filters.dataFim(),
                filters.organizacaoId(),
                filters.projetoId(),
                filters.envolvidoId(),
                categories,
                statuses,
                pageable)
                .map(eventoMapper::toResponse);
    }
}
