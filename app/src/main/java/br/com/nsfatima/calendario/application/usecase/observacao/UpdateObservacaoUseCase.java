package br.com.nsfatima.calendario.application.usecase.observacao;

import br.com.nsfatima.calendario.api.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.calendario.domain.type.TipoObservacaoResponse;
import br.com.nsfatima.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.ObservacaoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.ObservacaoNotaRevisaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ObservacaoNotaRevisaoJpaRepository;
import br.com.nsfatima.calendario.domain.service.ObservacaoMutationPolicyService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateObservacaoUseCase {

        private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
        private final ObservacaoNotaRevisaoJpaRepository observacaoNotaRevisaoJpaRepository;
        private final ObservacaoMutationPolicyService observacaoMutationPolicyService;
        private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;
        private final ObservacaoAuditPublisher observacaoAuditPublisher;

        public UpdateObservacaoUseCase(
                        ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
                        ObservacaoNotaRevisaoJpaRepository observacaoNotaRevisaoJpaRepository,
                        ObservacaoMutationPolicyService observacaoMutationPolicyService,
                        LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher,
                        ObservacaoAuditPublisher observacaoAuditPublisher) {
                this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
                this.observacaoNotaRevisaoJpaRepository = observacaoNotaRevisaoJpaRepository;
                this.observacaoMutationPolicyService = observacaoMutationPolicyService;
                this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
                this.observacaoAuditPublisher = observacaoAuditPublisher;
        }

        @Transactional
        public ObservacaoResponse execute(UUID eventoId, UUID observacaoId, UUID usuarioId, String actor,
                        String novoConteudo) {
                ObservacaoEventoEntity observacao = observacaoEventoJpaRepository
                                .findByIdAndEventoId(observacaoId, eventoId)
                                .orElseThrow(() -> new ObservacaoNaoEncontradaException("Observation not found"));

                observacaoMutationPolicyService.assertCanEditOrDelete(observacao, usuarioId);

                ObservacaoNotaRevisaoEntity revisao = new ObservacaoNotaRevisaoEntity();
                revisao.setId(UUID.randomUUID());
                revisao.setObservacaoId(observacao.getId());
                revisao.setConteudoAnterior(observacao.getConteudo());
                revisao.setConteudoNovo(novoConteudo);
                revisao.setRevisadoPorUsuarioId(usuarioId);
                revisao.setRevisadoEmUtc(Instant.now());
                observacaoNotaRevisaoJpaRepository.save(revisao);

                observacao.setConteudo(novoConteudo);
                ObservacaoEventoEntity saved = observacaoEventoJpaRepository.save(observacao);
                observacaoAuditPublisher.publishUpdate(
                                actor,
                                eventoId.toString(),
                                "success",
                                java.util.Map.of(
                                                "observacaoId", observacaoId.toString(),
                                                "eventoId", eventoId,
                                                "revisadoPorUsuarioId", usuarioId));

                return new ObservacaoResponse(
                                saved.getId(),
                                saved.getEventoId(),
                                saved.getUsuarioId(),
                                TipoObservacaoResponse.fromStoredValue(
                                                saved.getTipo(),
                                                legacyEnumInconsistencyPublisher,
                                                saved.getEventoId().toString()),
                                saved.getConteudo(),
                                saved.getCriadoEmUtc());
        }
}
