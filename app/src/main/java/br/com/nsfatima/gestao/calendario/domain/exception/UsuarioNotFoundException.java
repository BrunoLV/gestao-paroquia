package br.com.nsfatima.gestao.calendario.domain.exception;

import java.util.UUID;

public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(UUID id) {
        super("Usuario nao encontrado: " + id);
    }
}
