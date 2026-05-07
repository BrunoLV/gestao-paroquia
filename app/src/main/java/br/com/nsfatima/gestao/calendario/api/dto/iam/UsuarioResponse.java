package br.com.nsfatima.gestao.calendario.api.dto.iam;

import java.util.UUID;

public record UsuarioResponse(
    UUID id,
    String username,
    boolean enabled
) {}
