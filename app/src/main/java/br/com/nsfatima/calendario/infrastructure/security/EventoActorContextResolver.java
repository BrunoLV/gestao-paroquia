package br.com.nsfatima.calendario.infrastructure.security;

import br.com.nsfatima.calendario.domain.exception.ForbiddenOperationException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class EventoActorContextResolver {

    public static final String ACTOR_ROLE_HEADER = "X-Actor-Role";
    public static final String ACTOR_ORG_TYPE_HEADER = "X-Actor-Org-Type";
    public static final String ACTOR_ORG_ID_HEADER = "X-Actor-Org-Id";

    public EventoActorContext resolveRequired() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new ForbiddenOperationException("Missing request context for authorization");
        }

        String role = attributes.getRequest().getHeader(ACTOR_ROLE_HEADER);
        String orgType = attributes.getRequest().getHeader(ACTOR_ORG_TYPE_HEADER);
        String orgIdRaw = attributes.getRequest().getHeader(ACTOR_ORG_ID_HEADER);

        if (role == null || role.isBlank() || orgType == null || orgType.isBlank() || orgIdRaw == null
                || orgIdRaw.isBlank()) {
            throw new ForbiddenOperationException("Missing actor authorization headers");
        }

        UUID organizationId;
        try {
            organizationId = UUID.fromString(orgIdRaw);
        } catch (IllegalArgumentException ex) {
            throw new ForbiddenOperationException("Invalid X-Actor-Org-Id header");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null || authentication.getName() == null
                ? "anonymous"
                : authentication.getName();

        return new EventoActorContext(actor, role, orgType, organizationId);
    }
}
