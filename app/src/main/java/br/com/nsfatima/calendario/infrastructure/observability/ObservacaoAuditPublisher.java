package br.com.nsfatima.calendario.infrastructure.observability;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ObservacaoAuditPublisher {

    private final AuditLogService auditLogService;

    public ObservacaoAuditPublisher(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public void publish(String actor, String action, String target, String result) {
        auditLogService.log(actor, action, target, result, Map.of());
    }

    public void publishCreate(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogService.log(actor, "create-observacao", target, result, metadata);
    }

    public void publishUpdate(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogService.log(actor, "update-observacao", target, result, metadata);
    }

    public void publishDelete(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogService.log(actor, "delete-observacao", target, result, metadata);
    }

    public void publishList(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogService.log(actor, "list-observacao", target, result, metadata);
    }

    public void publishSystem(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogService.log(actor, "system-observacao", target, result, metadata);
    }
}
