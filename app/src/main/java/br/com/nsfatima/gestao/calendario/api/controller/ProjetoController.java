package br.com.nsfatima.gestao.calendario.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.api.dto.projeto.ProjetoCreateRequest;
import br.com.nsfatima.gestao.calendario.api.dto.projeto.ProjetoPatchRequest;
import br.com.nsfatima.gestao.calendario.api.dto.projeto.ProjetoResponse;
import br.com.nsfatima.gestao.calendario.api.dto.projeto.ProjetoResumoDTO;
import br.com.nsfatima.gestao.calendario.application.usecase.projeto.CreateProjetoUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.projeto.ListProjetosUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.projeto.ProjetoAgregacaoService;
import br.com.nsfatima.gestao.calendario.application.usecase.projeto.UpdateProjetoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projetos")
@Tag(name = "Projetos", description = "Endpoints para gerenciamento de projetos paroquiais")
public class ProjetoController {

    private final CreateProjetoUseCase createProjetoUseCase;
    private final ListProjetosUseCase listProjetosUseCase;
    private final UpdateProjetoUseCase updateProjetoUseCase;
    private final ProjetoAgregacaoService projetoAgregacaoService;
    private final br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher auditPublisher;
    private final br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver actorContextResolver;

    public ProjetoController(
            CreateProjetoUseCase createProjetoUseCase,
            ListProjetosUseCase listProjetosUseCase,
            UpdateProjetoUseCase updateProjetoUseCase,
            ProjetoAgregacaoService projetoAgregacaoService,
            br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher auditPublisher,
            br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver actorContextResolver) {
        this.createProjetoUseCase = createProjetoUseCase;
        this.listProjetosUseCase = listProjetosUseCase;
        this.updateProjetoUseCase = updateProjetoUseCase;
        this.projetoAgregacaoService = projetoAgregacaoService;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Registra um novo projeto paroquial.
     * 
     * Usage Example:
     * POST /api/v1/projetos
     * { "nome": "Novo Projeto", "organizacaoResponsavelId": "...", ... }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo projeto", description = "Registra um novo projeto no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ProjetoResponse create(@RequestBody @Valid ProjetoCreateRequest request) {
        var context = actorContextResolver.resolveRequired();
        var response = createProjetoUseCase.create(request);
        auditPublisher.publish(context.actor(), "create-project", response.id().toString(), "success");
        return response;
    }

    /**
     * Lista todos os projetos cadastrados.
     * 
     * Usage Example:
     * GET /api/v1/projetos
     */
    @GetMapping
    @Operation(summary = "Lista todos os projetos", description = "Retorna uma lista de todos os projetos cadastrados.")
    public List<ProjetoResponse> list() {
        return listProjetosUseCase.execute();
    }

    /**
     * Atualiza dados de um projeto existente.
     * 
     * Usage Example:
     * PATCH /api/v1/projetos/<UUID>
     * { "status": "INATIVO" }
     */
    @PatchMapping("/{projetoId}")
    @Operation(summary = "Atualiza um projeto", description = "Atualiza parcialmente os dados de um projeto existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ProjetoResponse patch(
            @Parameter(description = "ID único do projeto") @PathVariable UUID projetoId,
            @RequestBody @Valid ProjetoPatchRequest request) {
        var context = actorContextResolver.resolveRequired();
        var response = updateProjetoUseCase.execute(projetoId, request);
        auditPublisher.publish(context.actor(), "update-project", projetoId.toString(), "success");
        return response;
    }

    /**
     * Obtém um resumo consolidado da execução e saúde do projeto.
     * 
     * Usage Example:
     * GET /api/v1/projetos/<UUID>/resumo
     */
    @GetMapping("/{projetoId}/resumo")
    @Operation(summary = "Obtém resumo do projeto", description = "Retorna dados agregados de execução, colaboração e saúde temporal do projeto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ProjetoResumoDTO getResumo(
            @Parameter(description = "ID único do projeto") @PathVariable UUID projetoId) {
        return projetoAgregacaoService.obterResumo(projetoId);
    }
}
