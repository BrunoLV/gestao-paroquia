package br.com.nsfatima.calendario.api.dto.evento;

import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record EventoFiltroRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant start_date,
        
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant end_date,
        
        UUID organizacao_id,
        
        UUID projeto_id) {
}
