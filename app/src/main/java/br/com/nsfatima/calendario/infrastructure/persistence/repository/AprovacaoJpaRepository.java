package br.com.nsfatima.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AprovacaoJpaRepository extends JpaRepository<AprovacaoEntity, UUID> {

        Optional<AprovacaoEntity> findByIdAndEventoId(UUID id, UUID eventoId);

        Optional<AprovacaoEntity> findByIdAndStatusIgnoreCase(UUID id, String status);

        Optional<AprovacaoEntity> findTopByEventoIdAndTipoSolicitacaoAndStatusIgnoreCaseOrderByCriadoEmUtcDesc(
                        UUID eventoId,
                        String tipoSolicitacao,
                        String status);

        boolean existsByEventoIdAndTipoSolicitacaoAndStatusIgnoreCase(
                        UUID eventoId,
                        String tipoSolicitacao,
                        String status);

        java.util.List<AprovacaoEntity> findByTipoSolicitacaoAndStatusIgnoreCaseOrderByCriadoEmUtcAsc(
                        String tipoSolicitacao,
                        String status);

        java.util.List<AprovacaoEntity> findByStatusIgnoreCaseOrderByCriadoEmUtcAsc(String status);
}
