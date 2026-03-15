package br.com.nsfatima.calendario.domain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import br.com.nsfatima.calendario.api.dto.support.EnumRequestNormalizer;

public enum TipoSolicitacaoInput {
    ALTERACAO_HORARIO,
    CANCELAMENTO,
    RECLASSIFICACAO,
    OUTRO;

    @JsonCreator
    public static TipoSolicitacaoInput fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, TipoSolicitacaoInput.class);
    }
}
