package br.com.nsfatima.calendario.infrastructure.observability;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LegacyEnumInconsistencyPublisher {

    private final AuditLogService auditLogService;

    public LegacyEnumInconsistencyPublisher(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public void publish(String aggregateType, String aggregateId, String field, String rawValue) {
        auditLogService.log(
                "system",
                "legacy-enum-inconsistency",
                "%s:%s".formatted(aggregateType, aggregateId),
                "detected",
                Map.of(
                        "field", field,
                        "rawValue", sanitize(rawValue)));
    }

    private String sanitize(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "<empty>";
        }

        String normalized = rawValue.replaceAll("\\s+", " ").trim();
        return normalized.length() > 80 ? normalized.substring(0, 80) : normalized;
    }
}
