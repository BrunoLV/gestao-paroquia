package br.com.nsfatima.calendario.integration.foundation;

import br.com.nsfatima.calendario.infrastructure.observability.AuditLogPersistenceService;
import br.com.nsfatima.calendario.infrastructure.time.TimezoneConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FoundationInfrastructureTest {

    @Autowired
    private AuditLogPersistenceService auditLogPersistenceService;

    @Autowired
    private TimezoneConfig timezoneConfig;

    @Test
    void contextLoadsWithFoundationalBeans() {
        assertNotNull(auditLogPersistenceService);
        assertNotNull(timezoneConfig);
    }
}
