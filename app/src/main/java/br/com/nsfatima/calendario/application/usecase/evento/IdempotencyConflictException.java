package br.com.nsfatima.calendario.application.usecase.evento;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String message) {
        super(message);
    }
}
