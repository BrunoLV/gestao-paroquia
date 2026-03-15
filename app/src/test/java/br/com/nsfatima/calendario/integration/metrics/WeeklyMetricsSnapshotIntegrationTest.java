package br.com.nsfatima.calendario.integration.metrics;

import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.WeeklyMetricsSnapshotJob;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WeeklyMetricsSnapshotIntegrationTest {

    @Test
    void shouldProduceWeeklySnapshot() {
        WeeklyMetricsSnapshotJob job = new WeeklyMetricsSnapshotJob(new CadastroEventoMetricsPublisher());
        assertEquals("ok", job.snapshot().get("status"));
        assertNotNull(job.snapshot().get("cadastroEvento"));
    }
}
