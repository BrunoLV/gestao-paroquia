package br.com.nsfatima.calendario.api.controller;

import br.com.nsfatima.calendario.api.dto.metrics.AnoParoquialResponse;
import br.com.nsfatima.calendario.api.dto.metrics.UpdateAnoParoquialRequest;
import br.com.nsfatima.calendario.domain.service.AnoParoquialAuthorizationService;
import br.com.nsfatima.calendario.domain.type.AnoParoquialStatus;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/anos-paroquiais")
@Tag(name = "Gestão de Ano Paroquial", description = "Endpoints para controlar a trava do calendário anual")
public class AnoParoquialController {

    private final AnoParoquialJpaRepository repository;
    private final AnoParoquialAuthorizationService authorizationService;
    private final EventoActorContextResolver actorContextResolver;

    public AnoParoquialController(
            AnoParoquialJpaRepository repository,
            AnoParoquialAuthorizationService authorizationService,
            EventoActorContextResolver actorContextResolver) {
        this.repository = repository;
        this.authorizationService = authorizationService;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Lista todos os anos paroquiais registrados no sistema.
     * 
     * Usage Example:
     * GET /api/v1/anos-paroquiais
     */
    @GetMapping
    @Operation(summary = "Lista todos os anos paroquiais registrados")
    public List<AnoParoquialResponse> list() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Consulta o estado de trava de um ano específico.
     * 
     * Usage Example:
     * GET /api/v1/anos-paroquiais/2026
     */
    @GetMapping("/{ano}")
    @Operation(summary = "Consulta o estado de um ano específico")
    public ResponseEntity<AnoParoquialResponse> get(@PathVariable Integer ano) {
        return repository.findByAno(ano)
                .map(entity -> ResponseEntity.ok(toResponse(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Altera o status de um ano paroquial (Trava ou Destrava o calendário).
     * Requer privilégios de Paroco ou Coordenador do Conselho.
     * 
     * Usage Example:
     * PATCH /api/v1/anos-paroquiais/2026
     * { "status": "FECHADO" }
     */
    @PatchMapping("/{ano}")
    @Operation(summary = "Altera o status de um ano (Trava/Destrava)")
    @Transactional
    public ResponseEntity<AnoParoquialResponse> update(
            @PathVariable Integer ano,
            @Valid @RequestBody UpdateAnoParoquialRequest request) {

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanManage(actorContext);

        AnoParoquialEntity entity = repository.findByAno(ano)
                .orElseGet(() -> {
                    AnoParoquialEntity newEntity = new AnoParoquialEntity();
                    newEntity.setAno(ano);
                    return newEntity;
                });

        AnoParoquialStatus newStatus = AnoParoquialStatus.fromJson(request.status());
        entity.setStatus(newStatus.name());

        if (newStatus == AnoParoquialStatus.FECHADO && entity.getDataFechamentoUtc() == null) {
            entity.setDataFechamentoUtc(Instant.now());
        } else if (newStatus == AnoParoquialStatus.PLANEJAMENTO) {
            entity.setDataFechamentoUtc(null);
        }

        AnoParoquialEntity saved = repository.save(entity);
        return ResponseEntity.ok(toResponse(saved));
    }

    private AnoParoquialResponse toResponse(AnoParoquialEntity entity) {
        return new AnoParoquialResponse(
                entity.getAno(),
                entity.getStatus(),
                entity.getDataFechamentoUtc(),
                entity.getUpdatedAt());
    }
}
