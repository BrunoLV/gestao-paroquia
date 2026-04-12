package br.com.nsfatima.calendario.api.controller;

import jakarta.validation.Valid;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoCreateRequest;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionRequest;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoDecisionResponse;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoResponse;
import br.com.nsfatima.calendario.application.usecase.aprovacao.CreateSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.calendario.application.usecase.aprovacao.DecideSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.calendario.infrastructure.observability.EventoAuditPublisher;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/aprovacoes")
public class AprovacaoController {

    private final CreateSolicitacaoAprovacaoUseCase createSolicitacaoAprovacaoUseCase;
    private final DecideSolicitacaoAprovacaoUseCase decideSolicitacaoAprovacaoUseCase;
    private final EventoAuditPublisher eventoAuditPublisher;

    public AprovacaoController(
            CreateSolicitacaoAprovacaoUseCase createSolicitacaoAprovacaoUseCase,
            DecideSolicitacaoAprovacaoUseCase decideSolicitacaoAprovacaoUseCase,
            EventoAuditPublisher eventoAuditPublisher) {
        this.createSolicitacaoAprovacaoUseCase = createSolicitacaoAprovacaoUseCase;
        this.decideSolicitacaoAprovacaoUseCase = decideSolicitacaoAprovacaoUseCase;
        this.eventoAuditPublisher = eventoAuditPublisher;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AprovacaoResponse create(@RequestBody @Valid AprovacaoCreateRequest request) {
        return createSolicitacaoAprovacaoUseCase.create(
                request.eventoId(),
                request.tipoSolicitacao());
    }

    @PatchMapping("/{id}")
    public AprovacaoDecisionResponse decide(
            @PathVariable UUID id,
            @RequestBody @Valid AprovacaoDecisionRequest request) {
        eventoAuditPublisher.publish(
                "system",
                "approval-decision-request",
                id.toString(),
                "received",
                java.util.Map.of("correlationId", MDC.get("correlationId") == null ? "n/a" : MDC.get("correlationId")));
        return decideSolicitacaoAprovacaoUseCase.decide(id, request);
    }
}
