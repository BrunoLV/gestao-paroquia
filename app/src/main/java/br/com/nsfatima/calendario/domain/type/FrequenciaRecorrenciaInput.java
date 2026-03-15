package br.com.nsfatima.calendario.domain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import br.com.nsfatima.calendario.api.dto.support.EnumRequestNormalizer;

public enum FrequenciaRecorrenciaInput {
    DIARIA,
    SEMANAL,
    MENSAL;

    @JsonCreator
    public static FrequenciaRecorrenciaInput fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, FrequenciaRecorrenciaInput.class);
    }
}
