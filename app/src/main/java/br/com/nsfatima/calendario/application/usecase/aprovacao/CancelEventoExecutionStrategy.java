package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionResponse;
import br.com.nsfatima.calendario.api.dto.evento.EventoCanceladoResponse;
import br.com.nsfatima.calendario.application.usecase.evento.CancelEventoUseCase;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CancelEventoExecutionStrategy implements ApprovalExecutionStrategy {

    private final CancelEventoUseCase cancelEventoUseCase;
    private final EventoActorContextResolver actorContextResolver;

    public CancelEventoExecutionStrategy(
            CancelEventoUseCase cancelEventoUseCase,
            EventoActorContextResolver actorContextResolver) {
        this.cancelEventoUseCase = cancelEventoUseCase;
        this.actorContextResolver = actorContextResolver;
    }

    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.CANCELAMENTO;
    }

    @Override
    public AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        EventoCanceladoResponse response = cancelEventoUseCase.executeApprovedCancellation(
                aprovacao.getEventoId(),
                aprovacao.getMotivoCancelamentoSnapshot(),
                actorContext.actor(),
                actorContext.usuarioId());

        return new AprovacaoDecisionResponse.ActionExecution(
                "EXECUTED",
                response.id(),
                response.status(),
                null);
    }
}
