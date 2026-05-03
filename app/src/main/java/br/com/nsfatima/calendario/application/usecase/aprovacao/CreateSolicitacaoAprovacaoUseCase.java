package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoResponse;
import br.com.nsfatima.calendario.domain.policy.AuthorizationPolicy;
import br.com.nsfatima.calendario.domain.type.AprovacaoStatus;
import br.com.nsfatima.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoResponse;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSolicitacaoAprovacaoUseCase {

    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;
    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final EventoActorContextResolver eventoActorContextResolver;
    private final EventoAuditPublisher eventoAuditPublisher;

    @Autowired
    public CreateSolicitacaoAprovacaoUseCase(
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher,
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoActorContextResolver eventoActorContextResolver,
            EventoAuditPublisher eventoAuditPublisher) {
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.eventoAuditPublisher = eventoAuditPublisher;
    }

    public CreateSolicitacaoAprovacaoUseCase(
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher,
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoAuditPublisher eventoAuditPublisher) {
        this(
                legacyEnumInconsistencyPublisher,
                aprovacaoJpaRepository,
                new EventoActorContextResolver(new AuthorizationPolicy()),
                eventoAuditPublisher);
    }

    public CreateSolicitacaoAprovacaoUseCase(
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher,
            AprovacaoJpaRepository aprovacaoJpaRepository) {
        this(
                legacyEnumInconsistencyPublisher,
                aprovacaoJpaRepository,
                new EventoActorContextResolver(new AuthorizationPolicy()),
                null); // Audit publisher will be null, we should handle it in create()
    }

    @Transactional
    public AprovacaoResponse create(UUID eventoId, TipoSolicitacaoInput tipoSolicitacao) {
        AprovadorPapel aprovadorPapel = resolveAprovadorPapel();
        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setEventoId(eventoId);
        entity.setTipoSolicitacao(tipoSolicitacao.name());
        entity.setAprovadorPapel(aprovadorPapel);
        entity.setStatus(AprovacaoStatus.PENDENTE);
        entity.setCriadoEmUtc(Instant.now());
        entity.setDecididoEmUtc(null);
        aprovacaoJpaRepository.save(entity);

        if (eventoAuditPublisher != null) {
            eventoAuditPublisher.publishCreatePending(
                    resolveActor(),
                    entity.getId().toString(),
                    "N/A");
        }

        return new AprovacaoResponse(
                entity.getId(),
                eventoId,
                TipoSolicitacaoResponse.fromStoredValue(
                        tipoSolicitacao.name(),
                        legacyEnumInconsistencyPublisher,
                        entity.getId().toString()),
                entity.getStatus(),
                entity.getAprovadorPapel(),
                entity.getCriadoEmUtc(),
                entity.getDecididoEmUtc(),
                entity.getSolicitanteId(),
                entity.getAprovadorId());
    }

    private String resolveActor() {
        try {
            return eventoActorContextResolver.resolveRequired().actor();
        } catch (RuntimeException ex) {
            return "system";
        }
    }

    private AprovadorPapel resolveAprovadorPapel() {
        EventoActorContext actorContext;
        try {
            actorContext = eventoActorContextResolver.resolveRequired();
        } catch (RuntimeException ex) {
            return AprovadorPapel.CONSELHO_COORDENADOR;
        }

        return AprovadorPapel.resolveForApproval(actorContext.role(), actorContext.organizationType());
    }
}
