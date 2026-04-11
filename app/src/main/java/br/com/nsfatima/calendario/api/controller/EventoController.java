package br.com.nsfatima.calendario.api.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.evento.CreateEventoRequest;
import br.com.nsfatima.calendario.api.dto.evento.EventoResponse;
import br.com.nsfatima.calendario.api.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.calendario.application.usecase.evento.CreateEventoUseCase;
import br.com.nsfatima.calendario.application.usecase.evento.ListEventosUseCase;
import br.com.nsfatima.calendario.application.usecase.evento.UpdateEventoUseCase;
import br.com.nsfatima.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import java.time.Duration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoController {

    private final CreateEventoUseCase createEventoUseCase;
    private final ListEventosUseCase listEventosUseCase;
    private final UpdateEventoUseCase updateEventoUseCase;
    private final EventoAuditPublisher eventoAuditPublisher;
    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;

    public EventoController(
            CreateEventoUseCase createEventoUseCase,
            ListEventosUseCase listEventosUseCase,
            UpdateEventoUseCase updateEventoUseCase,
            EventoAuditPublisher eventoAuditPublisher,
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher) {
        this.createEventoUseCase = createEventoUseCase;
        this.listEventosUseCase = listEventosUseCase;
        this.updateEventoUseCase = updateEventoUseCase;
        this.eventoAuditPublisher = eventoAuditPublisher;
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventoResponse create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid CreateEventoRequest request) {
        return createEventoUseCase.execute(idempotencyKey, request);
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
    public EventoResponse patch(@PathVariable UUID eventoId, @RequestBody @Valid UpdateEventoRequest request) {
        String actor = resolveActor();
        Map<String, Object> metadata = Map.of(
                "approvalId", request.aprovacaoId() == null ? "NONE" : request.aprovacaoId().toString(),
                "sensitiveChange", request.changesSensitiveFields());
        try {
            EventoResponse response = updateEventoUseCase.execute(eventoId, request);
            eventoAuditPublisher.publish(actor, "patch", eventoId.toString(), "success", metadata);
            return response;
        } catch (RuntimeException ex) {
            eventoAuditPublisher.publish(actor, "patch", eventoId.toString(), "failure", metadata);
            throw ex;
        }
    }

    @DeleteMapping("/{eventoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID eventoId) {
        eventoAuditPublisher.publish("system", "cancel", eventoId.toString(), "success");
    }

    private String resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "anonymous";
        }
        return authentication.getName();
    }
}
