package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.dto.metrics.AuditoriaOperacaoResponse;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.AuditLogQueryService;
import br.com.nsfatima.gestao.calendario.infrastructure.security.UsuarioDetails;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ListAuditTrailUseCase {

    private final AuditLogQueryService auditLogQueryService;
    private final PeriodoOperacionalPolicy periodoOperacionalPolicy;

    public ListAuditTrailUseCase(
            AuditLogQueryService auditLogQueryService,
            PeriodoOperacionalPolicy periodoOperacionalPolicy) {
        this.auditLogQueryService = auditLogQueryService;
        this.periodoOperacionalPolicy = periodoOperacionalPolicy;
    }

    public AuditoriaOperacaoResponse execute(
            UUID organizacaoId,
            String granularidade,
            Instant inicio,
            Instant fim,
            String ator,
            String acao,
            String resultado,
            String correlationId) {
        assertAccessToOrganization(organizacaoId);
        PeriodoOperacionalPolicy.ResolvedPeriod periodo = periodoOperacionalPolicy.resolve(granularidade, inicio, fim);

        List<AuditoriaOperacaoResponse.RegistroAuditavelItem> items = auditLogQueryService.findAuditTrail(
                organizacaoId,
                periodo.inicio(),
                periodo.fim(),
                ator,
                acao,
                resultado,
                correlationId);

        return new AuditoriaOperacaoResponse(organizacaoId, periodo.toResponse(), items);
    }

    private void assertAccessToOrganization(UUID organizacaoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            throw new AccessDeniedException("Authenticated user required");
        }
        if (usuarioDetails.findMembershipByOrganization(organizacaoId).isEmpty()) {
            throw new AccessDeniedException("User cannot query audit data outside the responsible organization scope");
        }
    }
}
