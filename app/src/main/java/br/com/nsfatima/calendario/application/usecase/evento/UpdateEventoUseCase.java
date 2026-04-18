package br.com.nsfatima.calendario.application.usecase.evento;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.EventoApprovalPendingResponse;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.api.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.calendario.application.usecase.aprovacao.ApprovalActionPayload;
import br.com.nsfatima.calendario.application.usecase.aprovacao.UpdateEventoApprovalRequestUseCase;
import br.com.nsfatima.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.calendario.domain.service.EventoPatchAuthorizationService;
import br.com.nsfatima.calendario.domain.service.EventoPatchAuthorizationService.CreateRequestMode;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;
    private final UpdateEventoApprovalRequestUseCase updateEventoApprovalRequestUseCase;
    private final EventoAuditPublisher eventoAuditPublisher;

    public UpdateEventoUseCase(
            EventoDomainService eventoDomainService,
            EventoJpaRepository eventoJpaRepository,
            EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository,
            EventoMapper eventoMapper,
            EventoPatchAuthorizationService eventoPatchAuthorizationService,
            EventoActorContextResolver eventoActorContextResolver,
            UpdateEventoParticipantesUseCase updateEventoParticipantesUseCase,
            ClearEventoParticipantesUseCase clearEventoParticipantesUseCase,
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher,
            UpdateEventoApprovalRequestUseCase updateEventoApprovalRequestUseCase,
            EventoAuditPublisher eventoAuditPublisher) {
        this.eventoDomainService = eventoDomainService;
        this.eventoJpaRepository = eventoJpaRepository;
        this.eventoEnvolvidoJpaRepository = eventoEnvolvidoJpaRepository;
        this.eventoMapper = eventoMapper;
        this.eventoPatchAuthorizationService = eventoPatchAuthorizationService;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.updateEventoParticipantesUseCase = updateEventoParticipantesUseCase;
        this.clearEventoParticipantesUseCase = clearEventoParticipantesUseCase;
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
        this.updateEventoApprovalRequestUseCase = updateEventoApprovalRequestUseCase;
        this.eventoAuditPublisher = eventoAuditPublisher;
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
            CreateRequestMode mode = eventoPatchAuthorizationService.resolveCreateRequestMode(
                    actorContext, entity.getOrganizacaoResponsavelId());
            if (mode == CreateRequestMode.REQUIRES_APPROVAL) {
                EventoApprovalPendingResponse pending = updateEventoApprovalRequestUseCase.create(eventoId,
                        request);
                return new UpdateEventoResult(HttpStatus.ACCEPTED, pending);
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

        eventoAuditPublisher.publish(
                actorContext.actor(),
                "patch",
                saved.getId().toString(),
                "success",
                java.util.Map.of(
                        "organizacaoId", saved.getOrganizacaoResponsavelId(),
                        "scheduleChanged", scheduleChanged,
                        "cancellation", cancellation,
                        "responsibleOrgChanged", changesResponsibleOrganization,
                        "sensitiveChange", request.changesSensitiveFields()));

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

        boolean scheduleChanged = request.inicio() != null || request.fim() != null;
        boolean cancellation = request.status() == EventoStatusInput.CANCELADO;
        boolean changesResponsibleOrganization = request.organizacaoResponsavelId() != null
                && !request.organizacaoResponsavelId().equals(saved.getOrganizacaoResponsavelId());
        if (scheduleChanged || cancellation || changesResponsibleOrganization) {
            cadastroEventoMetricsPublisher.publishAdministrativeRework(
                    scheduleChanged,
                    cancellation,
                    changesResponsibleOrganization);
        }

        eventoAuditPublisher.publish(
                resolveActor(),
                "patch",
                saved.getId().toString(),
                "executed",
                java.util.Map.of(
                        "organizacaoId", saved.getOrganizacaoResponsavelId(),
                        "scheduleChanged", scheduleChanged,
                        "cancellation", cancellation,
                        "responsibleOrgChanged", changesResponsibleOrganization,
                        "approvalFlow", true));

        return eventoMapper.toResponse(saved);
    }

    public UpdateEventoRequest restoreFromApprovalPayload(ApprovalActionPayload payload) {
        return new UpdateEventoRequest(
                payload.titulo(),
                payload.descricao(),
                payload.inicio(),
                payload.fim(),
                payload.status(),
                payload.adicionadoExtraJustificativa(),
                payload.canceladoMotivo(),
                payload.organizacaoResponsavelId(),
                payload.participantes());
    }

    private String resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "anonymous";
        }
        return authentication.getName();
    }
}
