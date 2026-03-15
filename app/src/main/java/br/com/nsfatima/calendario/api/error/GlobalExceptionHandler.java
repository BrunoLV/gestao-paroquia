package br.com.nsfatima.calendario.api.error;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.util.List;
import br.com.nsfatima.calendario.application.usecase.evento.IdempotencyConflictException;
import br.com.nsfatima.calendario.domain.exception.ApprovalRequiredException;
import br.com.nsfatima.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.calendario.domain.exception.ForbiddenOperationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ValidationErrorItem> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationItem)
                .toList();
        return buildValidation(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Validation failed", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_ERROR,
                "Validation failed",
                List.of(toValidationItem(ex)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(),
                        "requestBody",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ValidationErrorResponse> handleIdempotencyConflict(IdempotencyConflictException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.IDEMPOTENCY_KEY_CONFLICT,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.IDEMPOTENCY_KEY_CONFLICT.name(),
                        "Idempotency-Key",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(EventoNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handleEventoNotFound(EventoNotFoundException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.EVENT_NOT_FOUND,
                "Evento not found",
                List.of(new ValidationErrorItem(
                        ErrorCodes.EVENT_NOT_FOUND.name(),
                        "eventoId",
                        "Event does not exist",
                        null)));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ValidationErrorResponse> handleForbiddenOperation(ForbiddenOperationException ex) {
        return buildValidation(
                HttpStatus.FORBIDDEN,
                ErrorCodes.FORBIDDEN,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.FORBIDDEN.name(),
                        "authorization",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ApprovalRequiredException.class)
    public ResponseEntity<ValidationErrorResponse> handleApprovalRequired(ApprovalRequiredException ex) {
        return buildValidation(
                HttpStatus.FORBIDDEN,
                ErrorCodes.APPROVAL_REQUIRED,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.APPROVAL_REQUIRED.name(),
                        "aprovacaoId",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ValidationErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.CONFLICT,
                "Concurrent update conflict",
                List.of(new ValidationErrorItem(
                        ErrorCodes.CONFLICT.name(),
                        "version",
                        "Event was updated by another transaction",
                        null)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationErrorResponse> handleUnexpected(Exception ex) {
        return buildValidation(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                "Unexpected error",
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(),
                        "requestBody",
                        "Unexpected error",
                        null)));
    }

    private ValidationErrorItem toValidationItem(FieldError error) {
        ErrorCodes code = isRequiredField(error) ? ErrorCodes.VALIDATION_REQUIRED_FIELD
                : ErrorCodes.VALIDATION_FIELD_INVALID;
        Object rejectedValue = error.getRejectedValue();
        return new ValidationErrorItem(
                code.name(),
                error.getField(),
                error.getDefaultMessage(),
                rejectedValue == null ? null : String.valueOf(rejectedValue));
    }

    private ValidationErrorItem toValidationItem(HttpMessageNotReadableException ex) {
        Throwable rootCause = ex.getMostSpecificCause();
        if (rootCause instanceof UnrecognizedPropertyException unknownPropertyException) {
            return new ValidationErrorItem(
                    ErrorCodes.VALIDATION_UNKNOWN_FIELD.name(),
                    unknownPropertyException.getPropertyName(),
                    "Unknown field '%s'".formatted(unknownPropertyException.getPropertyName()),
                    unknownPropertyException.getPropertyName());
        }

        if (rootCause instanceof InvalidFormatException invalidFormatException
                && invalidFormatException.getTargetType() != null
                && invalidFormatException.getTargetType().isEnum()) {
            return new ValidationErrorItem(
                    ErrorCodes.VALIDATION_ENUM_VALUE_INVALID.name(),
                    extractFieldName(invalidFormatException),
                    "Unsupported enum value '%s'".formatted(invalidFormatException.getValue()),
                    invalidFormatException.getValue() == null ? null
                            : String.valueOf(invalidFormatException.getValue()));
        }

        IllegalArgumentException enumNormalizationException = findCause(ex, IllegalArgumentException.class);
        if (enumNormalizationException != null
                && enumNormalizationException.getMessage() != null
                && enumNormalizationException.getMessage().startsWith("Unsupported enum value")) {
            JsonMappingException mappingException = findCause(ex, JsonMappingException.class);
            return new ValidationErrorItem(
                    ErrorCodes.VALIDATION_ENUM_VALUE_INVALID.name(),
                    extractFieldName(mappingException != null ? mappingException : ex),
                    enumNormalizationException.getMessage(),
                    extractRejectedValue(enumNormalizationException.getMessage()));
        }

        return new ValidationErrorItem(
                ErrorCodes.VALIDATION_FIELD_INVALID.name(),
                extractFieldName(rootCause),
                "Malformed JSON request",
                null);
    }

    private String extractRejectedValue(String message) {
        int firstQuote = message.indexOf('\'');
        if (firstQuote < 0) {
            return null;
        }

        int secondQuote = message.indexOf('\'', firstQuote + 1);
        if (secondQuote < 0) {
            return null;
        }

        return message.substring(firstQuote + 1, secondQuote);
    }

    private boolean isRequiredField(FieldError error) {
        String errorCode = error.getCode();
        return errorCode != null && switch (errorCode) {
            case "NotBlank", "NotNull", "NotEmpty" -> true;
            default -> false;
        };
    }

    private String extractFieldName(Throwable throwable) {
        if (throwable instanceof JsonMappingException jsonMappingException
                && !jsonMappingException.getPath().isEmpty()) {
            JsonMappingException.Reference reference = jsonMappingException.getPath().getLast();
            if (reference.getFieldName() != null && !reference.getFieldName().isBlank()) {
                return reference.getFieldName();
            }
        }
        return "requestBody";
    }

    private <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private ResponseEntity<ValidationErrorResponse> buildValidation(
            HttpStatus status,
            ErrorCodes errorCode,
            String message,
            List<ValidationErrorItem> errors) {
        return ResponseEntity.status(status.value()).body(new ValidationErrorResponse(
                errorCode.name(),
                message,
                resolveCorrelationId(),
                errors));
    }

    private String resolveCorrelationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId == null || correlationId.isBlank() ? "n/a" : correlationId;
    }
}
