package br.com.nsfatima.calendario.application.usecase.evento;

import br.com.nsfatima.calendario.api.dto.evento.CreateEventoRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoApprovalPendingResponse;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.application.usecase.aprovacao.ApprovalActionPayload;
import br.com.nsfatima.calendario.application.usecase.aprovacao.CreateEventoApprovalRequestUseCase;
import br.com.nsfatima.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.calendario.domain.service.EventoPatchAuthorizationService;
import br.com.nsfatima.calendario.domain.service.EventoPatchAuthorizationService.CreateRequestMode;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateEventoUseCase {

    private final EventoDomainService eventoDomainService;
    private final EventoJpaRepository eventoJpaRepository;
    private final EventoMapper eventoMapper;
    private final EventoIdempotencyService eventoIdempotencyService;
    private final EventoAuditPublisher eventoAuditPublisher;
    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;
    private final EventoPatchAuthorizationService eventoPatchAuthorizationService;
    private final EventoActorContextResolver eventoActorContextResolver;
    private final CreateEventoApprovalRequestUseCase createEventoApprovalRequestUseCase;

    public CreateEventoUseCase(
            EventoDomainService eventoDomainService,
            EventoJpaRepository eventoJpaRepository,
            EventoMapper eventoMapper,
            EventoIdempotencyService eventoIdempotencyService,
            EventoAuditPublisher eventoAuditPublisher,
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher,
            EventoPatchAuthorizationService eventoPatchAuthorizationService,
            EventoActorContextResolver eventoActorContextResolver,
            CreateEventoApprovalRequestUseCase createEventoApprovalRequestUseCase) {
        this.eventoDomainService = eventoDomainService;
        this.eventoJpaRepository = eventoJpaRepository;
        this.eventoMapper = eventoMapper;
        this.eventoIdempotencyService = eventoIdempotencyService;
        this.eventoAuditPublisher = eventoAuditPublisher;
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
        this.eventoPatchAuthorizationService = eventoPatchAuthorizationService;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.createEventoApprovalRequestUseCase = createEventoApprovalRequestUseCase;
    }

    @Transactional
    @SuppressWarnings("null")
    public CreateEventoResult execute(String idempotencyKey, CreateEventoRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        // Validate domain rules eagerly on all paths — prevents queueing invalid
        // requests
        EventoStatusInput status = request.status() == null ? EventoStatusInput.RASCUNHO : request.status();
        eventoDomainService.validateEvento(
                request.inicio(),
                request.fim(),
                status,
                request.adicionadoExtraJustificativa());
        eventoDomainService.validateOrganizacaoParticipantes(
                request.organizacaoResponsavelId(),
                request.participantes());

        EventoActorContext actorContext = eventoActorContextResolver.resolveRequired();
        CreateRequestMode mode = eventoPatchAuthorizationService.resolveCreateRequestMode(
                actorContext,
                request.organizacaoResponsavelId());

        if (mode == CreateRequestMode.REQUIRES_APPROVAL) {
            EventoApprovalPendingResponse pending = createEventoApprovalRequestUseCase.create(idempotencyKey, request);
            return new CreateEventoResult(HttpStatus.ACCEPTED, pending);
        }

        return new CreateEventoResult(HttpStatus.CREATED,
                createImmediate(idempotencyKey, request, actorContext.actor()));
    }

    @Transactional
    public EventoResponse executeApprovedCreation(CreateEventoRequest request, String idempotencyKey) {
        return createImmediate(idempotencyKey, request, "approval-flow");
    }

    @SuppressWarnings("null")
    private EventoResponse createImmediate(String idempotencyKey, CreateEventoRequest request, String actor) {
        EventoStatusInput status = request.status() == null ? EventoStatusInput.RASCUNHO : request.status();
        eventoDomainService.validateEvento(
                request.inicio(),
                request.fim(),
                status,
                request.adicionadoExtraJustificativa());
        eventoDomainService.validateOrganizacaoParticipantes(
                request.organizacaoResponsavelId(),
                request.participantes());

        try {
            EventoIdempotencyService.IdempotencyResult result = eventoIdempotencyService.execute(
                    idempotencyKey,
                    request,
                    () -> {
                        EventoEntity entity = eventoMapper.toNewEntity(request, status);
                        boolean hasOverlap = eventoJpaRepository.existsByInicioUtcLessThanAndFimUtcGreaterThan(
                                request.fim(),
                                request.inicio());
                        entity.setConflictState(eventoDomainService.resolveConflictState(hasOverlap));
                        entity.setConflictReason(eventoDomainService.resolveConflictReason(hasOverlap));

                        EventoEntity saved = Objects.requireNonNull(eventoJpaRepository.save(entity));
                        return eventoMapper.toResponse(saved);
                    });

            EventoResponse response = result.response();
            boolean conflictPending = "CONFLICT_PENDING".equals(response.conflictState());
            cadastroEventoMetricsPublisher.publishCreateSuccess(conflictPending, result.replay());
            cadastroEventoMetricsPublisher.publishEventRegistrationLeadTime(
                    Duration.between(Instant.now(), request.inicio()));
            eventoAuditPublisher.publishCreateSuccess(
                    actor,
                    response.id().toString(),
                    result.replay(),
                    response.conflictState());
            return response;
        } catch (RuntimeException ex) {
            cadastroEventoMetricsPublisher.publishCreateFailure("BUSINESS_OR_VALIDATION");
            eventoAuditPublisher.publishCreateFailure(actor, "BUSINESS_OR_VALIDATION", ex.getMessage());
            throw ex;
        }
    }

    public record CreateEventoResult(HttpStatus httpStatus, Object body) {
    }

    public CreateEventoRequest restoreFromApprovalPayload(ApprovalActionPayload payload) {
        if (payload.organizacaoResponsavelId() == null) {
            throw new IllegalArgumentException("organizacaoResponsavelId deve ser informado");
        }
        if (payload.inicio() == null) {
            throw new IllegalArgumentException("inicio deve ser informado");
        }
        if (payload.fim() == null) {
            throw new IllegalArgumentException("fim deve ser informado");
        }
        return new CreateEventoRequest(
                payload.titulo(),
                payload.descricao(),
                payload.organizacaoResponsavelId(),
                payload.inicio(),
                payload.fim(),
                payload.status(),
                payload.adicionadoExtraJustificativa(),
                payload.participantes());
    }
}
