package br.com.nsfatima.calendario.api.dto.metrics;

import java.time.Instant;

public record PeriodoOperacionalResponse(
        String granularidade,
        Instant inicio,
        Instant fim) {
}
