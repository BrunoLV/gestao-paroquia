package br.com.nsfatima.calendario.application.usecase.aprovacao;

import br.com.nsfatima.calendario.domain.exception.ApprovalRequiredException;
import br.com.nsfatima.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ValidateAprovacaoUseCase {

    private static final Set<String> ALLOWED_APPROVER_ROLES = Set.of(
            "paroco",
            "conselho-coordenador",
            "conselho-vice-coordenador");

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

        if (!"APROVADA".equalsIgnoreCase(aprovacao.getStatus())) {
            throw new ApprovalRequiredException("Approval is required for date change or cancellation");
        }

        String role = normalize(aprovacao.getAprovadorPapel());
        if (!ALLOWED_APPROVER_ROLES.contains(role)) {
            throw new ApprovalRequiredException(
                    "Conselho coordinator, conselho vice-coordinator, or parroco approval is required");
        }
    }

    public void validateApprovalDecisionRole(String role, String organizationType) {
        String normalizedRole = normalize(role);
        String normalizedOrgType = normalize(organizationType);
        if ("paroco".equals(normalizedRole)) {
            return;
        }
        if ("conselho".equals(normalizedOrgType)
                && ("coordenador".equals(normalizedRole) || "vice-coordenador".equals(normalizedRole))) {
            return;
        }
        throw new ForbiddenOperationException("User does not have permission to decide event approvals");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
