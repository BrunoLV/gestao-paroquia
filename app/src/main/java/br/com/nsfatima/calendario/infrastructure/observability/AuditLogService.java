package br.com.nsfatima.calendario.infrastructure.observability;

import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogService.class);

    public void log(String actor, String action, String target, String result, Map<String, Object> metadata) {
        Map<String, Object> enrichedMetadata = new TreeMap<>();
        if (metadata != null) {
            enrichedMetadata.putAll(metadata);
        }
        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY);
        if (correlationId != null && !correlationId.isBlank()) {
            enrichedMetadata.put("correlationId", correlationId);
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
    }
}
