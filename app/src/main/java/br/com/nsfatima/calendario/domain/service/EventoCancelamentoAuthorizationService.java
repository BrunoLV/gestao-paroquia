package br.com.nsfatima.calendario.domain.service;

import br.com.nsfatima.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EventoCancelamentoAuthorizationService {

    private static final Set<String> LEADERSHIP_ROLES = Set.of("coordenador", "vice-coordenador");
    private static final Set<String> LOCAL_SCOPED_ORG_TYPES = Set.of("pastoral", "laicato");

    public CancelamentoRequestMode resolveRequestMode(
            EventoActorContext actorContext,
            UUID eventoOrganizacaoResponsavelId) {
        String normalizedRole = normalize(actorContext.role());
        String normalizedOrgType = normalize(actorContext.organizationType());

        if (isConselhoLeadership(normalizedRole, normalizedOrgType) || "paroco".equals(normalizedRole)) {
            return CancelamentoRequestMode.IMMEDIATE;
        }

        if ("vigario".equals(normalizedRole)) {
            return CancelamentoRequestMode.REQUIRES_APPROVAL;
        }

        if (isLocalLeadership(normalizedRole, normalizedOrgType)) {
            UUID actorOrganizationId = actorContext.organizationId();
            if (actorOrganizationId == null) {
                throw new ForbiddenOperationException("User cannot cancel events without an organization scope");
            }
            if (eventoOrganizacaoResponsavelId == null || !eventoOrganizacaoResponsavelId.equals(actorOrganizationId)) {
                throw new ForbiddenOperationException(
                        "User cannot cancel events outside the responsible organization scope");
            }
            return CancelamentoRequestMode.REQUIRES_APPROVAL;
        }

        throw new ForbiddenOperationException("User does not have permission to cancel events");
    }

    public void assertCanDecideCancellation(EventoActorContext actorContext) {
        String normalizedRole = normalize(actorContext.role());
        String normalizedOrgType = normalize(actorContext.organizationType());
        if ("paroco".equals(normalizedRole) || isConselhoLeadership(normalizedRole, normalizedOrgType)) {
            return;
        }
        throw new ForbiddenOperationException("User does not have permission to decide event cancellation approvals");
    }

    private boolean isConselhoLeadership(String role, String organizationType) {
        return "conselho".equals(organizationType) && LEADERSHIP_ROLES.contains(role);
    }

    private boolean isLocalLeadership(String role, String organizationType) {
        return LOCAL_SCOPED_ORG_TYPES.contains(organizationType) && LEADERSHIP_ROLES.contains(role);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public enum CancelamentoRequestMode {
        IMMEDIATE,
        REQUIRES_APPROVAL
    }
}
