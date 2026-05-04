package br.com.nsfatima.calendario.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.calendario.application.usecase.aprovacao.ApprovalActionPayloadMapper;
import br.com.nsfatima.calendario.infrastructure.observability.AuditLogService;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class DeniedWriteAuditIntegrationTest {

    @Test
    void shouldLogAuditOnDeniedWrite() {
        AuditLogService service = new AuditLogService(
                mock(AuditoriaOperacaoJpaRepository.class),
                mock(EventoJpaRepository.class),
                mock(AprovacaoJpaRepository.class),
                mock(ObservacaoEventoJpaRepository.class),
                new ObjectMapper(),
                mock(ApprovalActionPayloadMapper.class));
        assertDoesNotThrow(() -> service.log("user", "write-denied", "evento", "ACCESS_DENIED", Map.of()));

        EventoAuditPublisher publisher = new EventoAuditPublisher(service);
        assertDoesNotThrow(() -> publisher.publishDeniedWrite("user", "evento"));
    }
}
