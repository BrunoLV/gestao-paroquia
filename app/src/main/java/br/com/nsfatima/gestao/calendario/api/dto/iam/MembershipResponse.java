package br.com.nsfatima.gestao.calendario.api.dto.iam;

import java.util.UUID;

public record MembershipResponse(
    UUID id,
    UUID organizacaoId,
    String tipo,
    String papel,
    boolean ativo
) {}
