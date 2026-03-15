package br.com.nsfatima.calendario.infrastructure.persistence.mapper;

import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.CreateEventoRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.calendario.domain.type.EventoStatusResponse;
import br.com.nsfatima.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import org.springframework.stereotype.Component;

@Component
public class EventoMapper {

    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;

    public EventoMapper(LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher) {
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
    }

    public EventoEntity toNewEntity(CreateEventoRequest request, EventoStatusInput status) {
        EventoEntity entity = new EventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo(request.titulo());
        entity.setDescricao(request.descricao());
        entity.setOrganizacaoResponsavelId(request.organizacaoResponsavelId());
        entity.setInicioUtc(request.inicio());
        entity.setFimUtc(request.fim());
        entity.setStatus(status.name());
        entity.setAdicionadoExtraJustificativa(request.adicionadoExtraJustificativa());
        return entity;
    }

    public EventoResponse toResponse(EventoEntity entity) {
        EventoStatusResponse status = EventoStatusResponse.fromStoredValue(
                entity.getStatus(),
                legacyEnumInconsistencyPublisher,
                entity.getId() == null ? "<null>" : entity.getId().toString());

        return new EventoResponse(
                entity.getId(),
                entity.getTitulo(),
                entity.getDescricao(),
                entity.getOrganizacaoResponsavelId(),
                entity.getInicioUtc(),
                entity.getFimUtc(),
                status,
                entity.getConflictState(),
                entity.getConflictReason());
    }
}
