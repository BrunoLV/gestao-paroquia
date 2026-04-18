package br.com.nsfatima.calendario.application.usecase.observacao;

import br.com.nsfatima.calendario.api.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.calendario.domain.service.ObservacaoMutationPolicyService;
import br.com.nsfatima.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.calendario.domain.type.TipoObservacaoResponse;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreateNotaObservacaoUseCase {

    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final ObservacaoMutationPolicyService observacaoMutationPolicyService;

    public CreateNotaObservacaoUseCase(
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            ObservacaoMutationPolicyService observacaoMutationPolicyService) {
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.observacaoMutationPolicyService = observacaoMutationPolicyService;
    }

    public ObservacaoResponse execute(UUID eventoId, UUID usuarioId, TipoObservacaoInput tipo, String conteudo) {
        observacaoMutationPolicyService.assertManualCreationAllowed(tipo);

        ObservacaoEventoEntity entity = new ObservacaoEventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setEventoId(eventoId);
        entity.setUsuarioId(usuarioId);
        entity.setTipo(tipo.name());
        entity.setConteudo(conteudo);
        entity.setCriadoEmUtc(Instant.now());
        entity.setRemovida(false);

        ObservacaoEventoEntity saved = observacaoEventoJpaRepository.save(entity);
        return new ObservacaoResponse(
                saved.getId(),
                saved.getEventoId(),
                saved.getUsuarioId(),
                TipoObservacaoResponse.fromInput(tipo),
                saved.getConteudo(),
                saved.getCriadoEmUtc());
    }
}
