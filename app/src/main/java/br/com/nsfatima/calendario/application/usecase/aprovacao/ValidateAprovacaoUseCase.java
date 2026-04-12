package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.domain.exception.ApprovalRequiredException;
import br.com.nsfatima.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.calendario.domain.type.AprovacaoStatus;
import br.com.nsfatima.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.calendario.domain.type.PapelOrganizacional;
import br.com.nsfatima.calendario.domain.type.TipoOrganizacao;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ValidateAprovacaoUseCase {

    private static final Set<AprovadorPapel> ALLOWED_APPROVER_ROLES = Set.of(
            AprovadorPapel.PAROCO,
            AprovadorPapel.CONSELHO_COORDENADOR,
            AprovadorPapel.CONSELHO_VICE_COORDENADOR);

    private final AprovacaoJpaRepository aprovacaoJpaRepository;

    public ValidateAprovacaoUseCase(AprovacaoJpaRepository aprovacaoJpaRepository) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
    }

    public void validateRequired(UUID eventoId, UUID aprovacaoId) {
        if (aprovacaoId == null) {
            throw new ApprovalRequiredException("Approval is required for date change or cancellation");
        }

        AprovacaoEntity aprovacao = aprovacaoJpaRepository
                .findByIdAndEventoId(aprovacaoId, eventoId)
                .orElseThrow(
                        () -> new ApprovalRequiredException("Approval is required for date change or cancellation"));

        if (aprovacao.getStatusEnum() != AprovacaoStatus.APROVADA) {
            throw new ApprovalRequiredException("Approval is required for date change or cancellation");
        }

        AprovadorPapel role = aprovacao.getAprovadorPapelEnum();
        if (!ALLOWED_APPROVER_ROLES.contains(role)) {
            throw new ApprovalRequiredException(
                    "Conselho coordinator, conselho vice-coordinator, or parroco approval is required");
        }
    }

    public void validateApprovalDecisionRole(String role, String organizationType) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(role);
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(organizationType);
        if (normalizedRole == PapelOrganizacional.PAROCO) {
            return;
        }
        if (normalizedOrgType == TipoOrganizacao.CONSELHO
                && (normalizedRole == PapelOrganizacional.COORDENADOR
                        || normalizedRole == PapelOrganizacional.VICE_COORDENADOR)) {
            return;
        }
        throw new ForbiddenOperationException("User does not have permission to decide event approvals");
    }
}
