package br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.calendario.api.dto.projeto.ProjetoResponse;
import br.com.nsfatima.gestao.calendario.domain.type.ProjetoStatus;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ProjetoEventoEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjetoMapper {

    public ProjetoResponse toResponse(ProjetoEventoEntity entity) {
        return toResponse(entity, false);
    }

    public ProjetoResponse toResponse(ProjetoEventoEntity entity, boolean updated) {
        if (entity == null) {
            return null;
        }
        return new ProjetoResponse(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getOrganizacaoResponsavelId(),
                entity.getInicioUtc(),
                entity.getFimUtc(),
                entity.getStatusEnum(),
                updated
        );
    }
}
