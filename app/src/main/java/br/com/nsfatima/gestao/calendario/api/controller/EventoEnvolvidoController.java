package br.com.nsfatima.gestao.calendario.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.api.dto.evento.EventoEnvolvidosRequest;
import br.com.nsfatima.gestao.calendario.api.dto.evento.EventoEnvolvidosResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.ClearEventoEnvolvidosUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.UpdateEventoEnvolvidosUseCase;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eventos")
@Tag(name = "Envolvidos no Evento", description = "Endpoints para gerenciar pastorais e movimentos envolvidos em um evento")
public class EventoEnvolvidoController {

    private final UpdateEventoEnvolvidosUseCase updateEventoEnvolvidosUseCase;
    private final ClearEventoEnvolvidosUseCase clearEventoEnvolvidosUseCase;
    private final EventoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;

    public EventoEnvolvidoController(
            UpdateEventoEnvolvidosUseCase updateEventoEnvolvidosUseCase,
            ClearEventoEnvolvidosUseCase clearEventoEnvolvidosUseCase,
            EventoAuditPublisher auditPublisher,
            EventoActorContextResolver actorContextResolver) {
        this.updateEventoEnvolvidosUseCase = updateEventoEnvolvidosUseCase;
        this.clearEventoEnvolvidosUseCase = clearEventoEnvolvidosUseCase;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Atualiza a lista de organizações envolvidas em um evento.
     * Substitui a lista atual pela nova lista informada.
     * 
     * Usage Example:
     * PUT /api/v1/eventos/<UUID>/envolvidos
     * { "envolvidos": [ { "organizacaoId": "...", "papel": "APOIO" } ] }
     */
    @PutMapping("/{eventoId}/envolvidos")
    @Operation(summary = "Atualiza envolvidos", description = "Substitui a lista de organizações envolvidas no evento.")
    public EventoEnvolvidosResponse putEnvolvidos(
            @Parameter(description = "ID do evento") @PathVariable UUID eventoId,
            @RequestBody @Valid EventoEnvolvidosRequest payload) {
        var context = actorContextResolver.resolveRequired();
        var response = updateEventoEnvolvidosUseCase.execute(
                eventoId,
                payload.envolvidos());
        auditPublisher.publish(context.actor(), "update-involved", eventoId.toString(), "success");
        return response;
    }

    /**
     * Remove todos os envolvidos de um evento.
     * 
     * Usage Example:
     * DELETE /api/v1/eventos/<UUID>/envolvidos
     */
    @DeleteMapping("/{eventoId}/envolvidos")
    @Operation(summary = "Remove todos os envolvidos", description = "Limpa a lista de organizações envolvidas no evento.")
    public EventoEnvolvidosResponse clearEnvolvidos(@Parameter(description = "ID do evento") @PathVariable UUID eventoId) {
        var context = actorContextResolver.resolveRequired();
        var response = clearEventoEnvolvidosUseCase.execute(eventoId);
        auditPublisher.publish(context.actor(), "clear-involved", eventoId.toString(), "success");
        return response;
    }
}
