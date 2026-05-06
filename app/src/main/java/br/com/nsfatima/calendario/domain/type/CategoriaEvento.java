package br.com.nsfatima.calendario.domain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import br.com.nsfatima.calendario.api.dto.support.EnumRequestNormalizer;

/**
 * Enum que define as categorias de eventos da paróquia.
 */
public enum CategoriaEvento {
    PASTORAL,
    SOCIAL,
    LITURGICO,
    ADMINISTRATIVO,
    SACRAMENTAL,
    FORMATIVO,
    ASSISTENCIAL;

    @JsonCreator
    public static CategoriaEvento fromValue(String value) {
        return EnumRequestNormalizer.normalize(value, CategoriaEvento.class);
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
