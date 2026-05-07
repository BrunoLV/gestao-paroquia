package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

public class ApprovalNotFoundException extends RuntimeException {

    public ApprovalNotFoundException(String message) {
        super(message);
    }
}
