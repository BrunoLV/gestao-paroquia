package br.com.nsfatima.calendario.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoJpaRepository extends JpaRepository<EventoEntity, UUID> {

    @Query("select e.status from EventoEntity e where e.id = :id")
    Optional<String> findStatusByIdNoLock(@Param("id") UUID id);

    @Query("""
            select count(distinct e.id) from EventoEntity e
            where e.organizacaoResponsavelId = :organizacaoId
              and e.id in :eventoIds
            """)
    long countDistinctByOrganizacaoResponsavelIdAndIdIn(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("eventoIds") List<UUID> eventoIds);

    @Query("""
            select e from EventoEntity e
            where (:inicioUtc is null or e.inicioUtc >= :inicioUtc)
              and (:fimUtc is null or e.fimUtc <= :fimUtc)
              and (:organizacaoId is null or e.organizacaoResponsavelId = :organizacaoId)
            """)
    Page<EventoEntity> findAllWithFilters(
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc,
            @Param("organizacaoId") UUID organizacaoId,
            Pageable pageable);

    List<EventoEntity> findAllByOrderByInicioUtcAscIdAsc();

    boolean existsByInicioUtcLessThanAndFimUtcGreaterThan(Instant fimUtcExclusive, Instant inicioUtcExclusive);

    @Query("""
            select count(e) from EventoEntity e
            where e.organizacaoResponsavelId = :organizacaoId
              and e.status = :status
              and e.inicioUtc >= :inicio
              and e.inicioUtc < :fim
            """)
    long countByOrganizacaoAndStatusAndPeriod(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("status") String status,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim);

    @Query("""
            select count(e) from EventoEntity e
            where e.organizacaoResponsavelId = :organizacaoId
              and e.inicioUtc >= :inicio
              and e.inicioUtc < :fim
            """)
    long countByOrganizacaoAndPeriod(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim);
}
