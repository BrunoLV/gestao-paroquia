package br.com.nsfatima.calendario.application.usecase.metrics;

import br.com.nsfatima.calendario.api.dto.metrics.IndicadorRetrabalhoResponse;
import org.springframework.stereotype.Service;

@Service
public class GetIndicadorRetrabalhoUseCase {

    public IndicadorRetrabalhoResponse execute(String periodo) {
        return new IndicadorRetrabalhoResponse(periodo, 0.0);
    }
}
