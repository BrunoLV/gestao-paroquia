package br.com.nsfatima.gestao.calendario.application.usecase.iam;

import java.util.UUID;
import br.com.nsfatima.gestao.calendario.api.dto.iam.UsuarioResponse;
import br.com.nsfatima.gestao.calendario.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;

    public GetUsuarioUseCase(UsuarioJpaRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse execute(UUID id) {
        return usuarioRepository.findById(id)
                .map(u -> new UsuarioResponse(u.getId(), u.getUsername(), u.isEnabled()))
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }
}
