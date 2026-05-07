package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import java.util.UUID;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ProjetoEventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoEventoJpaRepository extends JpaRepository<ProjetoEventoEntity, UUID> {
}
