package br.com.nsfatima.calendario.domain.service;

import br.com.nsfatima.calendario.api.dto.evento.EventoFiltroRequest;
import br.com.nsfatima.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.calendario.domain.type.PapelOrganizacional;
import br.com.nsfatima.calendario.domain.type.TipoOrganizacao;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventoService {

    private final EventoJpaRepository eventoJpaRepository;

    public EventoService(EventoJpaRepository eventoJpaRepository) {
        this.eventoJpaRepository = eventoJpaRepository;
    }

    public EventoEntity findById(UUID id, EventoActorContext actorContext) {
        EventoEntity evento = eventoJpaRepository.findById(id)
                .orElseThrow(() -> new EventoNotFoundException(id));

        assertCanView(actorContext, evento.getOrganizacaoResponsavelId());
        
        return evento;
    }

    public Page<EventoEntity> list(EventoFiltroRequest filters, Pageable pageable) {
        return eventoJpaRepository.findAllWithFilters(
                filters.start_date(),
                filters.end_date(),
                filters.organizacao_id(),
                filters.projeto_id(),
                pageable);
    }

    private void assertCanView(EventoActorContext actorContext, UUID organizacaoResponsavelId) {
        PapelOrganizacional role = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao orgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());

        // Clero pode ver tudo
        if (orgType == TipoOrganizacao.CLERO) {
            return;
        }

        // Conselho pode ver tudo
        if (orgType == TipoOrganizacao.CONSELHO) {
            return;
        }

        // Outros podem ver eventos da sua própria organização
        if (actorContext.organizationId() != null && actorContext.organizationId().equals(organizacaoResponsavelId)) {
            return;
        }

        throw new ForbiddenOperationException("Usuario nao tem permissao para visualizar este evento");
    }
}
