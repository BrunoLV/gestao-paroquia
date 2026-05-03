package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoResponse;
import br.com.nsfatima.calendario.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListAprovacoesUseCase {

    private final AprovacaoJpaRepository repository;
    private final AprovacaoMapper mapper;

    public ListAprovacoesUseCase(AprovacaoJpaRepository repository, AprovacaoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<AprovacaoResponse> execute(UUID eventoId, String status, Pageable pageable) {
        return repository.findByFilter(eventoId, status, pageable)
                .map(mapper::toResponse);
    }
}
