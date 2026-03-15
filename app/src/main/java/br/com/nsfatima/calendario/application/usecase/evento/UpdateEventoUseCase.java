package br.com.nsfatima.calendario.application.usecase.evento;

import java.time.Instant;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.api.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.calendario.domain.type.EventoStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class UpdateEventoUseCase {

    private final EventoDomainService eventoDomainService;

    public UpdateEventoUseCase(EventoDomainService eventoDomainService) {
        this.eventoDomainService = eventoDomainService;
    }

    public EventoResponse execute(UUID eventoId, UpdateEventoRequest request) {
        Instant inicio = request.inicio() != null ? request.inicio() : Instant.now();
        Instant fim = request.fim() != null ? request.fim() : inicio.plusSeconds(3600);
        EventoStatusInput status = request.status() != null ? request.status() : EventoStatusInput.RASCUNHO;

        eventoDomainService.validateEvento(inicio, fim, status, request.adicionadoExtraJustificativa());

        return new EventoResponse(
                eventoId,
                request.titulo(),
                request.descricao(),
                null,
                inicio,
                fim,
                EventoStatusResponse.fromInput(status),
                null,
                null);
    }
}
