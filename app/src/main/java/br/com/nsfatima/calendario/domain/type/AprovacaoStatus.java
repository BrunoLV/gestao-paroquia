package br.com.nsfatima.calendario.domain.type;

import br.com.nsfatima.calendario.api.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum AprovacaoStatus {
    PENDENTE,
    APROVADA,
    REPROVADA;

    @JsonCreator
    public static AprovacaoStatus fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, AprovacaoStatus.class);
    }
}
