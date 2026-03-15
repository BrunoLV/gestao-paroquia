package br.com.nsfatima.calendario.application.usecase.aprovacao;

import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoResponse;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.calendario.domain.type.TipoSolicitacaoResponse;
import br.com.nsfatima.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import java.time.Instant;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class CreateSolicitacaoAprovacaoUseCase {

    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;
    private final AprovacaoJpaRepository aprovacaoJpaRepository;

    public CreateSolicitacaoAprovacaoUseCase(
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher,
            AprovacaoJpaRepository aprovacaoJpaRepository) {
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
    }

    @Transactional
    public AprovacaoResponse create(UUID eventoId, TipoSolicitacaoInput tipoSolicitacao) {
        String aprovadorPapel = resolveAprovadorPapel();
        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setEventoId(eventoId);
        entity.setTipoSolicitacao(tipoSolicitacao.name());
        entity.setAprovadorPapel(aprovadorPapel);
        entity.setStatus("APROVADA");
        entity.setCriadoEmUtc(Instant.now());
        entity.setDecididoEmUtc(Instant.now());
        aprovacaoJpaRepository.save(entity);

        return new AprovacaoResponse(
                entity.getId(),
                eventoId,
                TipoSolicitacaoResponse.fromStoredValue(
                        tipoSolicitacao.name(),
                        legacyEnumInconsistencyPublisher,
                        eventoId.toString()),
                entity.getStatus());
    }

    private String resolveAprovadorPapel() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "conselho-coordenador";
        }

        String role = attributes.getRequest().getHeader(EventoActorContextResolver.ACTOR_ROLE_HEADER);
        String orgType = attributes.getRequest().getHeader(EventoActorContextResolver.ACTOR_ORG_TYPE_HEADER);
        if (role == null || role.isBlank()) {
            return "conselho-coordenador";
        }

        String normalizedRole = role.trim().toLowerCase();
        String normalizedOrgType = orgType == null ? "" : orgType.trim().toLowerCase();
        if ("paroco".equals(normalizedRole)) {
            return "paroco";
        }
        if ("conselho".equals(normalizedOrgType) && "vice-coordenador".equals(normalizedRole)) {
            return "conselho-vice-coordenador";
        }
        return "conselho-coordenador";
    }
}
