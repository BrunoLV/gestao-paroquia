package br.com.nsfatima.calendario.application.usecase.evento;

import br.com.nsfatima.calendario.api.dto.metrics.TaxaEventosExtraResponse;
import org.springframework.stereotype.Service;

@Service
public class GetTaxaEventosExtraUseCase {

    public TaxaEventosExtraResponse execute(String periodo) {
        return new TaxaEventosExtraResponse(periodo, 0.0);
    }
}
