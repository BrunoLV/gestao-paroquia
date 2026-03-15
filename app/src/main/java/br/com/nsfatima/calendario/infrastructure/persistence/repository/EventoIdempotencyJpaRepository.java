package br.com.nsfatima.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoIdempotencyJpaRepository extends JpaRepository<EventoIdempotencyEntity, String> {
}
