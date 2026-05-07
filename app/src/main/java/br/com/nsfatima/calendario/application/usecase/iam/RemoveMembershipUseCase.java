package br.com.nsfatima.calendario.application.usecase.iam;

import java.util.UUID;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveMembershipUseCase {

    private final MembroOrganizacaoJpaRepository membershipRepository;

    public RemoveMembershipUseCase(MembroOrganizacaoJpaRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public void execute(UUID membershipId) {
        MembroOrganizacaoEntity entity = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Membro nao encontrado: " + membershipId));

        entity.setAtivo(false);
        membershipRepository.save(entity);
    }
}
