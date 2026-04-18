package br.com.nsfatima.calendario.infrastructure.observability;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

@Component
public class ObservacaoAuditPublisher {

    private final AuditLogService auditLogService;

    public ObservacaoAuditPublisher(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public void publish(String actor, String action, String target, String result) {
        auditLogService.log(actor, action, target, result, withDefaults(target, Map.of()));
    }

    public void publishCreate(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogService.log(actor, "create-observacao", target, result, withDefaults(target, metadata));
    }

    public void publishUpdate(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogService.log(actor, "update-observacao", target, result, withDefaults(target, metadata));
    }

    public void publishDelete(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogService.log(actor, "delete-observacao", target, result, withDefaults(target, metadata));
    }

    public void publishList(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogService.log(actor, "list-observacao", target, result, withDefaults(target, metadata));
    }

    public void publishSystem(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogService.log(actor, "system-observacao", target, result, withDefaults(target, metadata));
    }

    private Map<String, Object> withDefaults(String target, Map<String, Object> metadata) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        resolved.put("resourceType", "OBSERVACAO");
        String correlationId = MDC.get("correlationId");
        if (correlationId != null && !correlationId.isBlank()) {
            resolved.put("correlationId", correlationId);
        }
        if (target != null && !target.isBlank()) {
            resolved.put("targetEventId", target);
        }
        resolved.putAll(metadata == null ? Map.of() : metadata);
        return resolved;
    }
}
