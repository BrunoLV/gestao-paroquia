package br.com.nsfatima.calendario.api.controller;

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
import br.com.nsfatima.calendario.infrastructure.security.UsuarioDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class ObservacaoController {

    private final CreateNotaObservacaoUseCase createNotaObservacaoUseCase;
    private final ListObservacoesUseCase listObservacoesUseCase;
    private final ListMinhasObservacoesUseCase listMinhasObservacoesUseCase;
    private final UpdateObservacaoUseCase updateObservacaoUseCase;
    private final DeleteObservacaoUseCase deleteObservacaoUseCase;
    private final ObservacaoAuditPublisher observacaoAuditPublisher;

    public ObservacaoController(
            CreateNotaObservacaoUseCase createNotaObservacaoUseCase,
            ListObservacoesUseCase listObservacoesUseCase,
            ListMinhasObservacoesUseCase listMinhasObservacoesUseCase,
            UpdateObservacaoUseCase updateObservacaoUseCase,
            DeleteObservacaoUseCase deleteObservacaoUseCase,
            ObservacaoAuditPublisher observacaoAuditPublisher) {
        this.createNotaObservacaoUseCase = createNotaObservacaoUseCase;
        this.listObservacoesUseCase = listObservacoesUseCase;
        this.listMinhasObservacoesUseCase = listMinhasObservacoesUseCase;
        this.updateObservacaoUseCase = updateObservacaoUseCase;
        this.deleteObservacaoUseCase = deleteObservacaoUseCase;
        this.observacaoAuditPublisher = observacaoAuditPublisher;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ObservacaoResponse create(@PathVariable UUID eventoId, @RequestBody @Valid ObservacaoCreateRequest request) {
        UUID usuarioId = resolveUsuarioId();
        return createNotaObservacaoUseCase.execute(
                eventoId,
                usuarioId,
                resolveActor(),
                request.tipo(),
                request.conteudo());
    }

    @GetMapping
    public List<ObservacaoResponse> list(@PathVariable UUID eventoId) {
        List<ObservacaoResponse> result = listObservacoesUseCase.execute(eventoId);
        observacaoAuditPublisher.publishList(
                resolveActor(),
                eventoId.toString(),
                "success",
                Map.of("modo", "todas", "count", result.size()));
        return result;
    }

    @GetMapping("/minhas")
    public List<ObservacaoResponse> listMinhas(@PathVariable UUID eventoId) {
        UUID usuarioId = resolveUsuarioId();
        List<ObservacaoResponse> result = listMinhasObservacoesUseCase.execute(eventoId, usuarioId);
        observacaoAuditPublisher.publishList(
                resolveActor(),
                eventoId.toString(),
                "success",
                Map.of("modo", "minhas", "count", result.size()));
        return result;
    }

    @PatchMapping("/{observacaoId}")
    @Transactional
    public ObservacaoResponse update(
            @PathVariable UUID eventoId,
            @PathVariable UUID observacaoId,
            @RequestBody @Valid ObservacaoUpdateRequest request) {
        UUID usuarioId = resolveUsuarioId();
        return updateObservacaoUseCase.execute(
                eventoId,
                observacaoId,
                usuarioId,
                resolveActor(),
                request.conteudo());
    }

    @DeleteMapping("/{observacaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable UUID eventoId, @PathVariable UUID observacaoId) {
        UUID usuarioId = resolveUsuarioId();
        deleteObservacaoUseCase.execute(eventoId, observacaoId, usuarioId, resolveActor());
    }

    private UUID resolveUsuarioId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails) {
            return usuarioDetails.getUsuarioId();
        }
        throw new AccessDeniedException("Authenticated user required");
    }

    private String resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "anonymous";
        }
        return authentication.getName();
    }
}
