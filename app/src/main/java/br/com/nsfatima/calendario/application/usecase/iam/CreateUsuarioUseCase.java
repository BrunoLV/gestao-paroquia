package br.com.nsfatima.calendario.application.usecase.iam;

import java.util.UUID;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUsuarioUseCase(UsuarioJpaRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UUID execute(String username, String rawPassword) {
        if (usuarioRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException("Username ja existe: " + username);
        }

        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername(username);
        entity.setPasswordHash(passwordEncoder.encode(rawPassword));
        entity.setEnabled(true);

        return usuarioRepository.save(entity).getId();
    }
}
