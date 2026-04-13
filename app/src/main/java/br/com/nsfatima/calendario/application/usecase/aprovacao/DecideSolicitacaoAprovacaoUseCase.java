package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionRequest;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionResponse;
import br.com.nsfatima.calendario.api.dto.evento.EventoCanceladoResponse;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.application.usecase.evento.CancelEventoUseCase;
import br.com.nsfatima.calendario.application.usecase.evento.CreateEventoUseCase;
import br.com.nsfatima.calendario.application.usecase.evento.UpdateEventoUseCase;
import br.com.nsfatima.calendario.domain.service.EventoCancelamentoAuthorizationService;
import br.com.nsfatima.calendario.domain.type.AprovacaoStatus;
import br.com.nsfatima.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
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

                if (aprovacao.getStatusEnum() != AprovacaoStatus.PENDENTE) {
                        throw new ApprovalAlreadyDecidedException("Approval already decided: " + aprovacaoId);
                }

                EventoActorContext actorContext = eventoActorContextResolver.resolveRequired();
                eventoCancelamentoAuthorizationService.assertCanDecideApproval(actorContext);
                validateAprovacaoUseCase.validateApprovalDecisionRole(
                                actorContext.role(),
                                actorContext.organizationType());

                AprovacaoStatus decision = request.status();
                aprovacao.setStatus(decision);
                aprovacao.setDecididoEmUtc(Instant.now());
                aprovacao.setDecisionObservacao(request.observacao());
                aprovacao.setAprovadorId(resolveUsuarioId().toString());
                aprovacao.setAprovadorPapel(resolveApproverRole(actorContext));

                if (decision == AprovacaoStatus.REPROVADA) {
                        aprovacaoJpaRepository.save(aprovacao);
                        eventoAuditPublisher.publishApprovalDecision(
                                        actorContext.actor(),
                                        aprovacao,
                                        "rejected",
                                        Map.of("decisionStatus", AprovacaoStatus.REPROVADA.name()));
                        cadastroEventoMetricsPublisher.publishApprovalFlow(
                                        aprovacao.getTipoSolicitacao() == null ? "UNKNOWN"
                                                        : aprovacao.getTipoSolicitacao(),
                                        "REJECTED");
                        return new AprovacaoDecisionResponse(
                                        aprovacao.getId(),
                                        aprovacao.getStatusEnum().name(),
                                        new AprovacaoDecisionResponse.ActionExecution(
                                                        "REJECTED",
                                                        aprovacao.getEventoId(),
                                                        fetchCurrentEventStatus(aprovacao.getEventoId()),
                                                        null));
                }

                TipoSolicitacaoInput tipo = TipoSolicitacaoInput.fromJson(aprovacao.getTipoSolicitacao());
                return switch (tipo) {
                        case CANCELAMENTO -> decideCancellationApproved(aprovacao, actorContext);
                        case CRIACAO_EVENTO -> decideCreateApproved(aprovacao, actorContext);
                        case EDICAO_EVENTO -> decideUpdateApproved(aprovacao, actorContext);
                        default -> throw new ApprovalExecutionFailedException(
                                        "Unsupported approval type for automatic execution: "
                                                        + aprovacao.getTipoSolicitacao());
                };
        }

        @SuppressWarnings("null")
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
                        aprovacaoJpaRepository.save(aprovacao);
                        eventoAuditPublisher.publishCancellationExecuted(
                                        actorContext.actor(),
                                        aprovacao.getId().toString(),
                                        aprovacao.getEventoId().toString());
                        cadastroEventoMetricsPublisher.publishCancellationFlow("APPROVAL_DECISION", "EXECUTED");
                        if (aprovacao.getDecididoEmUtc() != null) {
                                cadastroEventoMetricsPublisher.publishApprovalExecutionLatency(
                                                TipoSolicitacaoInput.CANCELAMENTO.name(),
                                                java.time.Duration.between(aprovacao.getDecididoEmUtc(), executedAt));
                        }
                        return new AprovacaoDecisionResponse(
                                        aprovacao.getId(),
                                        aprovacao.getStatusEnum().name(),
                                        new AprovacaoDecisionResponse.ActionExecution("EXECUTED", response.id(),
                                                        response.status(), null));
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

        @SuppressWarnings("null")
        private AprovacaoDecisionResponse decideCreateApproved(AprovacaoEntity aprovacao,
                        EventoActorContext actorContext) {
                try {
                        ApprovalActionPayload payload = approvalActionPayloadMapper
                                        .toPayload(aprovacao.getActionPayloadJson());
                        String idempotencyKey = payload.idempotencyKey() == null
                                        ? "approval-exec-" + aprovacao.getId()
                                        : payload.idempotencyKey();
                        EventoResponse response = createEventoUseCase.executeApprovedCreation(
                                        createEventoUseCase.restoreFromApprovalPayload(payload),
                                        idempotencyKey);
                        aprovacao.setEventoId(response.id());
                        Instant executedAt = Instant.now();
                        aprovacao.setExecutadoEmUtc(executedAt);
                        aprovacaoJpaRepository.save(aprovacao);
                        eventoAuditPublisher.publishApprovalDecision(
                                        actorContext.actor(),
                                        aprovacao,
                                        "executed",
                                        Map.of("eventoId", response.id().toString(), "decisionStatus",
                                                        AprovacaoStatus.APROVADA.name()));
                        cadastroEventoMetricsPublisher.publishApprovalFlow(TipoSolicitacaoInput.CRIACAO_EVENTO.name(),
                                        "EXECUTED");
                        if (aprovacao.getDecididoEmUtc() != null) {
                                cadastroEventoMetricsPublisher.publishApprovalExecutionLatency(
                                                TipoSolicitacaoInput.CRIACAO_EVENTO.name(),
                                                java.time.Duration.between(aprovacao.getDecididoEmUtc(), executedAt));
                        }
                        return new AprovacaoDecisionResponse(
                                        aprovacao.getId(),
                                        aprovacao.getStatusEnum().name(),
                                        new AprovacaoDecisionResponse.ActionExecution("EXECUTED", response.id(),
                                                        response.status() == null ? null : response.status().name(),
                                                        null));
                } catch (RuntimeException ex) {
                        aprovacaoJpaRepository.save(aprovacao);
                        eventoAuditPublisher.publishApprovalDecision(
                                        actorContext.actor(),
                                        aprovacao,
                                        "failed",
                                        Map.of("error", ex.getClass().getSimpleName(), "decisionStatus",
                                                        AprovacaoStatus.APROVADA.name()));
                        cadastroEventoMetricsPublisher.publishApprovalFlow(TipoSolicitacaoInput.CRIACAO_EVENTO.name(),
                                        "FAILED");
                        throw new ApprovalExecutionFailedException(
                                        "Approved request could not be executed automatically: " + ex.getMessage());
                }
        }

        @SuppressWarnings("null")
        private AprovacaoDecisionResponse decideUpdateApproved(AprovacaoEntity aprovacao,
                        EventoActorContext actorContext) {
                try {
                        ApprovalActionPayload payload = approvalActionPayloadMapper
                                        .toPayload(aprovacao.getActionPayloadJson());
                        UUID eventoId = payload.eventoId();
                        if (eventoId == null) {
                                throw new IllegalArgumentException("eventoId must be present in approval payload");
                        }
                        EventoResponse response = updateEventoUseCase.executeApprovedUpdate(
                                        eventoId,
                                        updateEventoUseCase.restoreFromApprovalPayload(payload));
                        Instant executedAt = Instant.now();
                        aprovacao.setExecutadoEmUtc(executedAt);
                        aprovacaoJpaRepository.save(aprovacao);
                        eventoAuditPublisher.publishApprovalDecision(
                                        actorContext.actor(),
                                        aprovacao,
                                        "executed",
                                        Map.of("eventoId", eventoId.toString(), "decisionStatus",
                                                        AprovacaoStatus.APROVADA.name()));
                        cadastroEventoMetricsPublisher.publishApprovalFlow(TipoSolicitacaoInput.EDICAO_EVENTO.name(),
                                        "EXECUTED");
                        if (aprovacao.getDecididoEmUtc() != null) {
                                cadastroEventoMetricsPublisher.publishApprovalExecutionLatency(
                                                TipoSolicitacaoInput.EDICAO_EVENTO.name(),
                                                java.time.Duration.between(aprovacao.getDecididoEmUtc(), executedAt));
                        }
                        return new AprovacaoDecisionResponse(
                                        aprovacao.getId(),
                                        aprovacao.getStatusEnum().name(),
                                        new AprovacaoDecisionResponse.ActionExecution(
                                                        "EXECUTED",
                                                        response.id(),
                                                        response.status() == null ? null : response.status().name(),
                                                        null));
                } catch (RuntimeException ex) {
                        aprovacaoJpaRepository.save(aprovacao);
                        eventoAuditPublisher.publishApprovalDecision(
                                        actorContext.actor(),
                                        aprovacao,
                                        "failed",
                                        Map.of("error", ex.getClass().getSimpleName(), "decisionStatus",
                                                        AprovacaoStatus.APROVADA.name()));
                        cadastroEventoMetricsPublisher.publishApprovalFlow(TipoSolicitacaoInput.EDICAO_EVENTO.name(),
                                        "FAILED");
                        throw new ApprovalExecutionFailedException(
                                        "Approved update request could not be executed automatically: "
                                                        + ex.getMessage());
                }
        }

        private String fetchCurrentEventStatus(UUID eventoId) {
                if (eventoId == null) {
                        return null;
                }
                return eventoJpaRepository.findStatusByIdNoLock(eventoId).orElse(null);
        }

        private UUID resolveUsuarioId() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails) {
                        return usuarioDetails.getUsuarioId();
                }
                return UUID.fromString("00000000-0000-0000-0000-000000000001");
        }

        private AprovadorPapel resolveApproverRole(EventoActorContext actorContext) {
                return AprovadorPapel.resolveForApproval(actorContext.role(), actorContext.organizationType());
        }
}
