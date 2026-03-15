package br.com.nsfatima.calendario.api.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.projeto.ProjetoCreateRequest;
import br.com.nsfatima.calendario.api.dto.projeto.ProjetoPatchRequest;
import br.com.nsfatima.calendario.api.dto.projeto.ProjetoResponse;
import br.com.nsfatima.calendario.application.usecase.projeto.CreateProjetoUseCase;
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
public class ProjetoController {

    private final CreateProjetoUseCase createProjetoUseCase;

    public ProjetoController(CreateProjetoUseCase createProjetoUseCase) {
        this.createProjetoUseCase = createProjetoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjetoResponse create(@RequestBody @Valid ProjetoCreateRequest request) {
        return createProjetoUseCase.create(request.nome(), request.descricao());
    }

    @GetMapping
    public List<ProjetoResponse> list() {
        return List.of(new ProjetoResponse(
                UUID.randomUUID(),
                "Projeto Pastoral",
                "Planejamento",
                false));
    }

    @PatchMapping("/{projetoId}")
    public ProjetoResponse patch(@PathVariable UUID projetoId, @RequestBody @Valid ProjetoPatchRequest request) {
        return new ProjetoResponse(
                projetoId,
                request.nome(),
                request.descricao(),
                true);
    }
}
