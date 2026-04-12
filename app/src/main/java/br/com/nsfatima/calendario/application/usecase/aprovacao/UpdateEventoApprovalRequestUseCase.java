package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.api.dto.evento.EventoApprovalPendingResponse;
import br.com.nsfatima.calendario.api.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.calendario.domain.type.AprovacaoStatus;
import br.com.nsfatima.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.calendario.infrastructure.security.UsuarioDetails;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        aprovacao.setSolicitanteId(resolveUsuarioId().toString());
        aprovacao.setSolicitantePapel(actorContext.role());
        aprovacao.setSolicitanteTipoOrganizacao(actorContext.organizationType());
        aprovacao.setCorrelationId(approvalId.toString());
        aprovacao.setActionPayloadJson(approvalActionPayloadMapper.toJson(buildPayload(eventoId, request)));
        aprovacaoJpaRepository.save(aprovacao);

        eventoAuditPublisher.publish(
                actorContext.actor(),
                "update",
                "evento",
                "pending",
                Map.of(
                        "solicitacaoAprovacaoId", approvalId.toString(),
                        "tipoSolicitacao", TipoSolicitacaoInput.EDICAO_EVENTO.name(),
                        "eventoId", eventoId.toString()));

        return new EventoApprovalPendingResponse(approvalId, AprovacaoStatus.PENDENTE.name(), "APPROVAL_PENDING");
    }

    private ApprovalActionPayload buildPayload(UUID eventoId, UpdateEventoRequest request) {
        return new ApprovalActionPayload(
                null,
                eventoId,
                request.titulo(),
                request.descricao(),
                request.organizacaoResponsavelId(),
                request.inicio(),
                request.fim(),
                request.status(),
                request.adicionadoExtraJustificativa(),
                request.canceladoMotivo(),
                null,
                request.participantes());
    }

    private UUID resolveUsuarioId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails) {
            return usuarioDetails.getUsuarioId();
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    private AprovadorPapel resolveAprovadorPapel(EventoActorContext actorContext) {
        return AprovadorPapel.resolveForApproval(actorContext.role(), actorContext.organizationType());
    }
}
