package br.com.nsfatima.calendario.application.usecase.aprovacao;

public class ApprovalNotFoundException extends RuntimeException {

    public ApprovalNotFoundException(String message) {
        super(message);
    }
}
