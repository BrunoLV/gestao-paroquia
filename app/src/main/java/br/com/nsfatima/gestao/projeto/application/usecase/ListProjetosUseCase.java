package br.com.nsfatima.gestao.projeto.application.usecase;

import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListProjetosUseCase {

    private final ProjetoEventoJpaRepository repository;
    private final ProjetoMapper mapper;

    public ListProjetosUseCase(ProjetoEventoJpaRepository repository, ProjetoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ProjetoResponse> execute() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
