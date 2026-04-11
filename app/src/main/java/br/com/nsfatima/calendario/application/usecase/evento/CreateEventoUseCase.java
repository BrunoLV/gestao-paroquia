package br.com.nsfatima.calendario.application.usecase.evento;

import br.com.nsfatima.calendario.api.dto.evento.CreateEventoRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.calendario.domain.service.EventoPatchAuthorizationService;
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
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import org.springframework.stereotype.Service;

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

    public CreateEventoUseCase(
            EventoDomainService eventoDomainService,
            EventoJpaRepository eventoJpaRepository,
            EventoMapper eventoMapper,
            EventoIdempotencyService eventoIdempotencyService,
            EventoAuditPublisher eventoAuditPublisher,
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher,
            EventoPatchAuthorizationService eventoPatchAuthorizationService,
            EventoActorContextResolver eventoActorContextResolver) {
        this.eventoDomainService = eventoDomainService;
        this.eventoJpaRepository = eventoJpaRepository;
        this.eventoMapper = eventoMapper;
        this.eventoIdempotencyService = eventoIdempotencyService;
        this.eventoAuditPublisher = eventoAuditPublisher;
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
        this.eventoPatchAuthorizationService = eventoPatchAuthorizationService;
        this.eventoActorContextResolver = eventoActorContextResolver;
    }

    @Transactional
    @SuppressWarnings("null")
    public EventoResponse execute(String idempotencyKey, CreateEventoRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        EventoActorContext actorContext = eventoActorContextResolver.resolveRequired();
        eventoPatchAuthorizationService.assertCanCreate(actorContext, request.organizacaoResponsavelId());

        EventoStatusInput status = request.status() == null ? EventoStatusInput.RASCUNHO : request.status();
        eventoDomainService.validateEvento(request.inicio(), request.fim(), status,
                request.adicionadoExtraJustificativa());
        eventoDomainService.validateOrganizacaoParticipantes(
                request.organizacaoResponsavelId(),
                request.participantes());

        try {
            EventoIdempotencyService.IdempotencyResult result = eventoIdempotencyService.execute(idempotencyKey,
                    request, () -> {
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
            eventoAuditPublisher.publishCreateSuccess("system", response.id().toString(), result.replay(),
                    response.conflictState());
            return response;
        } catch (RuntimeException ex) {
            cadastroEventoMetricsPublisher.publishCreateFailure("BUSINESS_OR_VALIDATION");
            eventoAuditPublisher.publishCreateFailure("system", "BUSINESS_OR_VALIDATION", ex.getMessage());
            throw ex;
        }
    }
}
