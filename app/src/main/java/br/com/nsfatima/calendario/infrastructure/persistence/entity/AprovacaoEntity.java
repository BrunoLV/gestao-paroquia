package br.com.nsfatima.calendario.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "aprovacoes", schema = "calendario")
public class AprovacaoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(name = "evento_id", nullable = false)
    private UUID eventoId;

    @Column(name = "tipo_solicitacao", nullable = false, length = 64)
    private String tipoSolicitacao;

    @Column(name = "aprovador_papel", nullable = false, length = 64)
    private String aprovadorPapel;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

    @Column(name = "decidido_em_utc")
    private Instant decididoEmUtc;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public void setEventoId(UUID eventoId) {
        this.eventoId = eventoId;
    }

    public String getTipoSolicitacao() {
        return tipoSolicitacao;
    }

    public void setTipoSolicitacao(String tipoSolicitacao) {
        this.tipoSolicitacao = tipoSolicitacao;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public void setCriadoEmUtc(Instant criadoEmUtc) {
        this.criadoEmUtc = criadoEmUtc;
    }

    public String getAprovadorPapel() {
        return aprovadorPapel;
    }

    public void setAprovadorPapel(String aprovadorPapel) {
        this.aprovadorPapel = aprovadorPapel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getDecididoEmUtc() {
        return decididoEmUtc;
    }

    public void setDecididoEmUtc(Instant decididoEmUtc) {
        this.decididoEmUtc = decididoEmUtc;
    }
}
