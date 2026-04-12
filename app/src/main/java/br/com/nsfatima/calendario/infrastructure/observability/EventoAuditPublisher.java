package br.com.nsfatima.calendario.infrastructure.observability;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EventoAuditPublisher {

    private final AuditLogService auditLogService;

    public EventoAuditPublisher(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public void publish(String actor, String action, String target, String result) {
        auditLogService.log(actor, action, target, result, Map.of());
    }

    public void publish(String actor, String action, String target, String result, Map<String, Object> metadata) {
        auditLogService.log(actor, action, target, result, metadata);
    }

    public void publishCreateSuccess(String actor, String target, boolean replay, String conflictState) {
        auditLogService.log(
                actor,
                "create",
                target,
                "success",
                Map.of(
                        "replay", replay,
                        "conflictState", conflictState == null ? "NONE" : conflictState));
    }

    public void publishCreateFailure(String actor, String errorCategory, String message) {
        auditLogService.log(
                actor,
                "create",
                "evento",
                "failure",
                Map.of(
                        "errorCategory", errorCategory,
                        "message", message));
    }

    public void publishListSuccess(String actor, int totalItems) {
        auditLogService.log(actor, "list", "eventos", "success", Map.of("totalItems", totalItems));
    }

    public void publishDeniedWrite(String actor, String target) {
        auditLogService.log(actor, "write-denied", target, "ACCESS_DENIED", Map.of("errorCategory", "AUTHZ"));
    }

    public void publishCancellationPending(String actor, String eventoId, String solicitacaoAprovacaoId,
            String motivo) {
        auditLogService.log(
                actor,
                "cancel",
                eventoId,
                "pending",
                Map.of(
                        "solicitacaoAprovacaoId", solicitacaoAprovacaoId,
                        "canceladoMotivo", motivo));
    }

    public void publishCancellationRejected(String actor, String aprovacaoId, String eventoId) {
        auditLogService.log(
                actor,
                "approval-decision",
                aprovacaoId,
                "rejected",
                Map.of("eventoId", eventoId));
    }

    public void publishCancellationExecuted(String actor, String aprovacaoId, String eventoId) {
        auditLogService.log(
                actor,
                "approval-decision",
                aprovacaoId,
                "executed",
                Map.of("eventoId", eventoId));
    }

    public void publishCancellationExecutionFailed(String actor, String aprovacaoId, String eventoId, String error) {
        auditLogService.log(
                actor,
                "approval-decision",
                aprovacaoId,
                "failed",
                Map.of(
                        "eventoId", eventoId,
                        "error", error));
    }
}
