package br.com.nsfatima.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.calendario.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditoriaOperacaoJpaRepository extends JpaRepository<AuditoriaOperacaoEntity, UUID> {

    @Query(value = """
            select * from calendario.auditoria_operacoes a
            where a.organizacao_id = :organizacaoId
              and a.ocorrido_em_utc >= :inicioUtc
              and a.ocorrido_em_utc < :fimUtc
              and (cast(:ator as text) is null or a.ator = cast(:ator as text))
              and (cast(:acao as text) is null or lower(a.acao) = lower(cast(:acao as text)))
              and (cast(:resultado as text) is null or lower(a.resultado) = lower(cast(:resultado as text)))
              and (cast(:correlationId as text) is null or a.correlation_id = cast(:correlationId as text))
            order by a.ocorrido_em_utc asc, a.id asc
            """, nativeQuery = true)
    List<AuditoriaOperacaoEntity> findForTrail(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc,
            @Param("ator") String ator,
            @Param("acao") String acao,
            @Param("resultado") String resultado,
            @Param("correlationId") String correlationId);

    List<AuditoriaOperacaoEntity> findByOrganizacaoIdAndOcorridoEmUtcGreaterThanEqualAndOcorridoEmUtcLessThanOrderByOcorridoEmUtcAscIdAsc(
            UUID organizacaoId,
            Instant inicioUtc,
            Instant fimUtc);

    List<AuditoriaOperacaoEntity> findByOcorridoEmUtcGreaterThanEqualAndOcorridoEmUtcLessThanOrderByOcorridoEmUtcAscIdAsc(
            Instant inicioUtc,
            Instant fimUtc);

    @Query(value = """
            select count(*)
            from calendario.auditoria_operacoes a
            where a.organizacao_id = :organizacaoId
              and a.ocorrido_em_utc >= :inicioUtc
              and a.ocorrido_em_utc < :fimUtc
              and lower(a.resultado) in ('success', 'executed')
              and (
                    lower(a.acao) = 'cancel'
                    or (
                        lower(a.acao) = 'patch'
                        and (
                            a.detalhes_auditaveis_json like '%"scheduleChanged":true%'
                            or a.detalhes_auditaveis_json like '%"responsibleOrgChanged":true%'
                            or a.detalhes_auditaveis_json like '%"cancellation":true%'
                        )
                    )
              )
            """, nativeQuery = true)
    long countEligibleAdministrativeReworkOccurrences(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc);

    @Query(value = """
            select count(distinct a.evento_id)
            from calendario.auditoria_operacoes a
            where a.organizacao_id = :organizacaoId
              and a.ocorrido_em_utc >= :inicioUtc
              and a.ocorrido_em_utc < :fimUtc
              and a.evento_id is not null
            """, nativeQuery = true)
    long countDistinctAffectedEvents(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc);
}
