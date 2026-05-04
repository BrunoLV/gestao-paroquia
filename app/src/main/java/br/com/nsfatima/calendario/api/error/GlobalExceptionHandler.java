package br.com.nsfatima.calendario.api.error;

import java.util.List;
import br.com.nsfatima.calendario.infrastructure.observability.AuditLogService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Fallback exception handler for unexpected system errors.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler extends BaseExceptionHandler {

    public GlobalExceptionHandler(AuditLogService auditLogService) {
        super(auditLogService);
    }

    /**
     * Handles any unexpected system exceptions that were not caught by specific handlers.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationErrorResponse> handleUnexpected(Exception ex) {
        return buildValidation(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                "An unexpected error occurred",
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(),
                        "system",
                        ex.getMessage() != null ? ex.getMessage() : "Unexpected error",
                        null)));
    }
}
