package br.com.nsfatima.calendario.api.dto.metrics;

import java.util.UUID;

public record IndicadorRetrabalhoResponse(
                UUID organizacaoId,
                PeriodoOperacionalResponse periodo,
                double taxaRetrabalho,
                long numeradorOcorrenciasElegiveis,
                long denominadorEventosAfetados) {
}
