package br.com.nsfatima.calendario.api.dto.metrics;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AuditoriaOperacaoResponse(
        UUID organizacaoId,
        PeriodoOperacionalResponse periodo,
        List<RegistroAuditavelItem> items) {

    public record RegistroAuditavelItem(
            UUID id,
            UUID organizacaoId,
            String recursoTipo,
            String recursoId,
            String acao,
            String resultado,
            String ator,
            String correlationId,
            Instant ocorridoEmUtc,
            Map<String, Object> detalhesAuditaveis) {
    }
}
