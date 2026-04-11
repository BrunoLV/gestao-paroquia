package br.com.nsfatima.calendario.application.usecase.aprovacao;

import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoResponse;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoResponse;
import br.com.nsfatima.calendario.domain.policy.AuthorizationPolicy;
import br.com.nsfatima.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSolicitacaoAprovacaoUseCase {

    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;
    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final EventoActorContextResolver eventoActorContextResolver;

    @Autowired
    public CreateSolicitacaoAprovacaoUseCase(
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher,
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoActorContextResolver eventoActorContextResolver) {
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.eventoActorContextResolver = eventoActorContextResolver;
    }

    public CreateSolicitacaoAprovacaoUseCase(
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher,
            AprovacaoJpaRepository aprovacaoJpaRepository) {
        this(
                legacyEnumInconsistencyPublisher,
                aprovacaoJpaRepository,
                new EventoActorContextResolver(new AuthorizationPolicy()));
    }

    @Transactional
    public AprovacaoResponse create(UUID eventoId, TipoSolicitacaoInput tipoSolicitacao) {
        String aprovadorPapel = resolveAprovadorPapel();
        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setEventoId(eventoId);
        entity.setTipoSolicitacao(tipoSolicitacao.name());
        entity.setAprovadorPapel(aprovadorPapel);
        entity.setStatus("PENDENTE");
        entity.setCriadoEmUtc(Instant.now());
        entity.setDecididoEmUtc(null);
        aprovacaoJpaRepository.save(entity);

        return new AprovacaoResponse(
                entity.getId(),
                eventoId,
                TipoSolicitacaoResponse.fromStoredValue(
                        tipoSolicitacao.name(),
                        legacyEnumInconsistencyPublisher,
                        eventoId.toString()),
                entity.getStatus());
    }

    private String resolveAprovadorPapel() {
        EventoActorContext actorContext;
        try {
            actorContext = eventoActorContextResolver.resolveRequired();
        } catch (RuntimeException ex) {
            return "conselho-coordenador";
        }

        String normalizedRole = actorContext.role() == null ? "" : actorContext.role().trim().toLowerCase();
        String normalizedOrgType = actorContext.organizationType() == null
                ? ""
                : actorContext.organizationType().trim().toLowerCase();
        if ("paroco".equals(normalizedRole)) {
            return "paroco";
        }
        if ("conselho".equals(normalizedOrgType) && "vice-coordenador".equals(normalizedRole)) {
            return "conselho-vice-coordenador";
        }
        return "conselho-coordenador";
    }
}
