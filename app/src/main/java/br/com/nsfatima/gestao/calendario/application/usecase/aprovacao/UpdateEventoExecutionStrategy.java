package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import br.com.nsfatima.gestao.calendario.api.dto.aprovacao.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.UpdateEventoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UpdateEventoExecutionStrategy implements ApprovalExecutionStrategy {

    private final UpdateEventoUseCase updateEventoUseCase;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;

    public UpdateEventoExecutionStrategy(
            UpdateEventoUseCase updateEventoUseCase,
            ApprovalActionPayloadMapper approvalActionPayloadMapper) {
        this.updateEventoUseCase = updateEventoUseCase;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
    }

    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.EDICAO_EVENTO;
    }

    @Override
    public AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        ApprovalActionPayload payload = approvalActionPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
        UUID eventoId = payload.eventoId();
        if (eventoId == null) {
            throw new IllegalArgumentException("eventoId must be present in approval payload");
        }

        EventoResponse response = updateEventoUseCase.executeApprovedUpdate(
                eventoId,
                updateEventoUseCase.restoreFromApprovalPayload(payload));

        return new AprovacaoDecisionResponse.ActionExecution(
                "EXECUTED",
                response.id(),
                response.status() == null ? null : response.status().name(),
                null);
    }
}
