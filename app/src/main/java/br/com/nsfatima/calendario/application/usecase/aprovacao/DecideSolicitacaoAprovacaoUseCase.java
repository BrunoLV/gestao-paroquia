package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionRequest;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionResponse;
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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core use case for processing decisions on pending approval requests.
 */
@Service
public class DecideSolicitacaoAprovacaoUseCase {

    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final EventoJpaRepository eventoJpaRepository;
    private final EventoActorContextResolver eventoActorContextResolver;
    private final EventoCancelamentoAuthorizationService eventoCancelamentoAuthorizationService;
    private final ValidateAprovacaoUseCase validateAprovacaoUseCase;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;
    private final EventoAuditPublisher eventoAuditPublisher;
    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;
    private final List<ApprovalExecutionStrategy> strategies;
    private final ApplicationEventPublisher eventPublisher;

    public DecideSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoJpaRepository eventoJpaRepository,
            EventoActorContextResolver eventoActorContextResolver,
            EventoCancelamentoAuthorizationService eventoCancelamentoAuthorizationService,
            ValidateAprovacaoUseCase validateAprovacaoUseCase,
            ApprovalActionPayloadMapper approvalActionPayloadMapper,
            EventoAuditPublisher eventoAuditPublisher,
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher,
            List<ApprovalExecutionStrategy> strategies,
            ApplicationEventPublisher eventPublisher) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.eventoJpaRepository = eventoJpaRepository;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.eventoCancelamentoAuthorizationService = eventoCancelamentoAuthorizationService;
        this.validateAprovacaoUseCase = validateAprovacaoUseCase;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
        this.eventoAuditPublisher = eventoAuditPublisher;
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
        this.strategies = strategies;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Processes a decision (APPROVE/REJECT) for a given approval request.
     * 
     * Usage Example:
     * useCase.decide(id, new AprovacaoDecisionRequest(AprovacaoStatus.APROVADA, "Confirmed"));
     */
    @Transactional(noRollbackFor = ApprovalExecutionFailedException.class)
    public AprovacaoDecisionResponse decide(UUID aprovacaoId, AprovacaoDecisionRequest request) {
        AprovacaoEntity aprovacao = findAndValidatePending(aprovacaoId);
        EventoActorContext actorContext = eventoActorContextResolver.resolveRequired();
        
        validateAuthorization(actorContext);
        applyDecisionState(aprovacao, request, actorContext);

        if (request.status() == AprovacaoStatus.REPROVADA) {
            return processRejection(aprovacao, actorContext);
        }

        return processApproval(aprovacao, actorContext);
    }

    private AprovacaoEntity findAndValidatePending(UUID aprovacaoId) {
        AprovacaoEntity aprovacao = aprovacaoJpaRepository.findById(aprovacaoId)
                .orElseThrow(() -> new ApprovalNotFoundException("Approval not found: " + aprovacaoId));

        if (aprovacao.getStatusEnum() != AprovacaoStatus.PENDENTE) {
            throw new ApprovalAlreadyDecidedException("Approval already decided: " + aprovacaoId);
        }

        if (isExpired(aprovacao)) {
            Instant start = approvalActionPayloadMapper.toPayload(aprovacao.getActionPayloadJson()).inicio();
            throw new ApprovalExecutionFailedException("Approval request has expired (event already started at %s)".formatted(start));
        }
        return aprovacao;
    }

    private void validateAuthorization(EventoActorContext actorContext) {
        eventoCancelamentoAuthorizationService.assertCanDecideApproval(actorContext);
        validateAprovacaoUseCase.validateApprovalDecisionRole(
                actorContext.role(),
                actorContext.organizationType());
    }

    private void applyDecisionState(AprovacaoEntity aprovacao, AprovacaoDecisionRequest request, EventoActorContext actorContext) {
        aprovacao.setStatus(request.status());
        aprovacao.setDecididoEmUtc(Instant.now());
        aprovacao.setDecisionObservacao(request.observacao());
        aprovacao.setAprovadorId(actorContext.usuarioId().toString());
        aprovacao.setAprovadorPapel(resolveApproverRole(actorContext));
    }

    private AprovacaoDecisionResponse processRejection(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        aprovacaoJpaRepository.save(aprovacao);
        eventoAuditPublisher.publishApprovalDecision(actorContext.actor(), aprovacao, "rejected",
                Map.of("decisionStatus", AprovacaoStatus.REPROVADA.name()));
        
        cadastroEventoMetricsPublisher.publishApprovalFlow(
                aprovacao.getTipoSolicitacao() == null ? "UNKNOWN" : aprovacao.getTipoSolicitacao(),
                "REJECTED");

        publishDecisionEvent(aprovacao, null);

        return new AprovacaoDecisionResponse(aprovacao.getId(), aprovacao.getStatusEnum().name(),
                new AprovacaoDecisionResponse.ActionExecution("REJECTED", aprovacao.getEventoId(),
                        fetchCurrentEventStatus(aprovacao.getEventoId()), null));
    }

    private AprovacaoDecisionResponse processApproval(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        TipoSolicitacaoInput tipo = TipoSolicitacaoInput.fromJson(aprovacao.getTipoSolicitacao());
        ApprovalExecutionStrategy strategy = strategies.stream()
                .filter(s -> s.supports(tipo))
                .findFirst()
                .orElseThrow(() -> new ApprovalExecutionFailedException(
                        "Unsupported approval type: " + aprovacao.getTipoSolicitacao()));

        try {
            AprovacaoDecisionResponse.ActionExecution execution = strategy.execute(aprovacao, actorContext);
            finalizeSuccessfulExecution(aprovacao, execution, actorContext, tipo);
            return new AprovacaoDecisionResponse(aprovacao.getId(), aprovacao.getStatusEnum().name(), execution);
        } catch (RuntimeException ex) {
            handleExecutionFailure(aprovacao, ex, actorContext, tipo);
            throw new ApprovalExecutionFailedException("Execution failed: " + ex.getMessage());
        }
    }

    private void finalizeSuccessfulExecution(AprovacaoEntity aprovacao, AprovacaoDecisionResponse.ActionExecution execution, 
                                          EventoActorContext actorContext, TipoSolicitacaoInput tipo) {
        Instant executedAt = Instant.now();
        aprovacao.setExecutadoEmUtc(executedAt);
        aprovacaoJpaRepository.save(aprovacao);

        eventoAuditPublisher.publishApprovalDecision(actorContext.actor(), aprovacao, "executed",
                Map.of("eventoId", execution.eventoId() == null ? "N/A" : execution.eventoId().toString(),
                        "decisionStatus", AprovacaoStatus.APROVADA.name()));

        cadastroEventoMetricsPublisher.publishApprovalFlow(tipo.name(), "EXECUTED");
        if (aprovacao.getDecididoEmUtc() != null) {
            cadastroEventoMetricsPublisher.publishApprovalExecutionLatency(tipo.name(), 
                    Duration.between(aprovacao.getDecididoEmUtc(), executedAt));
        }
        publishDecisionEvent(aprovacao, null);
    }

    private void handleExecutionFailure(AprovacaoEntity aprovacao, RuntimeException ex, 
                                       EventoActorContext actorContext, TipoSolicitacaoInput tipo) {
        aprovacao.setStatus(AprovacaoStatus.FALHA_EXECUCAO);
        aprovacao.setMensagemErroExecucao(ex.getMessage());
        aprovacaoJpaRepository.save(aprovacao);
        
        eventoAuditPublisher.publishApprovalDecision(actorContext.actor(), aprovacao, "failed",
                Map.of("error", ex.getClass().getSimpleName(), "decisionStatus", AprovacaoStatus.APROVADA.name()));
        
        cadastroEventoMetricsPublisher.publishApprovalFlow(tipo.name(), "FAILED");
        publishDecisionEvent(aprovacao, ex.getMessage());
    }

    private void publishDecisionEvent(AprovacaoEntity aprovacao, String errorMessage) {
        eventPublisher.publishEvent(new ApprovalDecisionEvent(
                aprovacao.getId(), aprovacao.getEventoId(), aprovacao.getSolicitanteId(),
                aprovacao.getStatusEnum(), aprovacao.getAprovadorId(), errorMessage));
    }

    private String fetchCurrentEventStatus(UUID eventoId) {
        if (eventoId == null) return null;
        return eventoJpaRepository.findStatusByIdNoLock(eventoId).orElse(null);
    }

    private AprovadorPapel resolveApproverRole(EventoActorContext actorContext) {
        return AprovadorPapel.resolveForApproval(actorContext.role(), actorContext.organizationType());
    }

    private boolean isExpired(AprovacaoEntity aprovacao) {
        try {
            ApprovalActionPayload payload = approvalActionPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
            return payload.inicio() != null && payload.inicio().isBefore(Instant.now());
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
