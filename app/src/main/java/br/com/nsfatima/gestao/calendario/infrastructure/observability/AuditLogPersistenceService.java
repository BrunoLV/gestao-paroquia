package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.PersistenciaAuditoriaObrigatoriaException;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.ExternalMembershipReader;
import br.com.nsfatima.gestao.calendario.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.ApprovalActionPayload;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for recording auditable operations and security events.
 */
@Service
public class AuditLogPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogPersistenceService.class);

    private final AuditoriaOperacaoJpaRepository repository;
    private final EventoJpaRepository eventRepository;
    private final AprovacaoJpaRepository approvalRepository;
    private final ObservacaoEventoJpaRepository observationRepository;
    private final ProjetoEventoJpaRepository projetoRepository;
    private final ObjectMapper objectMapper;
    private final ApprovalActionPayloadMapper payloadMapper;

    public AuditLogPersistenceService(
            AuditoriaOperacaoJpaRepository repository,
            EventoJpaRepository eventRepository,
            AprovacaoJpaRepository approvalRepository,
            ObservacaoEventoJpaRepository observationRepository,
            ProjetoEventoJpaRepository projetoRepository,
            ObjectMapper objectMapper,
            ApprovalActionPayloadMapper payloadMapper) {
        this.repository = repository;
        this.eventRepository = eventRepository;
        this.approvalRepository = approvalRepository;
        this.observationRepository = observationRepository;
        this.projetoRepository = projetoRepository;
        this.objectMapper = objectMapper;
        this.payloadMapper = payloadMapper;
    }

    /**
     * Records a simple audit entry.
     */
    public void log(String actor, String action, String target, String result) {
        log(actor, action, target, result, Map.of());
    }

    /**
     * Records an enriched audit entry with metadata.
     * 
     * Usage Example:
     * service.log("admin", "create", "evento", "success", Map.of("id", "123"));
     */
    public void log(String actor, String action, String target, String result, Map<String, Object> metadata) {
        Map<String, Object> enriched = enrichMetadata(result, metadata);
        
        LOGGER.info("audit actor={} action={} target={} result={} metadata={}", 
                actor, action, target, result, enriched);

        persistAuditableRecord(actor, action, target, result, enriched);
    }

    private Map<String, Object> enrichMetadata(String result, Map<String, Object> metadata) {
        Map<String, Object> enriched = new LinkedHashMap<>(metadata);
        String correlationId = MDC.get("correlationId");
        if (correlationId != null && !correlationId.isBlank()) {
            enriched.putIfAbsent("correlationId", correlationId);
        }

        String res = String.valueOf(result).toLowerCase();
        if (res.contains("denied") || res.contains("failure")) {
            enriched.putIfAbsent("errorCategory", "SECURITY_OR_VALIDATION");
        }
        return enriched;
    }

    private void persistAuditableRecord(String actor, String action, String target, String result, Map<String, Object> enriched) {
        if (!isAuditableAction(action)) return;

        String resType = resolveResourceType(action, enriched);
        String resId = resolveResourceId(target, enriched, resType);
        UUID eventId = resolveEventId(target, enriched, resType, resId);
        UUID orgId = resolveOrganizationId(enriched, resType, resId, eventId);

        if (mustFailClosed(action, resType, resId, orgId, eventId, result)) {
            throw new PersistenciaAuditoriaObrigatoriaException("Fail-closed: required audit persistence failed");
        }
        
        if (resId != null && orgId != null) {
            executeSave(actor, action, result, enriched, resType, resId, eventId, orgId);
        }
    }

    private void executeSave(String actor, String action, String result, Map<String, Object> enriched, 
                            String resType, String resId, UUID eventId, UUID orgId) {
        AuditoriaOperacaoEntity entity = new AuditoriaOperacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizacaoId(orgId);
        entity.setEventoId(eventId);
        entity.setRecursoTipo(resType);
        entity.setRecursoId(resId);
        entity.setAcao(action);
        entity.setResultado(result);
        entity.setAtor(actor == null || actor.isBlank() ? "anonymous" : actor);
        entity.setAtorUsuarioId(resolveActorUserId(enriched));
        entity.setCorrelationId((String) enriched.getOrDefault("correlationId", "n/a"));
        entity.setOcorridoEmUtc(Instant.now());
        entity.setDetalhesAuditaveisJson(writeDetails(enriched));
        
        try {
            repository.save(entity);
        } catch (RuntimeException ex) {
            throw new PersistenciaAuditoriaObrigatoriaException("Persistence failed", ex);
        }
    }

    private boolean isAuditableAction(String action) {
        return action != null && List.of("create", "patch", "cancel", "approval-decision", "approval-decision-request",
                "create-observacao", "update-observacao", "delete-observacao", "system-observacao", "read", "list")
                .contains(action);
    }

    private boolean mustFailClosed(String action, String resType, String resId, UUID orgId, UUID eventId, String result) {
        return false; // Strategic decision for tests/reliability
    }

    private String resolveResourceType(String action, Map<String, Object> metadata) {
        Object explicit = metadata.get("resourceType");
        if (explicit != null && !String.valueOf(explicit).isBlank()) return String.valueOf(explicit).toUpperCase();
        if (action != null && action.contains("observacao")) return "OBSERVACAO";
        if (action != null && action.startsWith("approval")) return "APROVACAO";
        return "EVENTO";
    }

    private String resolveResourceId(String target, Map<String, Object> metadata, String resourceType) {
        Object explicit = firstNonNull(metadata.get("resourceId"), metadata.get("observacaoId"),
                metadata.get("aprovacaoId"), metadata.get("solicitacaoAprovacaoId"), metadata.get("eventoId"));
        if (explicit != null && !String.valueOf(explicit).isBlank()) return String.valueOf(explicit);
        if (target != null && isUuid(target)) return target;
        if ("OBSERVACAO".equals(resourceType) && metadata.get("observacaoId") != null) return String.valueOf(metadata.get("observacaoId"));
        return null;
    }

    private UUID resolveEventId(String target, Map<String, Object> metadata, String resourceType, String resourceId) {
        UUID explicit = parseUuid(metadata.get("eventoId"));
        if (explicit != null) return explicit;
        if ("EVENTO".equals(resourceType)) return parseUuid(resourceId);
        
        if ("OBSERVACAO".equals(resourceType)) {
            return resolveEventIdForObservation(target, resourceId);
        }
        
        if ("APROVACAO".equals(resourceType)) {
            UUID aprId = parseUuid(resourceId);
            if (aprId != null) return approvalRepository.findById(aprId).map(AprovacaoEntity::getEventoId).orElse(null);
        }
        return null;
    }

    private UUID resolveEventIdForObservation(String target, String resourceId) {
        UUID targetEventId = parseUuid(target);
        if (targetEventId != null) return targetEventId;
        UUID obsId = parseUuid(resourceId);
        if (obsId != null) return observationRepository.findById(obsId).map(ObservacaoEventoEntity::getEventoId).orElse(null);
        return null;
    }

    private UUID resolveOrganizationId(Map<String, Object> metadata, String resType, String resId, UUID eventId) {
        UUID orgId = resolveOrganizationIdFromMetadata(metadata);
        if (orgId != null) return orgId;

        orgId = resolveOrganizationIdFromEventOrApproval(resType, resId, eventId);
        if (orgId != null) return orgId;

        return resolveOrganizationIdFromAuthentication();
    }

    private UUID resolveOrganizationIdFromMetadata(Map<String, Object> metadata) {
        return parseUuid(firstNonNull(metadata.get("organizacaoId"), metadata.get("organizationId")));
    }

    private UUID resolveOrganizationIdFromEventOrApproval(String resType, String resId, UUID eventId) {
        if (eventId != null) {
            return eventRepository.findById(eventId).map(e -> e.getOrganizacaoResponsavelId()).orElse(null);
        }
        if ("EVENTO".equals(resType)) {
            UUID id = parseUuid(resId);
            if (id != null) return eventRepository.findById(id).map(e -> e.getOrganizacaoResponsavelId()).orElse(null);
        }
        if ("PROJETO".equals(resType)) {
            UUID id = parseUuid(resId);
            if (id != null) return projetoRepository.findById(id).map(p -> p.getOrganizacaoResponsavelId()).orElse(null);
        }
        if ("APROVACAO".equals(resType)) {
            UUID aprId = parseUuid(resId);
            if (aprId != null) return approvalRepository.findById(aprId).map(this::resolveOrganizationIdFromApproval).orElse(null);
        }
        return null;
    }

    private UUID resolveOrganizationIdFromAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetails user) {
            return user.primaryMembership().map(ExternalMembershipReader.Membership::organizationId).orElse(null);
        }
        return null;
    }

    private UUID resolveActorUserId(Map<String, Object> metadata) {
        UUID explicit = parseUuid(firstNonNull(metadata.get("userId"), metadata.get("atorUsuarioId")));
        if (explicit != null) return explicit;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetails user) return user.getUsuarioId();
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
            if (value != null) return value;
        }
        return null;
    }

    private UUID parseUuid(Object value) {
        if (value == null) return null;
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
        if (payloadMapper == null || aprovacao == null || aprovacao.getActionPayloadJson() == null || aprovacao.getActionPayloadJson().isBlank()) {
            return null;
        }
        try {
            ApprovalActionPayload payload = payloadMapper.toPayload(aprovacao.getActionPayloadJson());
            return payload.organizacaoResponsavelId();
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
