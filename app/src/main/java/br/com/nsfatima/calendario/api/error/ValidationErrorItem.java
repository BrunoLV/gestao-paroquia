package br.com.nsfatima.calendario.api.error;

public record ValidationErrorItem(
        String code,
        String field,
        String message,
        String rejectedValue) {
}
