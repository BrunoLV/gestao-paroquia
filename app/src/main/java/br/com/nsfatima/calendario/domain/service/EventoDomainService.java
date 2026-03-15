package br.com.nsfatima.calendario.domain.service;

import java.time.Instant;
import br.com.nsfatima.calendario.domain.policy.CalendarIntegrityPolicy;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;
import org.springframework.stereotype.Service;

@Service
public class EventoDomainService {

    private final CalendarIntegrityPolicy calendarIntegrityPolicy;

    public EventoDomainService(CalendarIntegrityPolicy calendarIntegrityPolicy) {
        this.calendarIntegrityPolicy = calendarIntegrityPolicy;
    }

    public void validateEvento(Instant inicio, Instant fim, EventoStatusInput status, String justificativa) {
        calendarIntegrityPolicy.validateInterval(inicio, fim);
        EventoStatusInput canonicalStatus = status == null ? EventoStatusInput.RASCUNHO : status;
        if (canonicalStatus == EventoStatusInput.ADICIONADO_EXTRA
                && (justificativa == null || justificativa.isBlank())) {
            throw new IllegalArgumentException("ADICIONADO_EXTRA exige justificativa");
        }
    }
}
