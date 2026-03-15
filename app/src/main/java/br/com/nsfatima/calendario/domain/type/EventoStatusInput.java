package br.com.nsfatima.calendario.domain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import br.com.nsfatima.calendario.api.dto.support.EnumRequestNormalizer;

public enum EventoStatusInput {
    RASCUNHO,
    CONFIRMADO,
    ADICIONADO_EXTRA,
    CANCELADO;

    @JsonCreator
    public static EventoStatusInput fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, EventoStatusInput.class);
    }
}
