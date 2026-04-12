package br.com.nsfatima.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.calendario.domain.type.ProjetoStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "projetos_eventos", schema = "calendario")
public class ProjetoEventoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(length = 2000)
    private String descricao;

    @Column(nullable = false, length = 32)
    private String status = ProjetoStatus.ATIVO.name();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus() {
        return status;
    }

    public ProjetoStatus getStatusEnum() {
        return ProjetoStatus.fromJson(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatus(ProjetoStatus status) {
        this.status = status == null ? null : status.name();
    }
}
