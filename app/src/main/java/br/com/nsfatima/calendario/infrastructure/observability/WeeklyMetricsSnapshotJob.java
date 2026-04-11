package br.com.nsfatima.calendario.infrastructure.observability;

import java.time.Instant;
import java.util.Map;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeeklyMetricsSnapshotJob {

    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;

    public WeeklyMetricsSnapshotJob(CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher) {
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
    }

    @Scheduled(cron = "0 0 1 * * MON", zone = "UTC")
    public Map<String, Object> snapshot() {
        Map<String, Object> cadastroMetrics = cadastroEventoMetricsPublisher.snapshot();
        return Map.of(
                "capturedAt", Instant.now().toString(),
                "status", "ok",
                "cadastroEvento", cadastroMetrics);
    }
}
