package br.com.nsfatima.calendario.infrastructure.persistence.mapper;

import br.com.nsfatima.calendario.api.dto.projeto.ProjetoResponse;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.ProjetoEventoEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjetoMapper {

    public ProjetoResponse toResponse(ProjetoEventoEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ProjetoResponse(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                false // For now, following existing contract expectation
        );
    }

    public ProjetoResponse toResponse(ProjetoEventoEntity entity, boolean updated) {
        if (entity == null) {
            return null;
        }
        return new ProjetoResponse(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                updated
        );
    }
}
