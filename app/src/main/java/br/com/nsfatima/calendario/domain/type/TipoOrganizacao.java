package br.com.nsfatima.calendario.domain.type;

import br.com.nsfatima.calendario.api.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoOrganizacao {
    PASTORAL,
    LAICATO,
    CLERO,
    CONSELHO;

    @JsonCreator
    public static TipoOrganizacao fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, TipoOrganizacao.class);
    }

    public static TipoOrganizacao fromStoredValue(String rawValue) {
        return rawValue == null ? null : EnumRequestNormalizer.normalize(rawValue, TipoOrganizacao.class);
    }
}
