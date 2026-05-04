package br.com.nsfatima.calendario.api.error;

import java.util.List;
import br.com.nsfatima.calendario.application.usecase.aprovacao.ApprovalAlreadyDecidedException;
import br.com.nsfatima.calendario.application.usecase.aprovacao.ApprovalExecutionFailedException;
import br.com.nsfatima.calendario.application.usecase.aprovacao.ApprovalNotFoundException;
import br.com.nsfatima.calendario.domain.exception.ApprovalRequiredException;
import br.com.nsfatima.calendario.infrastructure.observability.AuditLogService;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions related to the event approval workflow.
 */
@RestControllerAdvice
@Order(2)
public class ApprovalExceptionHandler extends BaseExceptionHandler {

    public ApprovalExceptionHandler(AuditLogService auditLogService) {
        super(auditLogService);
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

    @ExceptionHandler(ApprovalNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handleApprovalNotFound(ApprovalNotFoundException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.APPROVAL_NOT_FOUND,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.APPROVAL_NOT_FOUND.name(),
                        "aprovacaoId",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ApprovalAlreadyDecidedException.class)
    public ResponseEntity<ValidationErrorResponse> handleApprovalAlreadyDecided(ApprovalAlreadyDecidedException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.APPROVAL_ALREADY_DECIDED,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.APPROVAL_ALREADY_DECIDED.name(),
                        "aprovacaoId",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ApprovalExecutionFailedException.class)
    public ResponseEntity<ValidationErrorResponse> handleApprovalExecutionFailed(ApprovalExecutionFailedException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.APPROVAL_EXECUTION_FAILED,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.APPROVAL_EXECUTION_FAILED.name(),
                        "approvalExecution",
                        ex.getMessage(),
                        null)));
    }
}
