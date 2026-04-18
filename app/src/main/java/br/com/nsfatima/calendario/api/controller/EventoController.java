package br.com.nsfatima.calendario.api.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.CreateEventoRequest;
import br.com.nsfatima.calendario.api.dto.evento.CancelEventoRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.api.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.calendario.application.usecase.evento.CancelEventoUseCase;
import br.com.nsfatima.calendario.application.usecase.evento.CancelEventoUseCase.CancelEventoResult;
import br.com.nsfatima.calendario.application.usecase.evento.CreateEventoUseCase;
import br.com.nsfatima.calendario.application.usecase.evento.ListEventosUseCase;
import br.com.nsfatima.calendario.application.usecase.evento.UpdateEventoUseCase;
import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import java.time.Duration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoController {

    private final CreateEventoUseCase createEventoUseCase;
    private final ListEventosUseCase listEventosUseCase;
    private final UpdateEventoUseCase updateEventoUseCase;
    private final CancelEventoUseCase cancelEventoUseCase;
    private final EventoAuditPublisher eventoAuditPublisher;
    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;

    public EventoController(
            CreateEventoUseCase createEventoUseCase,
            ListEventosUseCase listEventosUseCase,
            UpdateEventoUseCase updateEventoUseCase,
            CancelEventoUseCase cancelEventoUseCase,
            EventoAuditPublisher eventoAuditPublisher,
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher) {
        this.createEventoUseCase = createEventoUseCase;
        this.listEventosUseCase = listEventosUseCase;
        this.updateEventoUseCase = updateEventoUseCase;
        this.cancelEventoUseCase = cancelEventoUseCase;
        this.eventoAuditPublisher = eventoAuditPublisher;
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
    }

    @PostMapping
    @SuppressWarnings("null")
    public ResponseEntity<Object> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid CreateEventoRequest request) {
        CreateEventoUseCase.CreateEventoResult result = createEventoUseCase.execute(idempotencyKey, request);
        return ResponseEntity.status(result.httpStatus()).body(result.body());
    }

    @GetMapping
    public List<EventoResponse> list() {
        long startedAt = System.nanoTime();
        List<EventoResponse> response = listEventosUseCase.execute();
        cadastroEventoMetricsPublisher.publishCalendarQueryLatency(
                "/api/v1/eventos",
                Duration.ofNanos(System.nanoTime() - startedAt));
        eventoAuditPublisher.publishListSuccess("system", response.size());
        return response;
    }

    @PatchMapping("/{eventoId}")
    @Transactional
    @SuppressWarnings("null")
    public ResponseEntity<Object> patch(@PathVariable UUID eventoId, @RequestBody @Valid UpdateEventoRequest request) {
        try {
            UpdateEventoUseCase.UpdateEventoResult result = updateEventoUseCase.execute(eventoId, request);
            return ResponseEntity.status(result.httpStatus()).body(result.body());
        } catch (RuntimeException ex) {
            String actor = resolveActor();
            Map<String, Object> metadata = Map.of(
                    "sensitiveChange", request.changesSensitiveFields());
            eventoAuditPublisher.publish(actor, "patch", eventoId.toString(), "failure", metadata);
            throw ex;
        }
    }

    @DeleteMapping("/{eventoId}")
    @SuppressWarnings("null")
    public ResponseEntity<Object> cancel(
            @PathVariable UUID eventoId,
            @RequestBody @Valid CancelEventoRequest request) {
        CancelEventoResult result = cancelEventoUseCase.execute(eventoId, request);
        return ResponseEntity.status(result.httpStatus()).body(result.body());
    }

    private String resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "anonymous";
        }
        return authentication.getName();
    }
}
