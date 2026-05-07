package br.com.nsfatima.calendario.application.usecase.iam;

import java.util.UUID;
import br.com.nsfatima.calendario.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangePasswordUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordUseCase(UsuarioJpaRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void execute(UUID id, String newRawPassword) {
        UsuarioEntity entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        entity.setPasswordHash(passwordEncoder.encode(newRawPassword));
        usuarioRepository.save(entity);
    }
}
