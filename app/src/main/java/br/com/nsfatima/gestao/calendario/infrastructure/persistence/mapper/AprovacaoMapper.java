package br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.calendario.api.dto.aprovacao.AprovacaoResponse;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import org.springframework.stereotype.Component;

@Component
public class AprovacaoMapper {

    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;

    public AprovacaoMapper(LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher) {
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
    }

    public AprovacaoResponse toResponse(AprovacaoEntity entity) {
        if (entity == null) {
            return null;
        }

        return new AprovacaoResponse(
                entity.getId(),
                entity.getEventoId(),
                TipoSolicitacaoResponse.fromStoredValue(
                        entity.getTipoSolicitacao(),
                        legacyEnumInconsistencyPublisher,
                        entity.getId().toString()),
                entity.getStatus(),
                entity.getAprovadorPapel(),
                entity.getCriadoEmUtc(),
                entity.getDecididoEmUtc(),
                entity.getSolicitanteId(),
                entity.getAprovadorId(),
                entity.getMensagemErroExecucao());
    }
}
