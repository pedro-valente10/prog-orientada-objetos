package model;

/**
 * Peão do Khan. Começa fora do tabuleiro; a cada uso precisa ir para um local
 * diferente do atual (uma das 4 províncias Khan ou a área de melhorias).
 */
class Khan {

    static final String AREA_MELHORIAS = "MELHORIAS";

    private String local; // null = fora do tabuleiro

    String getLocal() { return local; }

    void mover(String destino) {
        if (destino.equals(local)) {
            throw new RegraInvalidaException("O Khan não pode permanecer onde está.");
        }
        local = destino;
    }
}
