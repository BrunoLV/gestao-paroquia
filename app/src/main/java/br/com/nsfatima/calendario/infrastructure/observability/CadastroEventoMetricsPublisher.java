package br.com.nsfatima.calendario.infrastructure.observability;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CadastroEventoMetricsPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(CadastroEventoMetricsPublisher.class);

    private final AtomicLong successCount = new AtomicLong();
    private final AtomicLong failureCount = new AtomicLong();
    private final AtomicLong conflictPendingCount = new AtomicLong();
    private final AtomicLong replayCount = new AtomicLong();

    public void publishCadastroTempo(Duration duracao) {
        LOGGER.info("metric cadastro_evento_tempo_ms={}", duracao.toMillis());
    }

    public void publishCreateSuccess(boolean conflictPending, boolean replay) {
        long success = successCount.incrementAndGet();
        long conflict = conflictPending ? conflictPendingCount.incrementAndGet() : conflictPendingCount.get();
        long replayed = replay ? replayCount.incrementAndGet() : replayCount.get();
        LOGGER.info(
                "metric cadastro_evento outcome=success successCount={} conflictPendingCount={} replayCount={}",
                success,
                conflict,
                replayed);
    }

    public void publishCreateFailure(String category) {
        long failure = failureCount.incrementAndGet();
        LOGGER.info("metric cadastro_evento outcome=failure category={} failureCount={}", category, failure);
    }

    public Map<String, Long> snapshot() {
        return Map.of(
                "cadastroEventoSuccess", successCount.get(),
                "cadastroEventoFailure", failureCount.get(),
                "cadastroEventoConflictPending", conflictPendingCount.get(),
                "cadastroEventoReplay", replayCount.get());
    }
}
