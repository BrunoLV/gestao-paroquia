package br.com.nsfatima.calendario.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoJpaRepository extends JpaRepository<EventoEntity, UUID> {

    @Query("select e.status from EventoEntity e where e.id = :id")
    Optional<String> findStatusByIdNoLock(@Param("id") UUID id);

    List<EventoEntity> findAllByOrderByInicioUtcAscIdAsc();

    boolean existsByInicioUtcLessThanAndFimUtcGreaterThan(Instant fimUtcExclusive, Instant inicioUtcExclusive);
}
