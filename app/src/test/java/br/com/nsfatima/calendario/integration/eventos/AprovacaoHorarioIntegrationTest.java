package br.com.nsfatima.calendario.integration.eventos;

import br.com.nsfatima.calendario.application.usecase.aprovacao.CreateSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.calendario.infrastructure.observability.AuditLogService;
import br.com.nsfatima.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class AprovacaoHorarioIntegrationTest {

    @Test
    void shouldCreateApprovalRequest() {
        CreateSolicitacaoAprovacaoUseCase useCase = new CreateSolicitacaoAprovacaoUseCase(
                new LegacyEnumInconsistencyPublisher(new AuditLogService()),
                mock(AprovacaoJpaRepository.class));
        assertNotNull(useCase.create(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                TipoSolicitacaoInput.ALTERACAO_HORARIO));
    }
}
