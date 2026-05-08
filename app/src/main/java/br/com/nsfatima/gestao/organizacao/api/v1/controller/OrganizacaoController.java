package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.service.OrganizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/organizacoes")
@Tag(name = "Organizations", description = "Management of parish organizations (Pastorals, Movements, etc.)")
public class OrganizacaoController {

    private final OrganizacaoService organizacaoService;

    public OrganizacaoController(OrganizacaoService organizacaoService) {
        this.organizacaoService = organizacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new organization", description = "Only administrators can create organizations.")
    public OrganizacaoResponse create(@RequestBody @Valid OrganizacaoRequest request) {
        Organizacao org = organizacaoService.createOrganization(
            request.nome(),
            request.tipo(),
            request.contato()
        );
        return toResponse(org);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing organization", description = "Only administrators can update organizations.")
    public OrganizacaoResponse update(@PathVariable UUID id, @RequestBody @Valid OrganizacaoRequest request) {
        Organizacao org = organizacaoService.updateOrganization(
            id,
            request.nome(),
            request.tipo(),
            request.contato(),
            request.ativo()
        );
        return toResponse(org);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an organization", description = "Only administrators can delete organizations. Cannot delete organizations with active dependencies.")
    public void delete(@PathVariable UUID id) {
        organizacaoService.deleteOrganization(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization details")
    public ResponseEntity<OrganizacaoResponse> get(@PathVariable UUID id) {
        return organizacaoService.getOrganization(id)
            .map(this::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all organizations")
    public List<OrganizacaoResponse> list() {
        return organizacaoService.listOrganizations().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private OrganizacaoResponse toResponse(Organizacao domain) {
        return new OrganizacaoResponse(
            domain.getId(),
            domain.getNome(),
            domain.getTipo(),
            domain.getContato(),
            domain.isAtivo()
        );
    }
}
