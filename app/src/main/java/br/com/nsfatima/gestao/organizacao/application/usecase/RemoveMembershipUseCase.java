package br.com.nsfatima.gestao.organizacao.application.usecase;

import java.util.UUID;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
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
