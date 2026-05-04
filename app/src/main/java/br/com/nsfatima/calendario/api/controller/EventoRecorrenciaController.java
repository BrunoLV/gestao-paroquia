package br.com.nsfatima.calendario.api.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.EventoRecorrenciaRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoRecorrenciaResponse;
import br.com.nsfatima.calendario.application.usecase.evento.CreateEventoRecorrenciaUseCase;
import br.com.nsfatima.calendario.domain.type.RegraRecorrencia;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoRecorrenciaController {

    private final CreateEventoRecorrenciaUseCase createEventoRecorrenciaUseCase;

    public EventoRecorrenciaController(CreateEventoRecorrenciaUseCase createEventoRecorrenciaUseCase) {
        this.createEventoRecorrenciaUseCase = createEventoRecorrenciaUseCase;
    }

    @PutMapping("/{eventoId}/recorrencia")
    public EventoRecorrenciaResponse createRecorrencia(
            @PathVariable UUID eventoId,
            @RequestBody @Valid EventoRecorrenciaRequest payload) {
        return createEventoRecorrenciaUseCase.execute(eventoId, new RegraRecorrencia(
                payload.frequencia() != null ? payload.frequencia().name() : "SINGLE",
                payload.intervalo(),
                payload.diasDaSemana(),
                payload.posicaoNoMes(),
                payload.posicaoNoAno(),
                payload.mesDoAno(),
                payload.diaDoMes(),
                payload.dataLimite()));
    }
}
