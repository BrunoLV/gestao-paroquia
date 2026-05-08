package br.com.nsfatima.gestao.local.api.v1.controller;

import br.com.nsfatima.gestao.local.api.v1.dto.LocalRequest;
import br.com.nsfatima.gestao.local.api.v1.dto.LocalResponse;
import br.com.nsfatima.gestao.local.domain.model.Local;
import br.com.nsfatima.gestao.local.domain.service.LocalService;
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
@RequestMapping("/api/v1/locais")
@Tag(name = "Locais", description = "Gerenciamento de locais da paróquia")
public class LocalController {

    private final LocalService localService;

    public LocalController(LocalService localService) {
        this.localService = localService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar um novo local", description = "Apenas administradores podem criar locais.")
    public LocalResponse create(@RequestBody @Valid LocalRequest request) {
        Local local = localService.createLocal(
                request.nome(),
                request.tipo(),
                request.endereco(),
                request.capacidade(),
                request.caracteristicas()
        );
        return toResponse(local);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar um local existente", description = "Apenas administradores podem atualizar locais.")
    public LocalResponse update(@PathVariable UUID id, @RequestBody @Valid LocalRequest request) {
        Local local = localService.updateLocal(
                id,
                request.nome(),
                request.tipo(),
                request.endereco(),
                request.capacidade(),
                request.caracteristicas(),
                request.ativo()
        );
        return toResponse(local);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir um local", description = "Apenas administradores podem excluir locais. Não é possível excluir locais em uso.")
    public void delete(@PathVariable UUID id) {
        localService.deleteLocal(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter detalhes de um local")
    public ResponseEntity<LocalResponse> get(@PathVariable UUID id) {
        return localService.getLocal(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar todos os locais")
    public List<LocalResponse> list() {
        return localService.listLocais().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private LocalResponse toResponse(Local domain) {
        return new LocalResponse(
                domain.getId(),
                domain.getNome(),
                domain.getTipo(),
                domain.getEndereco(),
                domain.getCapacidade(),
                domain.getCaracteristicas(),
                domain.isAtivo()
        );
    }
}
