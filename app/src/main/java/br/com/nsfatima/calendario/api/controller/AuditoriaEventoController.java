package br.com.nsfatima.calendario.api.controller;

import br.com.nsfatima.calendario.api.dto.metrics.TaxaEventosExtraResponse;
import br.com.nsfatima.calendario.application.usecase.evento.GetTaxaEventosExtraUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auditoria/eventos")
public class AuditoriaEventoController {

    private final GetTaxaEventosExtraUseCase getTaxaEventosExtraUseCase;

    public AuditoriaEventoController(GetTaxaEventosExtraUseCase getTaxaEventosExtraUseCase) {
        this.getTaxaEventosExtraUseCase = getTaxaEventosExtraUseCase;
    }

    @GetMapping("/extras")
    public TaxaEventosExtraResponse getTaxaExtras(@RequestParam(defaultValue = "anual") String periodo) {
        return getTaxaEventosExtraUseCase.execute(periodo);
    }
}
