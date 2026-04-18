package br.com.nsfatima.calendario.application.usecase.metrics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.calendario.api.dto.metrics.AuditoriaOperacaoResponse;
import br.com.nsfatima.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.UsuarioDetails;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListAuditTrailUseCase {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;
    private final PeriodoOperacionalPolicy periodoOperacionalPolicy;
    private final ObjectMapper objectMapper;

    public ListAuditTrailUseCase(
            AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository,
            PeriodoOperacionalPolicy periodoOperacionalPolicy,
            ObjectMapper objectMapper) {
        this.auditoriaOperacaoJpaRepository = auditoriaOperacaoJpaRepository;
        this.periodoOperacionalPolicy = periodoOperacionalPolicy;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
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

        List<AuditoriaOperacaoResponse.RegistroAuditavelItem> items = auditoriaOperacaoJpaRepository.findForTrail(
                organizacaoId,
                periodo.inicio(),
                periodo.fim(),
                ator,
                acao,
                resultado,
                correlationId)
                .stream()
                .map(this::toItem)
                .toList();

        return new AuditoriaOperacaoResponse(organizacaoId, periodo.toResponse(), items);
    }

    private AuditoriaOperacaoResponse.RegistroAuditavelItem toItem(AuditoriaOperacaoEntity entity) {
        return new AuditoriaOperacaoResponse.RegistroAuditavelItem(
                entity.getId(),
                entity.getOrganizacaoId(),
                entity.getRecursoTipo(),
                entity.getRecursoId(),
                entity.getAcao(),
                entity.getResultado(),
                entity.getAtor(),
                entity.getCorrelationId(),
                entity.getOcorridoEmUtc(),
                deserializeDetails(entity.getDetalhesAuditaveisJson()));
    }

    private Map<String, Object> deserializeDetails(String json) {
        try {
            return json == null || json.isBlank() ? Map.of() : objectMapper.readValue(json, MAP_TYPE);
        } catch (IOException ex) {
            return Map.of("raw", json == null ? "" : json);
        }
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
