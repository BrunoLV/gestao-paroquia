package br.com.nsfatima.calendario.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.observacao.ObservacaoCreateRequest;
import br.com.nsfatima.calendario.api.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.calendario.api.dto.observacao.ObservacaoUpdateRequest;
import br.com.nsfatima.calendario.application.usecase.observacao.CreateNotaObservacaoUseCase;
import br.com.nsfatima.calendario.application.usecase.observacao.DeleteObservacaoUseCase;
import br.com.nsfatima.calendario.application.usecase.observacao.ListMinhasObservacoesUseCase;
import br.com.nsfatima.calendario.application.usecase.observacao.ListObservacoesUseCase;
import br.com.nsfatima.calendario.application.usecase.observacao.UpdateObservacaoUseCase;
import br.com.nsfatima.calendario.infrastructure.observability.ObservacaoAuditPublisher;
import br.com.nsfatima.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/observacoes")
@Tag(name = "Observações e Colaboração", description = "Endpoints para adicionar notas e observações em eventos")
public class ObservacaoController {

    private final CreateNotaObservacaoUseCase createNotaObservacaoUseCase;
    private final ListObservacoesUseCase listObservacoesUseCase;
    private final ListMinhasObservacoesUseCase listMinhasObservacoesUseCase;
    private final UpdateObservacaoUseCase updateObservacaoUseCase;
    private final DeleteObservacaoUseCase deleteObservacaoUseCase;
    private final ObservacaoAuditPublisher observacaoAuditPublisher;
    private final EventoActorContextResolver actorContextResolver;

    public ObservacaoController(
            CreateNotaObservacaoUseCase createNotaObservacaoUseCase,
            ListObservacoesUseCase listObservacoesUseCase,
            ListMinhasObservacoesUseCase listMinhasObservacoesUseCase,
            UpdateObservacaoUseCase updateObservacaoUseCase,
            DeleteObservacaoUseCase deleteObservacaoUseCase,
            ObservacaoAuditPublisher observacaoAuditPublisher,
            EventoActorContextResolver actorContextResolver) {
        this.createNotaObservacaoUseCase = createNotaObservacaoUseCase;
        this.listObservacoesUseCase = listObservacoesUseCase;
        this.listMinhasObservacoesUseCase = listMinhasObservacoesUseCase;
        this.updateObservacaoUseCase = updateObservacaoUseCase;
        this.deleteObservacaoUseCase = deleteObservacaoUseCase;
        this.observacaoAuditPublisher = observacaoAuditPublisher;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Adiciona uma nova observação ou nota técnica a um evento.
     * 
     * Usage Example:
     * POST /api/v1/eventos/<UUID>/observacoes
     * { "tipo": "NOTA", "conteudo": "Lembrar de levar material" }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona uma observação", description = "Cria uma nova nota ou observação vinculada ao evento informado.")
    @Transactional
    public ObservacaoResponse create(
            @Parameter(description = "ID do evento") @PathVariable UUID eventoId,
            @RequestBody @Valid ObservacaoCreateRequest request) {
        var context = actorContextResolver.resolveRequired();
        return createNotaObservacaoUseCase.execute(
                eventoId,
                context.usuarioId(),
                context.actor(),
                request.tipo(),
                request.conteudo());
    }

    /**
     * Lista todas as observações visíveis de um evento.
     * 
     * Usage Example:
     * GET /api/v1/eventos/<UUID>/observacoes
     */
    @GetMapping
    @Operation(summary = "Lista todas as observações", description = "Retorna todas as observações associadas ao evento.")
    public List<ObservacaoResponse> list(@Parameter(description = "ID do evento") @PathVariable UUID eventoId) {
        var context = actorContextResolver.resolveRequired();
        List<ObservacaoResponse> result = listObservacoesUseCase.execute(eventoId);
        observacaoAuditPublisher.publishList(
                context.actor(),
                eventoId.toString(),
                "success",
                Map.of("modo", "todas", "count", result.size()));
        return result;
    }

    /**
     * Lista apenas as observações criadas pelo usuário logado no evento.
     * 
     * Usage Example:
     * GET /api/v1/eventos/<UUID>/observacoes/minhas
     */
    @GetMapping("/minhas")
    @Operation(summary = "Lista minhas observações", description = "Retorna apenas as observações criadas pelo usuário atual no evento.")
    public List<ObservacaoResponse> listMinhas(@Parameter(description = "ID do evento") @PathVariable UUID eventoId) {
        var context = actorContextResolver.resolveRequired();
        List<ObservacaoResponse> result = listMinhasObservacoesUseCase.execute(eventoId, context.usuarioId());
        observacaoAuditPublisher.publishList(
                context.actor(),
                eventoId.toString(),
                "success",
                Map.of("modo", "minhas", "count", result.size()));
        return result;
    }

    /**
     * Atualiza o conteúdo de uma observação existente.
     * Apenas o autor pode editar sua própria observação.
     * 
     * Usage Example:
     * PATCH /api/v1/eventos/<UUID>/observacoes/<UUID>
     * { "conteudo": "Conteúdo atualizado" }
     */
    @PatchMapping("/{observacaoId}")
    @Operation(summary = "Atualiza uma observação", description = "Altera o conteúdo de uma observação existente. Requer que o ator seja o autor.")
    @Transactional
    public ObservacaoResponse update(
            @Parameter(description = "ID do evento") @PathVariable UUID eventoId,
            @Parameter(description = "ID da observação") @PathVariable UUID observacaoId,
            @RequestBody @Valid ObservacaoUpdateRequest request) {
        var context = actorContextResolver.resolveRequired();
        return updateObservacaoUseCase.execute(
                eventoId,
                observacaoId,
                context.usuarioId(),
                context.actor(),
                request.conteudo());
    }

    /**
     * Remove uma observação permanentemente.
     * Apenas o autor ou administradores podem remover.
     * 
     * Usage Example:
     * DELETE /api/v1/eventos/<UUID>/observacoes/<UUID>
     */
    @DeleteMapping("/{observacaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma observação", description = "Exclui permanentemente uma observação do sistema.")
    @Transactional
    public void delete(
            @Parameter(description = "ID do evento") @PathVariable UUID eventoId,
            @Parameter(description = "ID da observação") @PathVariable UUID observacaoId) {
        var context = actorContextResolver.resolveRequired();
        deleteObservacaoUseCase.execute(eventoId, observacaoId, context.usuarioId(), context.actor());
    }
}
