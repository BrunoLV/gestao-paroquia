package br.com.nsfatima.calendario.application.usecase.projeto;

import br.com.nsfatima.calendario.api.dto.projeto.ProjetoResponse;
import java.util.UUID;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.observability.ProjetoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProjetoUseCase {

    private final ProjetoEventoJpaRepository repository;
    private final ProjetoMapper mapper;
    private final ProjetoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;

    public CreateProjetoUseCase(
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
    public ProjetoResponse create(String nome, String descricao) {
        ProjetoEventoEntity entity = new ProjetoEventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setNome(nome);
        entity.setDescricao(descricao);
        
        ProjetoEventoEntity saved = repository.save(entity);
        
        String actor = actorContextResolver.resolveRequired().actor();
        auditPublisher.publishCreateSuccess(actor, saved.getId().toString());
        
        return mapper.toResponse(saved);
    }
}
