package br.com.nsfatima.calendario.infrastructure.security;

import java.util.UUID;

public record EventoActorContext(
        String actor,
        String role,
        String organizationType,
        UUID organizationId) {
}
