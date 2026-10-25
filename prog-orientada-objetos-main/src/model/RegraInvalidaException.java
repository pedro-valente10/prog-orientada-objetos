package model;

/**
 * Lançada quando uma ação viola as regras do jogo.
 */
public class RegraInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraInvalidaException(String mensagem) {
        super(mensagem);
    }
}
