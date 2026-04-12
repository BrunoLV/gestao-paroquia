package br.com.nsfatima.calendario.application.usecase.evento;

import br.com.nsfatima.calendario.api.dto.evento.CancelEventoRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoCanceladoResponse;
import br.com.nsfatima.calendario.api.dto.evento.CancelamentoPendenteResponse;
import br.com.nsfatima.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.calendario.domain.exception.InvalidStatusTransitionException;
import br.com.nsfatima.calendario.application.usecase.aprovacao.ApprovalActionPayloadMapper;
import br.com.nsfatima.calendario.domain.service.EventoCancelamentoAuthorizationService;
import br.com.nsfatima.calendario.domain.service.EventoCancelamentoAuthorizationService.CancelamentoRequestMode;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.calendario.infrastructure.security.UsuarioDetails;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelEventoUseCase {

    private final EventoJpaRepository eventoJpaRepository;
    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final EventoActorContextResolver eventoActorContextResolver;
    private final EventoCancelamentoAuthorizationService eventoCancelamentoAuthorizationService;
    private final EventoAuditPublisher eventoAuditPublisher;
    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;

    public CancelEventoUseCase(
            EventoJpaRepository eventoJpaRepository,
            AprovacaoJpaRepository aprovacaoJpaRepository,
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            EventoActorContextResolver eventoActorContextResolver,
            EventoCancelamentoAuthorizationService eventoCancelamentoAuthorizationService,
            EventoAuditPublisher eventoAuditPublisher,
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher,
            ApprovalActionPayloadMapper approvalActionPayloadMapper) {
        this.eventoJpaRepository = eventoJpaRepository;
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.eventoCancelamentoAuthorizationService = eventoCancelamentoAuthorizationService;
        this.eventoAuditPublisher = eventoAuditPublisher;
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
    }

    @Transactional
    public CancelEventoResult execute(UUID eventoId, CancelEventoRequest request) {
        EventoEntity evento = eventoJpaRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNotFoundException(eventoId));

        EventoActorContext actorContext = eventoActorContextResolver.resolveRequired();
        CancelamentoRequestMode requestMode = eventoCancelamentoAuthorizationService.resolveRequestMode(
                actorContext,
                evento.getOrganizacaoResponsavelId());

        validateCancellableStatus(evento);
        if (requestMode == CancelamentoRequestMode.IMMEDIATE) {
            EventoCanceladoResponse response = applyCancellation(
                    evento,
                    request.motivo(),
                    resolveActor(),
                    resolveUsuarioId(),
                    "success");
            cadastroEventoMetricsPublisher.publishCancellationFlow("IMMEDIATE", "SUCCESS");
            return new CancelEventoResult(HttpStatus.OK, response);
        }

        CancelamentoPendenteResponse response = createPendingApproval(evento, request, actorContext);
        cadastroEventoMetricsPublisher.publishCancellationFlow("PENDING_CREATED", "PENDING");
        return new CancelEventoResult(HttpStatus.ACCEPTED, response);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventoCanceladoResponse executeApprovedCancellation(
            UUID eventoId,
            String motivo,
            String actor,
            UUID usuarioId) {
        EventoEntity evento = eventoJpaRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNotFoundException(eventoId));
        validateCancellableStatus(evento);
        cadastroEventoMetricsPublisher.publishCancellationFlow("EXECUTED_AFTER_APPROVAL", "SUCCESS");
        return applyCancellation(evento, motivo, actor, usuarioId, "executed-after-approval");
    }

    private void validateCancellableStatus(EventoEntity evento) {
        if (!"CONFIRMADO".equalsIgnoreCase(evento.getStatus())) {
            throw new InvalidStatusTransitionException("Only CONFIRMADO events can be cancelled");
        }
    }

    private EventoCanceladoResponse applyCancellation(
            EventoEntity evento,
            String motivo,
            String actor,
            UUID usuarioId,
            String auditResult) {
        evento.setStatus("CANCELADO");
        evento.setCanceladoMotivo(motivo);
        EventoEntity saved = Objects.requireNonNull(eventoJpaRepository.save(evento));

        ObservacaoEventoEntity observacao = new ObservacaoEventoEntity();
        observacao.setId(UUID.randomUUID());
        observacao.setEventoId(saved.getId());
        observacao.setUsuarioId(usuarioId);
        observacao.setTipo(TipoObservacaoInput.CANCELAMENTO.name());
        observacao.setConteudo(motivo);
        observacao.setCriadoEmUtc(Instant.now());
        observacaoEventoJpaRepository.save(observacao);

        cadastroEventoMetricsPublisher.publishAdministrativeRework(false, true, false);
        eventoAuditPublisher.publish(
                actor,
                "cancel",
                saved.getId().toString(),
                auditResult,
                Map.of(
                        "status", saved.getStatus(),
                        "motivo", motivo,
                        "tipoObservacao", TipoObservacaoInput.CANCELAMENTO.name()));

        return new EventoCanceladoResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getCanceladoMotivo(),
                saved.getTitulo(),
                saved.getInicioUtc(),
                saved.getFimUtc(),
                saved.getOrganizacaoResponsavelId());
    }

    private CancelamentoPendenteResponse createPendingApproval(
            EventoEntity evento,
            CancelEventoRequest request,
            EventoActorContext actorContext) {
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(UUID.randomUUID());
        aprovacao.setEventoId(evento.getId());
        aprovacao.setTipoSolicitacao(TipoSolicitacaoInput.CANCELAMENTO.name());
        aprovacao.setAprovadorPapel("conselho-coordenador");
        aprovacao.setStatus("PENDENTE");
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setSolicitanteId(resolveUsuarioId().toString());
        aprovacao.setSolicitantePapel(actorContext.role());
        aprovacao.setSolicitanteTipoOrganizacao(actorContext.organizationType());
        aprovacao.setMotivoCancelamentoSnapshot(request.motivo());
        aprovacao.setActionPayloadJson(approvalActionPayloadMapper.toJson(Map.of(
                "tipo", "CANCELAMENTO",
                "motivo", request.motivo() != null ? request.motivo() : "")));
        aprovacao.setCorrelationId(evento.getId().toString());
        aprovacaoJpaRepository.save(aprovacao);

        eventoAuditPublisher.publishCancellationPending(
                resolveActor(),
                evento.getId().toString(),
                aprovacao.getId().toString(),
                request.motivo());

        return new CancelamentoPendenteResponse(
                aprovacao.getId(),
                aprovacao.getStatus(),
                evento.getId(),
                "APPROVAL_PENDING");
    }

    private UUID resolveUsuarioId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails) {
            return usuarioDetails.getUsuarioId();
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    private String resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "anonymous";
        }
        return authentication.getName();
    }

    public record CancelEventoResult(HttpStatus httpStatus, Object body) {
    }
}
