package br.com.nsfatima.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.calendario.infrastructure.persistence.entity.ObservacaoNotaRevisaoEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObservacaoNotaRevisaoJpaRepository extends JpaRepository<ObservacaoNotaRevisaoEntity, UUID> {

    List<ObservacaoNotaRevisaoEntity> findByObservacaoIdOrderByRevisadoEmUtcAscIdAsc(UUID observacaoId);
}
