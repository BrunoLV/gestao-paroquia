package br.com.nsfatima.calendario.api.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.EventoParticipantesRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoParticipantesResponse;
import br.com.nsfatima.calendario.api.dto.evento.EventoRecorrenciaRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoRecorrenciaResponse;
import br.com.nsfatima.calendario.application.usecase.evento.ClearEventoParticipantesUseCase;
import br.com.nsfatima.calendario.application.usecase.evento.CreateEventoRecorrenciaUseCase;
import br.com.nsfatima.calendario.application.usecase.evento.UpdateEventoParticipantesUseCase;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoParticipacaoController {

    private final UpdateEventoParticipantesUseCase updateEventoParticipantesUseCase;
    private final ClearEventoParticipantesUseCase clearEventoParticipantesUseCase;
    private final CreateEventoRecorrenciaUseCase createEventoRecorrenciaUseCase;

    public EventoParticipacaoController(
            UpdateEventoParticipantesUseCase updateEventoParticipantesUseCase,
            ClearEventoParticipantesUseCase clearEventoParticipantesUseCase,
            CreateEventoRecorrenciaUseCase createEventoRecorrenciaUseCase) {
        this.updateEventoParticipantesUseCase = updateEventoParticipantesUseCase;
        this.clearEventoParticipantesUseCase = clearEventoParticipantesUseCase;
        this.createEventoRecorrenciaUseCase = createEventoRecorrenciaUseCase;
    }

    @PutMapping("/{eventoId}/participantes")
    public EventoParticipantesResponse putParticipantes(
            @PathVariable UUID eventoId,
            @RequestBody @Valid EventoParticipantesRequest payload) {
        return updateEventoParticipantesUseCase.execute(
                eventoId,
                payload.organizacoesParticipantes());
    }

    @DeleteMapping("/{eventoId}/participantes")
    public EventoParticipantesResponse clearParticipantes(@PathVariable UUID eventoId) {
        return clearEventoParticipantesUseCase.execute(eventoId);
    }

    @PutMapping("/{eventoId}/recorrencia")
    public EventoRecorrenciaResponse createRecorrencia(
            @PathVariable UUID eventoId,
            @RequestBody @Valid EventoRecorrenciaRequest payload) {
        return createEventoRecorrenciaUseCase.execute(eventoId, payload.frequencia(), payload.intervalo());
    }
}
