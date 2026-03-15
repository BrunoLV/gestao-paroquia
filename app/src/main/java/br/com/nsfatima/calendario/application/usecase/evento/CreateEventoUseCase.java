package br.com.nsfatima.calendario.application.usecase.evento;

import br.com.nsfatima.calendario.api.dto.evento.CreateEventoRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
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

    public CreateEventoUseCase(
            EventoDomainService eventoDomainService,
            EventoJpaRepository eventoJpaRepository,
            EventoMapper eventoMapper,
            EventoIdempotencyService eventoIdempotencyService,
            EventoAuditPublisher eventoAuditPublisher,
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher) {
        this.eventoDomainService = eventoDomainService;
        this.eventoJpaRepository = eventoJpaRepository;
        this.eventoMapper = eventoMapper;
        this.eventoIdempotencyService = eventoIdempotencyService;
        this.eventoAuditPublisher = eventoAuditPublisher;
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
    }

    @Transactional
    @SuppressWarnings("null")
    public EventoResponse execute(String idempotencyKey, CreateEventoRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

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
