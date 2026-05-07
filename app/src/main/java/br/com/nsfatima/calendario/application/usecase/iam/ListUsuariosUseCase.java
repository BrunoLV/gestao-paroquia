package br.com.nsfatima.calendario.application.usecase.iam;

import java.util.List;
import br.com.nsfatima.calendario.api.dto.iam.UsuarioResponse;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListUsuariosUseCase {

    private final UsuarioJpaRepository usuarioRepository;

    public ListUsuariosUseCase(UsuarioJpaRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> execute() {
        return usuarioRepository.findAll().stream()
                .map(u -> new UsuarioResponse(u.getId(), u.getUsername(), u.isEnabled()))
                .toList();
    }
}
