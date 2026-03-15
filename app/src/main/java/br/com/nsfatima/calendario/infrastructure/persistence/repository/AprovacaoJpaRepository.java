package br.com.nsfatima.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AprovacaoJpaRepository extends JpaRepository<AprovacaoEntity, UUID> {

    Optional<AprovacaoEntity> findByIdAndEventoId(UUID id, UUID eventoId);
}
