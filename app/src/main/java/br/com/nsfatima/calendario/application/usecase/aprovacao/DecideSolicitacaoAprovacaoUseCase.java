package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionRequest;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionResponse;
import br.com.nsfatima.calendario.api.dto.evento.EventoCanceladoResponse;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.application.usecase.evento.CancelEventoUseCase;
import br.com.nsfatima.calendario.application.usecase.evento.CreateEventoUseCase;
import br.com.nsfatima.calendario.application.usecase.evento.UpdateEventoUseCase;
import br.com.nsfatima.calendario.domain.service.EventoCancelamentoAuthorizationService;
import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.calendario.infrastructure.security.UsuarioDetails;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
    private final CreateEventoUseCase createEventoUseCase;
    private final UpdateEventoUseCase updateEventoUseCase;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;
    private final EventoAuditPublisher eventoAuditPublisher;
    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;

    public DecideSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoJpaRepository eventoJpaRepository,
            EventoActorContextResolver eventoActorContextResolver,
            EventoCancelamentoAuthorizationService eventoCancelamentoAuthorizationService,
            ValidateAprovacaoUseCase validateAprovacaoUseCase,
            CancelEventoUseCase cancelEventoUseCase,
            CreateEventoUseCase createEventoUseCase,
            UpdateEventoUseCase updateEventoUseCase,
            ApprovalActionPayloadMapper approvalActionPayloadMapper,
            EventoAuditPublisher eventoAuditPublisher,
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.eventoJpaRepository = eventoJpaRepository;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.eventoCancelamentoAuthorizationService = eventoCancelamentoAuthorizationService;
        this.validateAprovacaoUseCase = validateAprovacaoUseCase;
        this.cancelEventoUseCase = cancelEventoUseCase;
        this.createEventoUseCase = createEventoUseCase;
        this.updateEventoUseCase = updateEventoUseCase;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
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
        eventoCancelamentoAuthorizationService.assertCanDecideApproval(actorContext);
        validateAprovacaoUseCase.validateApprovalDecisionRole(
                actorContext.role(),
                actorContext.organizationType());

        String decision = normalizeDecision(request.status());
        aprovacao.setStatus(decision);
        aprovacao.setDecididoEmUtc(Instant.now());
        aprovacao.setDecisionObservacao(request.observacao());
        aprovacao.setAprovadorId(resolveUsuarioId().toString());
        aprovacao.setAprovadorPapel(resolveApproverRole(actorContext));

        if ("REPROVADA".equals(decision)) {
            Objects.requireNonNull(aprovacaoJpaRepository.save(aprovacao));
            eventoAuditPublisher.publishApprovalDecision(
                    actorContext.actor(),
                    aprovacao,
                    "rejected",
                    Map.of("decisionStatus", "REPROVADA"));
            cadastroEventoMetricsPublisher.publishApprovalFlow(
                    aprovacao.getTipoSolicitacao() == null ? "UNKNOWN" : aprovacao.getTipoSolicitacao(),
                    "REJECTED");
            return new AprovacaoDecisionResponse(
                    aprovacao.getId(),
                    aprovacao.getStatus(),
                    new AprovacaoDecisionResponse.ActionExecution(
                            "REJECTED",
                            aprovacao.getEventoId(),
                            fetchCurrentEventStatus(aprovacao.getEventoId()),
                            null));
        }

        String tipo = aprovacao.getTipoSolicitacao() == null ? ""
                : aprovacao.getTipoSolicitacao().trim().toUpperCase(Locale.ROOT);
        return switch (tipo) {
            case "CANCELAMENTO" -> decideCancellationApproved(aprovacao, actorContext);
            case "CRIACAO_EVENTO" -> decideCreateApproved(aprovacao, actorContext);
            case "EDICAO_EVENTO" -> decideUpdateApproved(aprovacao, actorContext);
            default -> throw new ApprovalExecutionFailedException(
                    "Unsupported approval type for automatic execution: " + tipo);
        };
    }

    private AprovacaoDecisionResponse decideCancellationApproved(AprovacaoEntity aprovacao,
            EventoActorContext actorContext) {
        try {
            EventoCanceladoResponse response = cancelEventoUseCase.executeApprovedCancellation(
                    aprovacao.getEventoId(),
                    aprovacao.getMotivoCancelamentoSnapshot(),
                    actorContext.actor(),
                    resolveUsuarioId());
            Instant executedAt = Instant.now();
            aprovacao.setExecutadoEmUtc(executedAt);
            Objects.requireNonNull(aprovacaoJpaRepository.save(aprovacao));
            eventoAuditPublisher.publishCancellationExecuted(
                    actorContext.actor(),
                    aprovacao.getId().toString(),
                    aprovacao.getEventoId().toString());
            cadastroEventoMetricsPublisher.publishCancellationFlow("APPROVAL_DECISION", "EXECUTED");
            if (aprovacao.getDecididoEmUtc() != null) {
                cadastroEventoMetricsPublisher.publishApprovalExecutionLatency(
                        "CANCELAMENTO",
                        java.time.Duration.between(aprovacao.getDecididoEmUtc(), executedAt));
            }
            return new AprovacaoDecisionResponse(
                    aprovacao.getId(),
                    aprovacao.getStatus(),
                    new AprovacaoDecisionResponse.ActionExecution("EXECUTED", response.id(), response.status(), null));
        } catch (RuntimeException ex) {
            Objects.requireNonNull(aprovacaoJpaRepository.save(aprovacao));
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

    private AprovacaoDecisionResponse decideCreateApproved(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        try {
            Map<String, Object> payload = approvalActionPayloadMapper.toMap(aprovacao.getActionPayloadJson());
            String idempotencyKey = String
                    .valueOf(payload.getOrDefault("idempotencyKey", "approval-exec-" + aprovacao.getId()));
            EventoResponse response = createEventoUseCase.executeApprovedCreation(
                    createEventoUseCase.restoreFromApprovalPayload(payload),
                    idempotencyKey);
            aprovacao.setEventoId(response.id());
            Instant executedAt = Instant.now();
            aprovacao.setExecutadoEmUtc(executedAt);
            Objects.requireNonNull(aprovacaoJpaRepository.save(aprovacao));
            eventoAuditPublisher.publishApprovalDecision(
                    actorContext.actor(),
                    aprovacao,
                    "executed",
                    Map.of("eventoId", response.id().toString(), "decisionStatus", "APROVADA"));
            cadastroEventoMetricsPublisher.publishApprovalFlow("CRIACAO_EVENTO", "EXECUTED");
            if (aprovacao.getDecididoEmUtc() != null) {
                cadastroEventoMetricsPublisher.publishApprovalExecutionLatency(
                        "CRIACAO_EVENTO",
                        java.time.Duration.between(aprovacao.getDecididoEmUtc(), executedAt));
            }
            return new AprovacaoDecisionResponse(
                    aprovacao.getId(),
                    aprovacao.getStatus(),
                    new AprovacaoDecisionResponse.ActionExecution("EXECUTED", response.id(),
                            response.status() == null ? null : response.status().name(), null));
        } catch (RuntimeException ex) {
            Objects.requireNonNull(aprovacaoJpaRepository.save(aprovacao));
            eventoAuditPublisher.publishApprovalDecision(
                    actorContext.actor(),
                    aprovacao,
                    "failed",
                    Map.of("error", ex.getClass().getSimpleName(), "decisionStatus", "APROVADA"));
            cadastroEventoMetricsPublisher.publishApprovalFlow("CRIACAO_EVENTO", "FAILED");
            throw new ApprovalExecutionFailedException(
                    "Approved request could not be executed automatically: " + ex.getMessage());
        }
    }

    private AprovacaoDecisionResponse decideUpdateApproved(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        try {
            Map<String, Object> payload = approvalActionPayloadMapper.toMap(aprovacao.getActionPayloadJson());
            UUID eventoId = UUID.fromString(String.valueOf(payload.get("eventoId")));
            EventoResponse response = updateEventoUseCase.executeApprovedUpdate(
                    eventoId,
                    updateEventoUseCase.restoreFromApprovalPayload(payload));
            Instant executedAt = Instant.now();
            aprovacao.setExecutadoEmUtc(executedAt);
            Objects.requireNonNull(aprovacaoJpaRepository.save(aprovacao));
            eventoAuditPublisher.publishApprovalDecision(
                    actorContext.actor(),
                    aprovacao,
                    "executed",
                    Map.of("eventoId", eventoId.toString(), "decisionStatus", "APROVADA"));
            cadastroEventoMetricsPublisher.publishApprovalFlow("EDICAO_EVENTO", "EXECUTED");
            if (aprovacao.getDecididoEmUtc() != null) {
                cadastroEventoMetricsPublisher.publishApprovalExecutionLatency(
                        "EDICAO_EVENTO",
                        java.time.Duration.between(aprovacao.getDecididoEmUtc(), executedAt));
            }
            return new AprovacaoDecisionResponse(
                    aprovacao.getId(),
                    aprovacao.getStatus(),
                    new AprovacaoDecisionResponse.ActionExecution(
                            "EXECUTED",
                            response.id(),
                            response.status() == null ? null : response.status().name(),
                            null));
        } catch (RuntimeException ex) {
            Objects.requireNonNull(aprovacaoJpaRepository.save(aprovacao));
            eventoAuditPublisher.publishApprovalDecision(
                    actorContext.actor(),
                    aprovacao,
                    "failed",
                    Map.of("error", ex.getClass().getSimpleName(), "decisionStatus", "APROVADA"));
            cadastroEventoMetricsPublisher.publishApprovalFlow("EDICAO_EVENTO", "FAILED");
            throw new ApprovalExecutionFailedException(
                    "Approved update request could not be executed automatically: " + ex.getMessage());
        }
    }

    private String fetchCurrentEventStatus(UUID eventoId) {
        if (eventoId == null) {
            return null;
        }
        return eventoJpaRepository.findStatusByIdNoLock(eventoId).orElse(null);
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
