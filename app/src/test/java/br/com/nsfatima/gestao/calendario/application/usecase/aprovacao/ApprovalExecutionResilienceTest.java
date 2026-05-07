package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.nsfatima.gestao.calendario.api.dto.aprovacao.AprovacaoDecisionRequest;
import br.com.nsfatima.gestao.calendario.domain.service.EventoCancelamentoAuthorizationService;
import br.com.nsfatima.gestao.calendario.domain.type.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.calendario.support.fake.FakeAprovacaoRepository;
import br.com.nsfatima.gestao.calendario.support.fake.FakeCadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.support.fake.FakeEventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.support.fake.FakeEventoRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ApprovalExecutionResilienceTest {

    private FakeAprovacaoRepository aprovacaoJpaRepository;
    private FakeEventoRepository eventoJpaRepository;
    private FakeEventoAuditPublisher auditPublisher;
    private FakeCadastroEventoMetricsPublisher metricsPublisher;

    @Mock
    private EventoActorContextResolver actorContextResolver;
    @Mock
    private EventoCancelamentoAuthorizationService authorizationService;
    @Mock
    private ValidateAprovacaoUseCase validateAprovacaoUseCase;
    @Mock
    private ApprovalActionPayloadMapper payloadMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DecideSolicitacaoAprovacaoUseCase decideUseCase;

    private UUID approvalId;
    private AprovacaoEntity approvalEntity;
    private List<ApprovalExecutionStrategy> strategies;

    @BeforeEach
    void setUp() {
        aprovacaoJpaRepository = new FakeAprovacaoRepository();
        eventoJpaRepository = new FakeEventoRepository();
        auditPublisher = new FakeEventoAuditPublisher();
        metricsPublisher = new FakeCadastroEventoMetricsPublisher();

        approvalId = UUID.randomUUID();
        approvalEntity = new AprovacaoEntity();
        approvalEntity.setId(approvalId);
        approvalEntity.setStatus(AprovacaoStatus.PENDENTE);
        approvalEntity.setTipoSolicitacao(TipoSolicitacaoInput.CANCELAMENTO.name());
        approvalEntity.setEventoId(UUID.randomUUID());
        aprovacaoJpaRepository.save(approvalEntity);

        EventoActorContext actorContext = new EventoActorContext(
                "admin", "coordenador", "CONSELHO", UUID.randomUUID(), UUID.randomUUID());
        when(actorContextResolver.resolveRequired()).thenReturn(actorContext);

        ApprovalExecutionStrategy cancelStrategy = mock(ApprovalExecutionStrategy.class);
        when(cancelStrategy.supports(TipoSolicitacaoInput.CANCELAMENTO)).thenReturn(true);
        strategies = List.of(cancelStrategy);

        decideUseCase = new DecideSolicitacaoAprovacaoUseCase(
                aprovacaoJpaRepository,
                eventoJpaRepository,
                actorContextResolver,
                authorizationService,
                validateAprovacaoUseCase,
                payloadMapper,
                auditPublisher,
                metricsPublisher,
                strategies,
                eventPublisher);
    }

    @Test
    @DisplayName("Should set FALHA_EXECUCAO status when automatic execution fails")
    void shouldSetFailureStatusOnExecutionError() {
        // Simulate execution error in strategy
        when(strategies.get(0).execute(any(), any()))
                .thenThrow(new RuntimeException("Database error during cancellation"));

        AprovacaoDecisionRequest request = new AprovacaoDecisionRequest(AprovacaoStatus.APROVADA, "Approve it");

        assertThrows(ApprovalExecutionFailedException.class, () -> {
            decideUseCase.decide(approvalId, request);
        });

        AprovacaoEntity saved = aprovacaoJpaRepository.findById(approvalId).get();
        assertEquals(AprovacaoStatus.FALHA_EXECUCAO, saved.getStatusEnum());
        assertEquals("Database error during cancellation", saved.getMensagemErroExecucao());
    }
}
