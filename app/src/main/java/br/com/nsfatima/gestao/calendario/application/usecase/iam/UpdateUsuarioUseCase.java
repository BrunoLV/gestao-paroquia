package br.com.nsfatima.gestao.calendario.application.usecase.iam;

import java.util.UUID;
import br.com.nsfatima.gestao.calendario.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;

    public UpdateUsuarioUseCase(UsuarioJpaRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void execute(UUID id, String username, Boolean enabled) {
        UsuarioEntity entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        if (username != null) {
            entity.setUsername(username);
        }
        if (enabled != null) {
            entity.setEnabled(enabled);
        }
        usuarioRepository.save(entity);
    }
}
