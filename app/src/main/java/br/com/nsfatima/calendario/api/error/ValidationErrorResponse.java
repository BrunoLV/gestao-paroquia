package br.com.nsfatima.calendario.api.error;

import java.util.List;

public record ValidationErrorResponse(
        String errorCode,
        String message,
        String correlationId,
        List<ValidationErrorItem> errors) {
}
