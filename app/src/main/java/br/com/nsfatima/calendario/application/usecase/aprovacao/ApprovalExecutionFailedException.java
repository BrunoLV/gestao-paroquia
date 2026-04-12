package br.com.nsfatima.calendario.application.usecase.aprovacao;

public class ApprovalExecutionFailedException extends RuntimeException {

    public ApprovalExecutionFailedException(String message) {
        super(message);
    }
}
