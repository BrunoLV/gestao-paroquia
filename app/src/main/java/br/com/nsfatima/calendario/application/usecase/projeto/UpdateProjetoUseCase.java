package br.com.nsfatima.calendario.application.usecase.projeto;

import br.com.nsfatima.calendario.api.dto.projeto.ProjetoPatchRequest;
import br.com.nsfatima.calendario.api.dto.projeto.ProjetoResponse;
import br.com.nsfatima.calendario.domain.exception.ProjetoNotFoundException;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateProjetoUseCase {

    private final ProjetoEventoJpaRepository repository;
    private final ProjetoMapper mapper;

    public UpdateProjetoUseCase(ProjetoEventoJpaRepository repository, ProjetoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public ProjetoResponse execute(UUID id, ProjetoPatchRequest request) {
        ProjetoEventoEntity entity = repository.findById(id)
                .orElseThrow(() -> new ProjetoNotFoundException(id));

        if (request.nome() != null) {
            entity.setNome(request.nome());
        }
        if (request.descricao() != null) {
            entity.setDescricao(request.descricao());
        }

        ProjetoEventoEntity saved = repository.save(entity);
        return mapper.toResponse(saved, true);
    }
}
