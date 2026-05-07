package br.com.nsfatima.gestao.calendario.api.error;

import java.util.List;

public record ValidationErrorResponse(
        String errorCode,
        String message,
        String correlationId,
        List<ValidationErrorItem> errors) {
}
