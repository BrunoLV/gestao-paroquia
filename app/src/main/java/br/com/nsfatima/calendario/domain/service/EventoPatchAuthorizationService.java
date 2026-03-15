package br.com.nsfatima.calendario.domain.service;

import br.com.nsfatima.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EventoPatchAuthorizationService {

    private static final Set<String> ORG_EDITOR_ROLES = Set.of("coordenador", "vice-coordenador");

    public void assertCanEditGeneral(EventoActorContext actorContext, UUID eventoOrganizacaoResponsavelId) {
        String normalizedRole = normalize(actorContext.role());
        if (!ORG_EDITOR_ROLES.contains(normalizedRole)) {
            throw new ForbiddenOperationException("User does not have permission for this patch operation");
        }
        if (!eventoOrganizacaoResponsavelId.equals(actorContext.organizationId())) {
            throw new ForbiddenOperationException("User cannot edit events outside the responsible organization scope");
        }
    }

    public void assertCanManageParticipants(EventoActorContext actorContext, UUID eventoOrganizacaoResponsavelId) {
        assertCanEditGeneral(actorContext, eventoOrganizacaoResponsavelId);
    }

    public void assertCanChangeResponsibleOrganization(EventoActorContext actorContext) {
        String normalizedRole = normalize(actorContext.role());
        String normalizedOrgType = normalize(actorContext.organizationType());
        boolean conselhoCoordinator = "conselho".equals(normalizedOrgType)
                && ("coordenador".equals(normalizedRole) || "vice-coordenador".equals(normalizedRole));
        boolean parroco = "paroco".equals(normalizedRole);

        if (!conselhoCoordinator && !parroco) {
            throw new ForbiddenOperationException(
                    "Only conselho coordinator or parroco can change responsible organization");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
