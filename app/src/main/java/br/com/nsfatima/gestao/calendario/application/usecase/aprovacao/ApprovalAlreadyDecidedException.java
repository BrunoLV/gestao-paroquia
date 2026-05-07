package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

public class ApprovalAlreadyDecidedException extends RuntimeException {

    public ApprovalAlreadyDecidedException(String message) {
        super(message);
    }
}
