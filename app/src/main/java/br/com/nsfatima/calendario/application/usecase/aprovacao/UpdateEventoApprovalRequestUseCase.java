package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.api.dto.evento.EventoApprovalPendingResponse;
import br.com.nsfatima.calendario.api.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.calendario.infrastructure.security.UsuarioDetails;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEventoApprovalRequestUseCase {

    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;
    private final EventoActorContextResolver eventoActorContextResolver;
    private final EventoAuditPublisher eventoAuditPublisher;

    public UpdateEventoApprovalRequestUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            ApprovalActionPayloadMapper approvalActionPayloadMapper,
            EventoActorContextResolver eventoActorContextResolver,
            EventoAuditPublisher eventoAuditPublisher) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.eventoAuditPublisher = eventoAuditPublisher;
    }

    @Transactional
    public EventoApprovalPendingResponse create(UUID eventoId, UpdateEventoRequest request) {
        EventoActorContext actorContext = eventoActorContextResolver.resolveRequired();

        UUID approvalId = UUID.randomUUID();
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(approvalId);
        aprovacao.setEventoId(eventoId);
        aprovacao.setTipoSolicitacao("EDICAO_EVENTO");
        aprovacao.setAprovadorPapel(resolveAprovadorPapel(actorContext));
        aprovacao.setStatus("PENDENTE");
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setSolicitanteId(resolveUsuarioId().toString());
        aprovacao.setSolicitantePapel(actorContext.role());
        aprovacao.setSolicitanteTipoOrganizacao(actorContext.organizationType());
        aprovacao.setCorrelationId(approvalId.toString());
        aprovacao.setActionPayloadJson(approvalActionPayloadMapper.toJson(buildPayload(eventoId, request)));
        aprovacaoJpaRepository.save(aprovacao);

        eventoAuditPublisher.publish(
                actorContext.actor(),
                "update",
                "evento",
                "pending",
                Map.of(
                        "solicitacaoAprovacaoId", approvalId.toString(),
                        "tipoSolicitacao", "EDICAO_EVENTO",
                        "eventoId", eventoId.toString()));

        return new EventoApprovalPendingResponse(approvalId, "PENDENTE", "APPROVAL_PENDING");
    }

    private Map<String, Object> buildPayload(UUID eventoId, UpdateEventoRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventoId", eventoId.toString());
        payload.put("titulo", request.titulo());
        payload.put("descricao", request.descricao());
        payload.put("organizacaoResponsavelId",
                request.organizacaoResponsavelId() == null ? null : request.organizacaoResponsavelId().toString());
        payload.put("inicio", request.inicio() == null ? null : request.inicio().toString());
        payload.put("fim", request.fim() == null ? null : request.fim().toString());
        payload.put("status", request.status() == null ? null : request.status().name());
        payload.put("adicionadoExtraJustificativa", request.adicionadoExtraJustificativa());
        payload.put("canceladoMotivo", request.canceladoMotivo());
        payload.put("participantes", request.participantes() == null ? null
                : request.participantes().stream().map(UUID::toString).toList());
        return payload;
    }

    private UUID resolveUsuarioId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails) {
            return usuarioDetails.getUsuarioId();
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    private String resolveAprovadorPapel(EventoActorContext actorContext) {
        String role = actorContext.role() == null ? "" : actorContext.role().trim().toLowerCase();
        String orgType = actorContext.organizationType() == null
                ? ""
                : actorContext.organizationType().trim().toLowerCase();
        if ("paroco".equals(role)) {
            return "paroco";
        }
        if ("conselho".equals(orgType) && "vice-coordenador".equals(role)) {
            return "conselho-vice-coordenador";
        }
        return "conselho-coordenador";
    }
}
