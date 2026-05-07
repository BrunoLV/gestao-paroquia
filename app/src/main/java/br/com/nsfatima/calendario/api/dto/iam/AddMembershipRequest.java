package br.com.nsfatima.calendario.api.dto.iam;

import java.util.UUID;
import br.com.nsfatima.calendario.domain.type.PapelOrganizacional;
import br.com.nsfatima.calendario.domain.type.TipoOrganizacao;
import jakarta.validation.constraints.NotNull;

public record AddMembershipRequest(
    @NotNull UUID organizacaoId,
    @NotNull TipoOrganizacao tipo,
    @NotNull PapelOrganizacional papel
) {}
