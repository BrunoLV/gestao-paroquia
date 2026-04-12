package br.com.nsfatima.calendario.application.usecase.aprovacao;

public class ApprovalAlreadyDecidedException extends RuntimeException {

    public ApprovalAlreadyDecidedException(String message) {
        super(message);
    }
}
