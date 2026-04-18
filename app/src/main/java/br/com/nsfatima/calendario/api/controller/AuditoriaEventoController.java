package br.com.nsfatima.calendario.api.controller;

import br.com.nsfatima.calendario.api.dto.metrics.AuditoriaOperacaoResponse;
import br.com.nsfatima.calendario.api.dto.metrics.IndicadorRetrabalhoResponse;
import br.com.nsfatima.calendario.api.dto.metrics.TaxaEventosExtraResponse;
import br.com.nsfatima.calendario.application.usecase.evento.GetTaxaEventosExtraUseCase;
import br.com.nsfatima.calendario.application.usecase.metrics.GetIndicadorRetrabalhoUseCase;
import br.com.nsfatima.calendario.application.usecase.metrics.ListAuditTrailUseCase;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auditoria/eventos")
public class AuditoriaEventoController {

    private final GetTaxaEventosExtraUseCase getTaxaEventosExtraUseCase;
    private final ListAuditTrailUseCase listAuditTrailUseCase;
    private final GetIndicadorRetrabalhoUseCase getIndicadorRetrabalhoUseCase;

    public AuditoriaEventoController(
            GetTaxaEventosExtraUseCase getTaxaEventosExtraUseCase,
            ListAuditTrailUseCase listAuditTrailUseCase,
            GetIndicadorRetrabalhoUseCase getIndicadorRetrabalhoUseCase) {
        this.getTaxaEventosExtraUseCase = getTaxaEventosExtraUseCase;
        this.listAuditTrailUseCase = listAuditTrailUseCase;
        this.getIndicadorRetrabalhoUseCase = getIndicadorRetrabalhoUseCase;
    }

    @GetMapping("/extras")
    public TaxaEventosExtraResponse getTaxaExtras(@RequestParam(defaultValue = "anual") String periodo) {
        return getTaxaEventosExtraUseCase.execute(periodo);
    }

    @GetMapping("/trilha")
    public AuditoriaOperacaoResponse getTrilha(
            @RequestParam UUID organizacaoId,
            @RequestParam(required = false) String granularidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim,
            @RequestParam(required = false) String ator,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false) String correlationId) {
        return listAuditTrailUseCase.execute(
                organizacaoId,
                granularidade,
                inicio,
                fim,
                ator,
                acao,
                resultado,
                correlationId);
    }

    @GetMapping("/retrabalho")
    public IndicadorRetrabalhoResponse getRetrabalho(
            @RequestParam UUID organizacaoId,
            @RequestParam(required = false) String granularidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim) {
        return getIndicadorRetrabalhoUseCase.execute(organizacaoId, granularidade, inicio, fim);
    }
}
