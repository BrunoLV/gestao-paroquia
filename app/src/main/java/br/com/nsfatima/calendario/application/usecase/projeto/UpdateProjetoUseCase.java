package br.com.nsfatima.calendario.application.usecase.projeto;

import br.com.nsfatima.calendario.api.dto.projeto.ProjetoPatchRequest;
import br.com.nsfatima.calendario.api.dto.projeto.ProjetoResponse;
import br.com.nsfatima.calendario.domain.exception.ProjetoNotFoundException;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.observability.ProjetoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateProjetoUseCase {

    private final ProjetoEventoJpaRepository repository;
    private final ProjetoMapper mapper;
    private final ProjetoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;

    public UpdateProjetoUseCase(
            ProjetoEventoJpaRepository repository,
            ProjetoMapper mapper,
            ProjetoAuditPublisher auditPublisher,
            EventoActorContextResolver actorContextResolver) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
    }

    @Transactional
    public ProjetoResponse execute(UUID id, ProjetoPatchRequest request) {
        ProjetoEventoEntity entity = repository.findById(id)
                .orElseThrow(() -> new ProjetoNotFoundException(id));

        if (request.nome() != null) {
            entity.setNome(request.nome());
        }
        if (request.descricao() != null) {
            entity.setDescricao(request.descricao());
        }

        ProjetoEventoEntity saved = repository.save(entity);
        
        String actor = actorContextResolver.resolveRequired().actor();
        auditPublisher.publishPatchSuccess(actor, saved.getId().toString());
        
        return mapper.toResponse(saved, true);
    }
}
