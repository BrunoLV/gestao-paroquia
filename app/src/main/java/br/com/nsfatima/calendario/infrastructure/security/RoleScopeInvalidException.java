package br.com.nsfatima.calendario.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;

public class RoleScopeInvalidException extends AccessDeniedException {

    public RoleScopeInvalidException(String message) {
        super(message);
    }
}
