package br.com.nsfatima.gestao.calendario.domain.exception;

import java.util.UUID;

public class ProjetoNotFoundException extends RuntimeException {
    public ProjetoNotFoundException(UUID id) {
        super("Projeto with ID " + id + " not found");
    }
}
