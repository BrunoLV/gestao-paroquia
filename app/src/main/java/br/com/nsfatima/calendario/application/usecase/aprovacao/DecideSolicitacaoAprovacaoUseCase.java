package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionRequest;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionResponse;
import br.com.nsfatima.calendario.api.dto.evento.EventoCanceladoResponse;
import br.com.nsfatima.calendario.application.usecase.evento.CancelEventoUseCase;
import br.com.nsfatima.calendario.domain.service.EventoCancelamentoAuthorizationService;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.calendario.infrastructure.security.UsuarioDetails;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DecideSolicitacaoAprovacaoUseCase {

    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final EventoJpaRepository eventoJpaRepository;
    private final EventoActorContextResolver eventoActorContextResolver;
    private final EventoCancelamentoAuthorizationService eventoCancelamentoAuthorizationService;
    private final ValidateAprovacaoUseCase validateAprovacaoUseCase;
    private final CancelEventoUseCase cancelEventoUseCase;
    private final EventoAuditPublisher eventoAuditPublisher;
    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;

    public DecideSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoJpaRepository eventoJpaRepository,
            EventoActorContextResolver eventoActorContextResolver,
            EventoCancelamentoAuthorizationService eventoCancelamentoAuthorizationService,
            ValidateAprovacaoUseCase validateAprovacaoUseCase,
            CancelEventoUseCase cancelEventoUseCase,
            EventoAuditPublisher eventoAuditPublisher,
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.eventoJpaRepository = eventoJpaRepository;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.eventoCancelamentoAuthorizationService = eventoCancelamentoAuthorizationService;
        this.validateAprovacaoUseCase = validateAprovacaoUseCase;
        this.cancelEventoUseCase = cancelEventoUseCase;
        this.eventoAuditPublisher = eventoAuditPublisher;
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
    }

    @Transactional(noRollbackFor = ApprovalExecutionFailedException.class)
    @SuppressWarnings("null")
    public AprovacaoDecisionResponse decide(UUID aprovacaoId, AprovacaoDecisionRequest request) {
        AprovacaoEntity aprovacao = aprovacaoJpaRepository.findById(aprovacaoId)
                .orElseThrow(() -> new ApprovalNotFoundException("Approval not found: " + aprovacaoId));

        if (!"PENDENTE".equalsIgnoreCase(aprovacao.getStatus())) {
            throw new ApprovalAlreadyDecidedException("Approval already decided: " + aprovacaoId);
        }

        EventoActorContext actorContext = eventoActorContextResolver.resolveRequired();
        eventoCancelamentoAuthorizationService.assertCanDecideCancellation(actorContext);
        validateAprovacaoUseCase.validateCancellationDecisionRole(
                actorContext.role(),
                actorContext.organizationType());

        String decision = normalizeDecision(request.status());
        aprovacao.setStatus(decision);
        aprovacao.setDecididoEmUtc(Instant.now());
        aprovacao.setDecisionObservacao(request.observacao());
        aprovacao.setAprovadorId(resolveUsuarioId().toString());
        aprovacao.setAprovadorPapel(resolveApproverRole(actorContext));

        String currentEventStatus = eventoJpaRepository.findStatusByIdNoLock(aprovacao.getEventoId()).orElse(null);

        if ("REPROVADA".equals(decision)) {
            aprovacaoJpaRepository.save(aprovacao);
            eventoAuditPublisher.publishCancellationRejected(
                    actorContext.actor(),
                    aprovacao.getId().toString(),
                    aprovacao.getEventoId().toString());
            cadastroEventoMetricsPublisher.publishCancellationFlow("APPROVAL_DECISION", "REJECTED");
            return new AprovacaoDecisionResponse(
                    aprovacao.getId(),
                    aprovacao.getStatus(),
                    new AprovacaoDecisionResponse.ActionExecution(
                            "REJECTED",
                            aprovacao.getEventoId(),
                            currentEventStatus,
                            null));
        }

        try {
            EventoCanceladoResponse response = cancelEventoUseCase.executeApprovedCancellation(
                    aprovacao.getEventoId(),
                    aprovacao.getMotivoCancelamentoSnapshot(),
                    actorContext.actor(),
                    resolveUsuarioId());
            aprovacao.setExecutadoEmUtc(Instant.now());
            aprovacaoJpaRepository.save(aprovacao);
            eventoAuditPublisher.publishCancellationExecuted(
                    actorContext.actor(),
                    aprovacao.getId().toString(),
                    aprovacao.getEventoId().toString());
            cadastroEventoMetricsPublisher.publishCancellationFlow("APPROVAL_DECISION", "EXECUTED");
            return new AprovacaoDecisionResponse(
                    aprovacao.getId(),
                    aprovacao.getStatus(),
                    new AprovacaoDecisionResponse.ActionExecution(
                            "EXECUTED",
                            response.id(),
                            response.status(),
                            null));
        } catch (RuntimeException ex) {
            aprovacaoJpaRepository.save(aprovacao);
            eventoAuditPublisher.publishCancellationExecutionFailed(
                    actorContext.actor(),
                    aprovacao.getId().toString(),
                    aprovacao.getEventoId().toString(),
                    ex.getClass().getSimpleName());
            cadastroEventoMetricsPublisher.publishCancellationFlow("APPROVAL_DECISION", "FAILED");
            throw new ApprovalExecutionFailedException(
                    "Approved request could not be executed automatically: " + ex.getMessage());
        }
    }

    private String normalizeDecision(String status) {
        return status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
    }

    private UUID resolveUsuarioId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails) {
            return usuarioDetails.getUsuarioId();
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    private String resolveApproverRole(EventoActorContext actorContext) {
        String role = actorContext.role() == null ? "" : actorContext.role().trim().toLowerCase(Locale.ROOT);
        String organizationType = actorContext.organizationType() == null
                ? ""
                : actorContext.organizationType().trim().toLowerCase(Locale.ROOT);
        if ("paroco".equals(role)) {
            return "paroco";
        }
        if ("conselho".equals(organizationType) && "vice-coordenador".equals(role)) {
            return "conselho-vice-coordenador";
        }
        return "conselho-coordenador";
    }
}
