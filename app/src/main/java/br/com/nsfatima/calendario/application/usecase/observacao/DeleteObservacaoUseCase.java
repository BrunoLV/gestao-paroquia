package br.com.nsfatima.calendario.application.usecase.observacao;

import br.com.nsfatima.calendario.domain.service.ObservacaoMutationPolicyService;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteObservacaoUseCase {

    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final ObservacaoMutationPolicyService observacaoMutationPolicyService;

    public DeleteObservacaoUseCase(
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            ObservacaoMutationPolicyService observacaoMutationPolicyService) {
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.observacaoMutationPolicyService = observacaoMutationPolicyService;
    }

    @Transactional
    public void execute(UUID eventoId, UUID observacaoId, UUID usuarioId) {
        ObservacaoEventoEntity observacao = observacaoEventoJpaRepository
                .findByIdAndEventoId(observacaoId, eventoId)
                .orElseThrow(() -> new ObservacaoNaoEncontradaException("Observation not found"));

        observacaoMutationPolicyService.assertCanEditOrDelete(observacao, usuarioId);

        observacao.setRemovida(true);
        observacao.setRemovidaEmUtc(Instant.now());
        observacao.setRemovidaPorUsuarioId(usuarioId);
        observacaoEventoJpaRepository.save(observacao);
    }
}
