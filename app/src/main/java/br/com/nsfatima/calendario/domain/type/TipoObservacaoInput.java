package br.com.nsfatima.calendario.domain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import br.com.nsfatima.calendario.api.dto.support.EnumRequestNormalizer;

public enum TipoObservacaoInput {
    NOTA,
    JUSTIFICATIVA,
    APROVACAO,
    REPROVACAO,
    CANCELAMENTO,
    AJUSTE_HORARIO;

    @JsonCreator
    public static TipoObservacaoInput fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, TipoObservacaoInput.class);
    }
}
