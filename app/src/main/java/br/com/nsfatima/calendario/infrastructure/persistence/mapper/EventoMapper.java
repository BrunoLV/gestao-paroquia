package br.com.nsfatima.calendario.infrastructure.persistence.mapper;

import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.CreateEventoRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.api.dto.evento.UpdateEventoRequest;
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
        entity.setProjetoId(request.projetoId());
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
                entity.getProjetoId(),
                entity.getInicioUtc(),
                entity.getFimUtc(),
                status,
                entity.getConflictState(),
                entity.getConflictReason());
    }

    public void applyPatch(EventoEntity entity, UpdateEventoRequest request, EventoStatusInput status) {
        if (request.titulo() != null) {
            entity.setTitulo(request.titulo());
        }
        if (request.descricao() != null) {
            entity.setDescricao(request.descricao());
        }
        if (request.inicio() != null) {
            entity.setInicioUtc(request.inicio());
        }
        if (request.fim() != null) {
            entity.setFimUtc(request.fim());
        }
        if (request.canceladoMotivo() != null) {
            entity.setCanceladoMotivo(request.canceladoMotivo());
        }
        if (request.adicionadoExtraJustificativa() != null) {
            entity.setAdicionadoExtraJustificativa(request.adicionadoExtraJustificativa());
        }
        if (request.organizacaoResponsavelId() != null) {
            entity.setOrganizacaoResponsavelId(request.organizacaoResponsavelId());
        }
        if (request.projetoId() != null) {
            entity.setProjetoId(request.projetoId());
        }
        entity.setStatus(status.name());
    }
}
