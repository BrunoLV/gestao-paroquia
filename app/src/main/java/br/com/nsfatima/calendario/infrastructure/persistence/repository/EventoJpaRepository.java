package br.com.nsfatima.calendario.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface EventoJpaRepository extends JpaRepository<EventoEntity, UUID> {

    @Lock(LockModeType.OPTIMISTIC)
    Optional<EventoEntity> findById(UUID id);

    List<EventoEntity> findAllByOrderByInicioUtcAscIdAsc();

    boolean existsByInicioUtcLessThanAndFimUtcGreaterThan(Instant fimUtcExclusive, Instant inicioUtcExclusive);
}
