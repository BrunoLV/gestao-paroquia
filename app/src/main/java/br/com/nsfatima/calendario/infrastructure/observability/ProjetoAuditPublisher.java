package br.com.nsfatima.calendario.infrastructure.observability;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

@Component
public class ProjetoAuditPublisher {

    private final AuditLogService auditLogService;

    public ProjetoAuditPublisher(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public void publish(String actor, String action, String target, String result) {
        auditLogService.log(actor, action, target, result, withDefaults(target, Map.of()));
    }

    public void publish(String actor, String action, String target, String result, Map<String, Object> metadata) {
        auditLogService.log(actor, action, target, result, withDefaults(target, metadata));
    }

    public void publishCreateSuccess(String actor, String target) {
        auditLogService.log(actor, "create", target, "success", withDefaults(target, Map.of()));
    }

    public void publishPatchSuccess(String actor, String target) {
        auditLogService.log(actor, "patch", target, "success", withDefaults(target, Map.of()));
    }

    private Map<String, Object> withDefaults(String target, Map<String, Object> metadata) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        resolved.put("resourceType", "PROJETO");
        if (target != null && !target.isBlank()) {
            resolved.put("resourceId", target);
        }
        String correlationId = MDC.get("correlationId");
        if (correlationId != null && !correlationId.isBlank()) {
            resolved.put("correlationId", correlationId);
        }
        resolved.putAll(metadata == null ? Map.of() : metadata);
        return resolved;
    }
}
