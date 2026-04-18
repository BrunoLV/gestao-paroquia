package br.com.nsfatima.calendario.application.usecase.observacao;

import br.com.nsfatima.calendario.api.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.calendario.domain.type.TipoObservacaoResponse;
import br.com.nsfatima.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ListMinhasObservacoesUseCase {

    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;

    public ListMinhasObservacoesUseCase(
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher) {
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
    }

    public List<ObservacaoResponse> execute(UUID eventoId, UUID usuarioId) {
        return observacaoEventoJpaRepository
                .findByEventoIdAndUsuarioIdAndRemovidaFalseOrderByCriadoEmUtcAscIdAsc(eventoId, usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ObservacaoResponse toResponse(ObservacaoEventoEntity entity) {
        return new ObservacaoResponse(
                entity.getId(),
                entity.getEventoId(),
                entity.getUsuarioId(),
                TipoObservacaoResponse.fromStoredValue(
                        entity.getTipo(),
                        legacyEnumInconsistencyPublisher,
                        entity.getEventoId().toString()),
                entity.getConteudo(),
                entity.getCriadoEmUtc());
    }
}
