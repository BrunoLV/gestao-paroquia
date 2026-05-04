package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionResponse;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.application.usecase.evento.CreateEventoUseCase;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import org.springframework.stereotype.Component;

@Component
public class CreateEventoExecutionStrategy implements ApprovalExecutionStrategy {

    private final CreateEventoUseCase createEventoUseCase;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;

    public CreateEventoExecutionStrategy(
            CreateEventoUseCase createEventoUseCase,
            ApprovalActionPayloadMapper approvalActionPayloadMapper) {
        this.createEventoUseCase = createEventoUseCase;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
    }

    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.CRIACAO_EVENTO;
    }

    @Override
    public AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        ApprovalActionPayload payload = approvalActionPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
        String idempotencyKey = payload.idempotencyKey() == null
                ? "approval-exec-" + aprovacao.getId()
                : payload.idempotencyKey();

        EventoResponse response = createEventoUseCase.executeApprovedCreation(
                createEventoUseCase.restoreFromApprovalPayload(payload),
                idempotencyKey);

        aprovacao.setEventoId(response.id());

        return new AprovacaoDecisionResponse.ActionExecution(
                "EXECUTED",
                response.id(),
                response.status() == null ? null : response.status().name(),
                null);
    }
}
