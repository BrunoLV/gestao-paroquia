package br.com.nsfatima.calendario.integration.eventos;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoResponse;
import br.com.nsfatima.calendario.application.usecase.aprovacao.CreateSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.calendario.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AprovacaoHorarioIntegrationTest {

    @Test
    void shouldCreateApprovalRequest() {
        EventoActorContextResolver resolver = mock(EventoActorContextResolver.class);
        when(resolver.resolveRequired()).thenReturn(new EventoActorContext(
                "tester", "coordenador", "CONSELHO", UUID.randomUUID(), UUID.randomUUID()));

        AprovacaoMapper mapper = mock(AprovacaoMapper.class);
        when(mapper.toResponse(any())).thenReturn(mock(AprovacaoResponse.class));

        CreateSolicitacaoAprovacaoUseCase useCase = new CreateSolicitacaoAprovacaoUseCase(
                mock(AprovacaoJpaRepository.class),
                resolver,
                null,
                mapper);
        assertNotNull(useCase.create(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                TipoSolicitacaoInput.ALTERACAO_HORARIO));
    }
}
