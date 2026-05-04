package br.com.nsfatima.calendario.api.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.EventoEnvolvidosRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoEnvolvidosResponse;
import br.com.nsfatima.calendario.application.usecase.evento.ClearEventoEnvolvidosUseCase;
import br.com.nsfatima.calendario.application.usecase.evento.UpdateEventoEnvolvidosUseCase;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoEnvolvidoController {

    private final UpdateEventoEnvolvidosUseCase updateEventoEnvolvidosUseCase;
    private final ClearEventoEnvolvidosUseCase clearEventoEnvolvidosUseCase;

    public EventoEnvolvidoController(
            UpdateEventoEnvolvidosUseCase updateEventoEnvolvidosUseCase,
            ClearEventoEnvolvidosUseCase clearEventoEnvolvidosUseCase) {
        this.updateEventoEnvolvidosUseCase = updateEventoEnvolvidosUseCase;
        this.clearEventoEnvolvidosUseCase = clearEventoEnvolvidosUseCase;
    }

    @PutMapping("/{eventoId}/envolvidos")
    public EventoEnvolvidosResponse putEnvolvidos(
            @PathVariable UUID eventoId,
            @RequestBody @Valid EventoEnvolvidosRequest payload) {
        return updateEventoEnvolvidosUseCase.execute(
                eventoId,
                payload.envolvidos());
    }

    @DeleteMapping("/{eventoId}/envolvidos")
    public EventoEnvolvidosResponse clearEnvolvidos(@PathVariable UUID eventoId) {
        return clearEventoEnvolvidosUseCase.execute(eventoId);
    }
}
