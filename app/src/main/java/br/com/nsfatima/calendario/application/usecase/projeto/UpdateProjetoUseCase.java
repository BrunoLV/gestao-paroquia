package br.com.nsfatima.calendario.application.usecase.projeto;

import br.com.nsfatima.calendario.api.dto.projeto.ProjetoPatchRequest;
import br.com.nsfatima.calendario.api.dto.projeto.ProjetoResponse;
import br.com.nsfatima.calendario.domain.exception.ProjetoNotFoundException;
import br.com.nsfatima.calendario.domain.service.ProjetoAuthorizationService;
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
    private final ProjetoAuthorizationService authorizationService;

    public UpdateProjetoUseCase(
            ProjetoEventoJpaRepository repository,
            ProjetoMapper mapper,
            ProjetoAuditPublisher auditPublisher,
            EventoActorContextResolver actorContextResolver,
            ProjetoAuthorizationService authorizationService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public ProjetoResponse execute(UUID id, ProjetoPatchRequest request) {
        ProjetoEventoEntity entity = repository.findById(id)
                .orElseThrow(() -> new ProjetoNotFoundException(id));

        var actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanEdit(actorContext, entity.getOrganizacaoResponsavelId());

        if (request.nome() != null) {
            entity.setNome(request.nome());
        }
        if (request.descricao() != null) {
            entity.setDescricao(request.descricao());
        }
        if (request.organizacaoResponsavelId() != null) {
            entity.setOrganizacaoResponsavelId(request.organizacaoResponsavelId());
        }
        if (request.inicio() != null) {
            entity.setInicioUtc(request.inicio());
        }
        if (request.fim() != null) {
            entity.setFimUtc(request.fim());
        }
        if (request.status() != null) {
            entity.setStatus(request.status());
        }

        if (entity.getFimUtc() != null && entity.getInicioUtc() != null && entity.getFimUtc().isBefore(entity.getInicioUtc())) {
            throw new IllegalArgumentException("Project end date cannot be before start date");
        }

        ProjetoEventoEntity saved = repository.save(entity);
        
        auditPublisher.publishPatchSuccess(actorContext.actor(), saved.getId().toString());
        
        return mapper.toResponse(saved, true);
    }
}
