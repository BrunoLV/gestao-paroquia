package br.com.nsfatima.gestao.projeto.application.usecase;

import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoCreateRequest;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAuthorizationService;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.observability.ProjetoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProjetoUseCase {

    private final ProjetoEventoJpaRepository repository;
    private final ProjetoMapper mapper;
    private final ProjetoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;
    private final ProjetoAuthorizationService authorizationService;

    public CreateProjetoUseCase(
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
    public ProjetoResponse create(ProjetoCreateRequest request) {
        var actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanCreate(actorContext, request.organizacaoResponsavelId());

        if (request.fim().isBefore(request.inicio())) {
            throw new IllegalArgumentException("Project end date cannot be before start date");
        }

        ProjetoEventoEntity entity = new ProjetoEventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setNome(request.nome());
        entity.setDescricao(request.descricao());
        entity.setOrganizacaoResponsavelId(request.organizacaoResponsavelId());
        entity.setInicioUtc(request.inicio());
        entity.setFimUtc(request.fim());
        
        ProjetoEventoEntity saved = repository.save(entity);
        
        auditPublisher.publishCreateSuccess(actorContext.actor(), saved.getId().toString());
        
        return mapper.toResponse(saved);
    }
}
