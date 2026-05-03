package br.com.nsfatima.calendario.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.calendario.infrastructure.observability.AuditLogService;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class DeniedWriteAuditIntegrationTest {

    @Test
    void shouldLogAuditOnDeniedWrite() {
        AuditLogService service = new AuditLogService(
                mock(br.com.nsfatima.calendario.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository.class),
                mock(br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository.class),
                mock(br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository.class),
                mock(br.com.nsfatima.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository.class),
                new ObjectMapper(),
                mock(br.com.nsfatima.calendario.application.usecase.aprovacao.ApprovalActionPayloadMapper.class));
        assertDoesNotThrow(() -> service.log("user", "write-denied", "evento", "ACCESS_DENIED", Map.of()));

        EventoAuditPublisher publisher = new EventoAuditPublisher(service);
        assertDoesNotThrow(() -> publisher.publishDeniedWrite("user", "evento"));
    }
}
