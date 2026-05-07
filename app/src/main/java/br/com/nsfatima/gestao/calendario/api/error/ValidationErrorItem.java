package br.com.nsfatima.gestao.calendario.api.error;

public record ValidationErrorItem(
        String code,
        String field,
        String message,
        String rejectedValue) {
}
