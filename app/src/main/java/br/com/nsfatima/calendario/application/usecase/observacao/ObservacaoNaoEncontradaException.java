package br.com.nsfatima.calendario.application.usecase.observacao;

public class ObservacaoNaoEncontradaException extends RuntimeException {

    public ObservacaoNaoEncontradaException(String message) {
        super(message);
    }
}
