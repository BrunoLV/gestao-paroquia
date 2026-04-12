package br.com.nsfatima.calendario.domain.type;

import br.com.nsfatima.calendario.api.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProjetoStatus {
    ATIVO,
    INATIVO;

    @JsonCreator
    public static ProjetoStatus fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, ProjetoStatus.class);
    }
}
