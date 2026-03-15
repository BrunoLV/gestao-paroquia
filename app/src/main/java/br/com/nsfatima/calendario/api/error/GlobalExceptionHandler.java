package br.com.nsfatima.calendario.api.error;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        return buildValidation(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                List.of(toValidationItem(ex)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(),
                        "requestBody",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationErrorResponse> handleUnexpected(Exception ex) {
        return buildValidation(
                HttpStatus.INTERNAL_SERVER_ERROR,
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
            String message,
            List<ValidationErrorItem> errors) {
        return ResponseEntity.status(status.value()).body(new ValidationErrorResponse(
                ErrorCodes.VALIDATION_ERROR.name(),
                message,
                resolveCorrelationId(),
                errors));
    }

    private String resolveCorrelationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId == null || correlationId.isBlank() ? "n/a" : correlationId;
    }
}
