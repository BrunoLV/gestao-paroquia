package br.com.nsfatima.calendario.application.usecase.evento;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.api.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.calendario.domain.service.EventoPatchAuthorizationService;
import br.com.nsfatima.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.calendario.application.usecase.aprovacao.ValidateAprovacaoUseCase;
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

    public UpdateEventoUseCase(
            EventoDomainService eventoDomainService,
            EventoJpaRepository eventoJpaRepository,
            EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository,
            EventoMapper eventoMapper,
            EventoPatchAuthorizationService eventoPatchAuthorizationService,
            EventoActorContextResolver eventoActorContextResolver,
            UpdateEventoParticipantesUseCase updateEventoParticipantesUseCase,
            ClearEventoParticipantesUseCase clearEventoParticipantesUseCase,
            ValidateAprovacaoUseCase validateAprovacaoUseCase) {
        this.eventoDomainService = eventoDomainService;
        this.eventoJpaRepository = eventoJpaRepository;
        this.eventoEnvolvidoJpaRepository = eventoEnvolvidoJpaRepository;
        this.eventoMapper = eventoMapper;
        this.eventoPatchAuthorizationService = eventoPatchAuthorizationService;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.updateEventoParticipantesUseCase = updateEventoParticipantesUseCase;
        this.clearEventoParticipantesUseCase = clearEventoParticipantesUseCase;
        this.validateAprovacaoUseCase = validateAprovacaoUseCase;
    }

    @Transactional
    public EventoResponse execute(UUID eventoId, UpdateEventoRequest request) {
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
            validateAprovacaoUseCase.validateRequired(eventoId, request.aprovacaoId());
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
        EventoEntity saved = eventoJpaRepository.save(entity);

        if (request.participantes() != null) {
            if (request.participantes().isEmpty()) {
                clearEventoParticipantesUseCase.execute(eventoId);
            } else {
                updateEventoParticipantesUseCase.execute(eventoId, request.participantes());
            }
        }

        return eventoMapper.toResponse(saved);
    }
}
