package br.com.nsfatima.gestao.calendario.application.usecase.iam;

import java.util.List;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.api.dto.iam.MembershipResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListMembershipsUseCase {

    private final MembroOrganizacaoJpaRepository membershipRepository;

    public ListMembershipsUseCase(MembroOrganizacaoJpaRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> execute(UUID usuarioId) {
        return membershipRepository.findByUsuarioIdAndAtivoTrue(usuarioId).stream()
                .map(m -> new MembershipResponse(
                        m.getId(),
                        m.getOrganizacaoId(),
                        m.getTipoOrganizacao(),
                        m.getPapel(),
                        m.isAtivo()))
                .toList();
    }
}
