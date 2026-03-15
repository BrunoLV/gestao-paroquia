package br.com.nsfatima.calendario.api.controller;

import jakarta.validation.Valid;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoCreateRequest;
import br.com.nsfatima.calendario.api.dto.aprovacao.AprovacaoResponse;
import br.com.nsfatima.calendario.application.usecase.aprovacao.CreateSolicitacaoAprovacaoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/aprovacoes")
public class AprovacaoController {

    private final CreateSolicitacaoAprovacaoUseCase createSolicitacaoAprovacaoUseCase;

    public AprovacaoController(CreateSolicitacaoAprovacaoUseCase createSolicitacaoAprovacaoUseCase) {
        this.createSolicitacaoAprovacaoUseCase = createSolicitacaoAprovacaoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AprovacaoResponse create(@RequestBody @Valid AprovacaoCreateRequest request) {
        return createSolicitacaoAprovacaoUseCase.create(
                request.eventoId(),
                request.tipoSolicitacao());
    }
}
