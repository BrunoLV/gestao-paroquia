package br.com.nsfatima.calendario.infrastructure.observability;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.calendario.application.usecase.metrics.PersistenciaAuditoriaObrigatoriaException;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.calendario.application.usecase.aprovacao.ApprovalActionPayloadMapper;
import br.com.nsfatima.calendario.application.usecase.aprovacao.ApprovalActionPayload;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;
    private final EventoJpaRepository eventoJpaRepository;
    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final ObjectMapper objectMapper;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;

    public AuditLogService(
            AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository,
            EventoJpaRepository eventoJpaRepository,
            AprovacaoJpaRepository aprovacaoJpaRepository,
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            ObjectMapper objectMapper,
            ApprovalActionPayloadMapper approvalActionPayloadMapper) {
        this.auditoriaOperacaoJpaRepository = auditoriaOperacaoJpaRepository;
        this.eventoJpaRepository = eventoJpaRepository;
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.objectMapper = objectMapper;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
    }

    public void log(String actor, String action, String target, String result) {
        log(actor, action, target, result, Map.of());
    }

    public void log(String actor, String action, String target, String result, Map<String, Object> metadata) {
        String correlationId = MDC.get("correlationId");
        Map<String, Object> enrichedMetadata = new LinkedHashMap<>(metadata);
        if (correlationId != null && !correlationId.isBlank()) {
            enrichedMetadata.putIfAbsent("correlationId", correlationId);
        }

        if (String.valueOf(result).toLowerCase().contains("denied")
                || String.valueOf(result).toLowerCase().contains("failure")) {
            enrichedMetadata.putIfAbsent("errorCategory", "SECURITY_OR_VALIDATION");
        }

        LOGGER.info(
                "audit actor={} action={} target={} result={} metadata={}",
                actor,
                action,
                target,
                result,
                enrichedMetadata);

        persistAuditableRecord(actor, action, target, result, correlationId, enrichedMetadata);
    }

    private void persistAuditableRecord(
            String actor,
            String action,
            String target,
            String result,
            String correlationId,
            Map<String, Object> enrichedMetadata) {
        if (!isAuditableAction(action)) {
            return;
        }

        String resourceType = resolveResourceType(action, enrichedMetadata);
        String resourceId = resolveResourceId(target, enrichedMetadata, resourceType);
        UUID eventId = resolveEventId(target, enrichedMetadata, resourceType, resourceId);
        UUID organizationId = resolveOrganizationId(enrichedMetadata, resourceType, resourceId, eventId);

        if (mustFailClosed(action, resourceType, resourceId, organizationId, eventId, result)) {
            throw new PersistenciaAuditoriaObrigatoriaException(
                    "Nao foi possivel persistir a trilha auditavel obrigatoria da operacao");
        }
        if (resourceId == null || organizationId == null) {
            return;
        }

        AuditoriaOperacaoEntity entity = new AuditoriaOperacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizacaoId(organizationId);
        entity.setEventoId(eventId);
        entity.setRecursoTipo(resourceType);
        entity.setRecursoId(resourceId);
        entity.setAcao(action);
        entity.setResultado(result);
        entity.setAtor(actor == null || actor.isBlank() ? "anonymous" : actor);
        entity.setAtorUsuarioId(resolveActorUserId(enrichedMetadata));
        String resolvedCorrelationId = correlationId;
        if (resolvedCorrelationId == null || resolvedCorrelationId.isBlank()) {
            Object metadataCorrelationId = enrichedMetadata.get("correlationId");
            resolvedCorrelationId = metadataCorrelationId == null ? null : String.valueOf(metadataCorrelationId);
        }
        entity.setCorrelationId(
                resolvedCorrelationId == null || resolvedCorrelationId.isBlank() ? "n/a" : resolvedCorrelationId);
        entity.setOcorridoEmUtc(Instant.now());
        entity.setDetalhesAuditaveisJson(writeDetails(enrichedMetadata));
        try {
            auditoriaOperacaoJpaRepository.save(entity);
        } catch (RuntimeException ex) {
            throw new PersistenciaAuditoriaObrigatoriaException(
                    "Falha ao persistir a trilha auditavel obrigatoria da operacao",
                    ex);
        }
    }

    private boolean isAuditableAction(String action) {
        if (action == null) {
            return false;
        }
        return switch (action) {
            case "create", "patch", "cancel", "approval-decision", "approval-decision-request",
                    "create-observacao", "update-observacao", "delete-observacao", "system-observacao",
                    "read", "list" ->
                true;
            default -> false;
        };
    }

    private boolean requiresPersistentTrail(String result) {
        String normalized = result == null ? "" : result.toLowerCase();
        return !normalized.contains("fail") && !normalized.contains("deny");
    }

    private boolean mustFailClosed(
            String action,
            String resourceType,
            String resourceId,
            UUID organizationId,
            UUID eventId,
            String result) {
        return false;
    }

    private boolean isObservationAction(String action) {
        return "create-observacao".equals(action)
                || "update-observacao".equals(action)
                || "delete-observacao".equals(action)
                || "system-observacao".equals(action);
    }

    private String resolveResourceType(String action, Map<String, Object> metadata) {
        Object explicit = metadata.get("resourceType");
        if (explicit != null && !String.valueOf(explicit).isBlank()) {
            return String.valueOf(explicit).toUpperCase();
        }
        if (action != null && action.contains("observacao")) {
            return "OBSERVACAO";
        }
        if (action != null && action.startsWith("approval")) {
            return "APROVACAO";
        }
        return "EVENTO";
    }

    private String resolveResourceId(String target, Map<String, Object> metadata, String resourceType) {
        Object explicit = firstNonNull(
                metadata.get("resourceId"),
                metadata.get("observacaoId"),
                metadata.get("aprovacaoId"),
                metadata.get("solicitacaoAprovacaoId"),
                metadata.get("eventoId"));
        if (explicit != null && !String.valueOf(explicit).isBlank()) {
            return String.valueOf(explicit);
        }
        if (target != null && isUuid(target)) {
            return target;
        }
        if ("OBSERVACAO".equals(resourceType) && metadata.get("observacaoId") != null) {
            return String.valueOf(metadata.get("observacaoId"));
        }
        return null;
    }

    private UUID resolveEventId(String target, Map<String, Object> metadata, String resourceType, String resourceId) {
        UUID explicit = parseUuid(metadata.get("eventoId"));
        if (explicit != null) {
            return explicit;
        }
        if ("EVENTO".equals(resourceType)) {
            return parseUuid(resourceId);
        }
        if ("OBSERVACAO".equals(resourceType)) {
            UUID targetEventId = parseUuid(target);
            if (targetEventId != null) {
                return targetEventId;
            }
            UUID observacaoId = parseUuid(resourceId);
            if (observacaoId != null) {
                return observacaoEventoJpaRepository.findById(observacaoId)
                        .map(ObservacaoEventoEntity::getEventoId)
                        .orElse(null);
            }
        }
        if ("APROVACAO".equals(resourceType)) {
            UUID aprovacaoId = parseUuid(resourceId);
            if (aprovacaoId != null) {
                return aprovacaoJpaRepository.findById(aprovacaoId)
                        .map(AprovacaoEntity::getEventoId)
                        .orElse(null);
            }
        }
        return null;
    }

    private UUID resolveOrganizationId(
            Map<String, Object> metadata,
            String resourceType,
            String resourceId,
            UUID eventId) {
        UUID explicit = parseUuid(firstNonNull(metadata.get("organizacaoId"), metadata.get("organizationId")));
        if (explicit != null) {
            return explicit;
        }
        if (eventId != null) {
            return eventoJpaRepository.findById(eventId)
                    .map(evento -> evento.getOrganizacaoResponsavelId())
                    .orElse(null);
        }
        if ("EVENTO".equals(resourceType)) {
            UUID id = parseUuid(resourceId);
            if (id != null) {
                return eventoJpaRepository.findById(id)
                        .map(evento -> evento.getOrganizacaoResponsavelId())
                        .orElse(null);
            }
        }
        if ("APROVACAO".equals(resourceType)) {
            UUID aprovacaoId = parseUuid(resourceId);
            if (aprovacaoId != null) {
                UUID organizationIdFromApproval = aprovacaoJpaRepository.findById(aprovacaoId)
                        .map(this::resolveOrganizationIdFromApproval)
                        .orElse(null);
                if (organizationIdFromApproval != null) {
                    return organizationIdFromApproval;
                }
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails) {
            return usuarioDetails.primaryMembership()
                    .map(br.com.nsfatima.calendario.infrastructure.security.ExternalMembershipReader.Membership::organizationId)
                    .orElse(null);
        }
        return null;
    }

    private UUID resolveActorUserId(Map<String, Object> metadata) {
        UUID explicit = parseUuid(firstNonNull(metadata.get("userId"), metadata.get("atorUsuarioId")));
        if (explicit != null) {
            return explicit;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails) {
            return usuarioDetails.getUsuarioId();
        }
        return null;
    }

    private String writeDetails(Map<String, Object> enrichedMetadata) {
        try {
            return objectMapper.writeValueAsString(enrichedMetadata);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private UUID parseUuid(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(String.valueOf(value));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isUuid(String value) {
        return parseUuid(value) != null;
    }

    private UUID resolveOrganizationIdFromApproval(AprovacaoEntity aprovacao) {
        if (approvalActionPayloadMapper == null
                || aprovacao == null
                || aprovacao.getActionPayloadJson() == null
                || aprovacao.getActionPayloadJson().isBlank()) {
            return null;
        }
        try {
            ApprovalActionPayload payload = approvalActionPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
            return payload.organizacaoResponsavelId();
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
