package model;

/**
 * Lançada quando uma ação viola as regras do jogo.
 * É pública porque o Controller precisa capturá-la; não é um "elemento básico do jogo".
 */
public class RegraInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraInvalidaException(String mensagem) {
        super(mensagem);
    }
}
