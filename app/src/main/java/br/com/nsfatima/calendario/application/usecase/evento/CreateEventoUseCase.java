package br.com.nsfatima.calendario.application.usecase.evento;

import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.CreateEventoRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.calendario.domain.type.EventoStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class CreateEventoUseCase {

    private final EventoDomainService eventoDomainService;

    public CreateEventoUseCase(EventoDomainService eventoDomainService) {
        this.eventoDomainService = eventoDomainService;
    }

    public EventoResponse execute(CreateEventoRequest request) {
        EventoStatusInput status = request.status() == null ? EventoStatusInput.RASCUNHO : request.status();
        eventoDomainService.validateEvento(request.inicio(), request.fim(), status,
                request.adicionadoExtraJustificativa());
        return new EventoResponse(
                UUID.randomUUID(),
                request.titulo(),
                request.descricao(),
                request.inicio(),
                request.fim(),
                EventoStatusResponse.fromInput(status));
    }
}
