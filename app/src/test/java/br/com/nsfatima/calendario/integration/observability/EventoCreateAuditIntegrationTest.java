package br.com.nsfatima.calendario.integration.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.calendario.application.usecase.aprovacao.ApprovalActionPayloadMapper;
import br.com.nsfatima.calendario.infrastructure.observability.AuditLogService;
import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class EventoCreateAuditIntegrationTest {

    @Test
    void shouldPublishCreateAuditForSuccessAndFailure() {
        AuditLogService auditLogService = new AuditLogService(
                mock(AuditoriaOperacaoJpaRepository.class),
                mock(EventoJpaRepository.class),
                mock(AprovacaoJpaRepository.class),
                mock(ObservacaoEventoJpaRepository.class),
                new ObjectMapper(),
                mock(ApprovalActionPayloadMapper.class));
        EventoAuditPublisher auditPublisher = new EventoAuditPublisher(auditLogService);
        CadastroEventoMetricsPublisher metricsPublisher = new CadastroEventoMetricsPublisher();

        assertDoesNotThrow(() -> auditPublisher.publishCreateSuccess("system", "evt-001", false, null));
        assertDoesNotThrow(() -> auditPublisher.publishCreateFailure("system", "VALIDATION", "invalid payload"));
        assertDoesNotThrow(() -> metricsPublisher.publishCreateSuccess(false, false));
        assertDoesNotThrow(() -> metricsPublisher.publishCreateFailure("VALIDATION"));
    }
}
