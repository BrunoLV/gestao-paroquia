package br.com.nsfatima.gestao.iam.domain.service;

import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UsuarioAdminService {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioAdminService(UsuarioJpaRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void toggleActiveStatus(UUID id, boolean enabled) {
        UsuarioEntity user = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        user.setEnabled(enabled);
        usuarioRepository.save(user);
    }

    @Transactional
    public void resetPassword(UUID id, String newRawPassword) {
        UsuarioEntity user = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        user.setPasswordHash(passwordEncoder.encode(newRawPassword));
        usuarioRepository.save(user);
    }

    @Transactional
    public void updateGlobalRoles(UUID id, String roles) {
        // This project seems to store global roles as a comma-separated string in UsuarioEntity
        UsuarioEntity user = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        user.setRoles(roles);
        usuarioRepository.save(user);
    }
}
