package br.com.nsfatima.calendario.application.usecase.evento;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.EventoApprovalPendingResponse;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.api.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.calendario.application.usecase.aprovacao.UpdateEventoApprovalRequestUseCase;
import br.com.nsfatima.calendario.application.usecase.aprovacao.ValidateAprovacaoUseCase;
import br.com.nsfatima.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.calendario.domain.service.EventoPatchAuthorizationService;
import br.com.nsfatima.calendario.domain.service.EventoPatchAuthorizationService.CreateRequestMode;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEventoUseCase {

    private final EventoDomainService eventoDomainService;
    private final EventoJpaRepository eventoJpaRepository;
    private final EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository;
    private final EventoMapper eventoMapper;
    private final EventoPatchAuthorizationService eventoPatchAuthorizationService;
    private final EventoActorContextResolver eventoActorContextResolver;
    private final UpdateEventoParticipantesUseCase updateEventoParticipantesUseCase;
    private final ClearEventoParticipantesUseCase clearEventoParticipantesUseCase;
    private final ValidateAprovacaoUseCase validateAprovacaoUseCase;
    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;
    private final UpdateEventoApprovalRequestUseCase updateEventoApprovalRequestUseCase;

    public UpdateEventoUseCase(
            EventoDomainService eventoDomainService,
            EventoJpaRepository eventoJpaRepository,
            EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository,
            EventoMapper eventoMapper,
            EventoPatchAuthorizationService eventoPatchAuthorizationService,
            EventoActorContextResolver eventoActorContextResolver,
            UpdateEventoParticipantesUseCase updateEventoParticipantesUseCase,
            ClearEventoParticipantesUseCase clearEventoParticipantesUseCase,
            ValidateAprovacaoUseCase validateAprovacaoUseCase,
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher,
            UpdateEventoApprovalRequestUseCase updateEventoApprovalRequestUseCase) {
        this.eventoDomainService = eventoDomainService;
        this.eventoJpaRepository = eventoJpaRepository;
        this.eventoEnvolvidoJpaRepository = eventoEnvolvidoJpaRepository;
        this.eventoMapper = eventoMapper;
        this.eventoPatchAuthorizationService = eventoPatchAuthorizationService;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.updateEventoParticipantesUseCase = updateEventoParticipantesUseCase;
        this.clearEventoParticipantesUseCase = clearEventoParticipantesUseCase;
        this.validateAprovacaoUseCase = validateAprovacaoUseCase;
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
        this.updateEventoApprovalRequestUseCase = updateEventoApprovalRequestUseCase;
    }

    public record UpdateEventoResult(HttpStatus httpStatus, Object body) {
    }

    @Transactional
    @SuppressWarnings("null")
    public UpdateEventoResult execute(UUID eventoId, UpdateEventoRequest request) {
        if (request == null || request.isEmptyPayload()) {
            throw new IllegalArgumentException("PATCH payload must not be empty");
        }

        EventoEntity entity = eventoJpaRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNotFoundException(eventoId));

        EventoActorContext actorContext = eventoActorContextResolver.resolveRequired();
        boolean changesResponsibleOrganization = request.organizacaoResponsavelId() != null
                && !request.organizacaoResponsavelId().equals(entity.getOrganizacaoResponsavelId());

        if (changesResponsibleOrganization) {
            eventoPatchAuthorizationService.assertCanChangeResponsibleOrganization(actorContext);
        } else {
            eventoPatchAuthorizationService.assertCanEditGeneral(actorContext, entity.getOrganizacaoResponsavelId());
        }

        if (request.participantes() != null) {
            eventoPatchAuthorizationService.assertCanManageParticipants(actorContext,
                    entity.getOrganizacaoResponsavelId());
        }

        if (request.changesSensitiveFields()) {
            if (request.aprovacaoId() == null) {
                CreateRequestMode mode = eventoPatchAuthorizationService.resolveCreateRequestMode(
                        actorContext, entity.getOrganizacaoResponsavelId());
                if (mode == CreateRequestMode.REQUIRES_APPROVAL) {
                    EventoApprovalPendingResponse pending = updateEventoApprovalRequestUseCase.create(eventoId,
                            request);
                    return new UpdateEventoResult(HttpStatus.ACCEPTED, pending);
                }
            } else {
                validateAprovacaoUseCase.validateRequired(eventoId, request.aprovacaoId());
            }
        }

        EventoStatusInput mergedStatus = request.status() != null
                ? request.status()
                : EventoStatusInput.valueOf(entity.getStatus());
        Instant mergedInicio = request.inicio() != null ? request.inicio() : entity.getInicioUtc();
        Instant mergedFim = request.fim() != null ? request.fim() : entity.getFimUtc();
        String mergedJustificativa = request.adicionadoExtraJustificativa() != null
                ? request.adicionadoExtraJustificativa()
                : entity.getAdicionadoExtraJustificativa();

        eventoDomainService.validateEvento(mergedInicio, mergedFim, mergedStatus, mergedJustificativa);

        UUID mergedOrganizacaoResponsavel = request.organizacaoResponsavelId() != null
                ? request.organizacaoResponsavelId()
                : entity.getOrganizacaoResponsavelId();
        List<UUID> mergedParticipantes = request.participantes() != null
                ? request.participantes()
                : eventoEnvolvidoJpaRepository.findByEventoId(eventoId)
                        .stream()
                        .map(envolvido -> envolvido.getOrganizacaoId())
                        .toList();

        eventoDomainService.validateOrganizacaoParticipantes(mergedOrganizacaoResponsavel, mergedParticipantes);

        eventoMapper.applyPatch(entity, request, mergedStatus);
        EventoEntity saved = Objects.requireNonNull(eventoJpaRepository.save(entity));

        if (request.participantes() != null) {
            if (request.participantes().isEmpty()) {
                clearEventoParticipantesUseCase.execute(eventoId);
            } else {
                updateEventoParticipantesUseCase.execute(eventoId, request.participantes());
            }
        }

        boolean scheduleChanged = request.inicio() != null || request.fim() != null;
        boolean cancellation = request.status() == EventoStatusInput.CANCELADO;
        if (scheduleChanged || cancellation || changesResponsibleOrganization) {
            cadastroEventoMetricsPublisher.publishAdministrativeRework(
                    scheduleChanged,
                    cancellation,
                    changesResponsibleOrganization);
        }

        return new UpdateEventoResult(HttpStatus.OK, eventoMapper.toResponse(saved));
    }

    @Transactional(noRollbackFor = RuntimeException.class)
    @SuppressWarnings("null")
    public EventoResponse executeApprovedUpdate(UUID eventoId, UpdateEventoRequest request) {
        EventoEntity entity = eventoJpaRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNotFoundException(eventoId));

        EventoStatusInput mergedStatus = request.status() != null
                ? request.status()
                : EventoStatusInput.valueOf(entity.getStatus());
        Instant mergedInicio = request.inicio() != null ? request.inicio() : entity.getInicioUtc();
        Instant mergedFim = request.fim() != null ? request.fim() : entity.getFimUtc();
        String mergedJustificativa = request.adicionadoExtraJustificativa() != null
                ? request.adicionadoExtraJustificativa()
                : entity.getAdicionadoExtraJustificativa();

        eventoDomainService.validateEvento(mergedInicio, mergedFim, mergedStatus, mergedJustificativa);

        UUID mergedOrganizacaoResponsavel = request.organizacaoResponsavelId() != null
                ? request.organizacaoResponsavelId()
                : entity.getOrganizacaoResponsavelId();
        List<UUID> mergedParticipantes = request.participantes() != null
                ? request.participantes()
                : eventoEnvolvidoJpaRepository.findByEventoId(eventoId)
                        .stream()
                        .map(envolvido -> envolvido.getOrganizacaoId())
                        .toList();

        eventoDomainService.validateOrganizacaoParticipantes(mergedOrganizacaoResponsavel, mergedParticipantes);

        eventoMapper.applyPatch(entity, request, mergedStatus);
        EventoEntity saved = Objects.requireNonNull(eventoJpaRepository.save(entity));

        if (request.participantes() != null) {
            if (request.participantes().isEmpty()) {
                clearEventoParticipantesUseCase.execute(eventoId);
            } else {
                updateEventoParticipantesUseCase.execute(eventoId, request.participantes());
            }
        }

        return eventoMapper.toResponse(saved);
    }

    public UpdateEventoRequest restoreFromApprovalPayload(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<UUID> participantes = payload.get("participantes") == null
                ? null
                : ((List<Object>) payload.get("participantes")).stream()
                        .map(String::valueOf)
                        .map(UUID::fromString)
                        .toList();

        return new UpdateEventoRequest(
                payload.get("titulo") == null ? null : String.valueOf(payload.get("titulo")),
                payload.get("descricao") == null ? null : String.valueOf(payload.get("descricao")),
                payload.get("inicio") == null ? null : Instant.parse(String.valueOf(payload.get("inicio"))),
                payload.get("fim") == null ? null : Instant.parse(String.valueOf(payload.get("fim"))),
                payload.get("status") == null ? null : EventoStatusInput.valueOf(String.valueOf(payload.get("status"))),
                payload.get("adicionadoExtraJustificativa") == null
                        ? null
                        : String.valueOf(payload.get("adicionadoExtraJustificativa")),
                payload.get("canceladoMotivo") == null ? null : String.valueOf(payload.get("canceladoMotivo")),
                payload.get("organizacaoResponsavelId") == null
                        ? null
                        : UUID.fromString(String.valueOf(payload.get("organizacaoResponsavelId"))),
                participantes,
                null);
    }
}
