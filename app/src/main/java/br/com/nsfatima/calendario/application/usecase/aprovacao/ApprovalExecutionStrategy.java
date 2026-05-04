package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionResponse;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;

public interface ApprovalExecutionStrategy {

    boolean supports(TipoSolicitacaoInput tipo);

    AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao, EventoActorContext actorContext);
}
