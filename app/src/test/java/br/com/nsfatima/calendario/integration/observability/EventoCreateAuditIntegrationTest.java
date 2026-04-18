package br.com.nsfatima.calendario.integration.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.calendario.infrastructure.observability.AuditLogService;
import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class EventoCreateAuditIntegrationTest {

    @Test
    void shouldPublishCreateAuditForSuccessAndFailure() {
        AuditLogService auditLogService = new AuditLogService(
                new ObjectMapper(),
                mock(br.com.nsfatima.calendario.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository.class),
                mock(br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository.class),
                mock(br.com.nsfatima.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository.class),
                mock(br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository.class));
        EventoAuditPublisher auditPublisher = new EventoAuditPublisher(auditLogService);
        CadastroEventoMetricsPublisher metricsPublisher = new CadastroEventoMetricsPublisher();

        assertDoesNotThrow(() -> auditPublisher.publishCreateSuccess("system", "evt-001", false, null));
        assertDoesNotThrow(() -> auditPublisher.publishCreateFailure("system", "VALIDATION", "invalid payload"));
        assertDoesNotThrow(() -> metricsPublisher.publishCreateSuccess(false, false));
        assertDoesNotThrow(() -> metricsPublisher.publishCreateFailure("VALIDATION"));
    }
}
