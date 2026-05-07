package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import br.com.nsfatima.gestao.calendario.api.dto.evento.EventoApprovalPendingResponse;
import br.com.nsfatima.gestao.calendario.api.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.gestao.calendario.domain.type.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEventoApprovalRequestUseCase {

    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;
    private final EventoActorContextResolver eventoActorContextResolver;
    private final EventoAuditPublisher eventoAuditPublisher;

    public UpdateEventoApprovalRequestUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            ApprovalActionPayloadMapper approvalActionPayloadMapper,
            EventoActorContextResolver eventoActorContextResolver,
            EventoAuditPublisher eventoAuditPublisher) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.eventoAuditPublisher = eventoAuditPublisher;
    }

    /**
     * Creates a new approval request for an event update.
     * 
     * Usage Example:
     * useCase.create(eventoId, updateRequest);
     */
    @Transactional
    public EventoApprovalPendingResponse create(UUID eventoId, UpdateEventoRequest request) {
        EventoActorContext actorContext = eventoActorContextResolver.resolveRequired();

        UUID approvalId = UUID.randomUUID();
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(approvalId);
        aprovacao.setEventoId(eventoId);
        aprovacao.setTipoSolicitacao(TipoSolicitacaoInput.EDICAO_EVENTO.name());
        aprovacao.setAprovadorPapel(resolveAprovadorPapel(actorContext));
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setSolicitanteId(actorContext.usuarioId().toString());
        aprovacao.setSolicitantePapel(actorContext.role());
        aprovacao.setSolicitanteTipoOrganizacao(actorContext.organizationType());
        aprovacao.setCorrelationId(approvalId.toString());
        aprovacao.setActionPayloadJson(approvalActionPayloadMapper.toJson(buildPayload(eventoId, request)));
        aprovacaoJpaRepository.save(aprovacao);

        publishAudit(actorContext, approvalId, eventoId);

        return new EventoApprovalPendingResponse(approvalId, AprovacaoStatus.PENDENTE.name(), "APPROVAL_PENDING");
    }

    private void publishAudit(EventoActorContext actorContext, UUID approvalId, UUID eventoId) {
        eventoAuditPublisher.publish(
                actorContext.actor(),
                "update",
                "evento",
                "pending",
                Map.of(
                        "solicitacaoAprovacaoId", approvalId.toString(),
                        "tipoSolicitacao", TipoSolicitacaoInput.EDICAO_EVENTO.name(),
                        "eventoId", eventoId.toString()));
    }

    private ApprovalActionPayload buildPayload(UUID eventoId, UpdateEventoRequest request) {
        return new ApprovalActionPayload(
                null,
                eventoId,
                request.titulo(),
                request.descricao(),
                request.categoria(),
                request.organizacaoResponsavelId(),
                request.projetoId(),
                request.inicio(),
                request.fim(),
                request.status(),
                request.adicionadoExtraJustificativa(),
                request.canceladoMotivo(),
                null,
                request.participantes());
    }

    private AprovadorPapel resolveAprovadorPapel(EventoActorContext actorContext) {
        return AprovadorPapel.resolveForApproval(actorContext.role(), actorContext.organizationType());
    }
}
