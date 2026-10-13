package model;

/**
 * Área de melhorias com cinco vagas e ícones de moeda e espada nas posições definidas.
 * Ao visitar a área, as vagas são repostas e os ícones visíveis concedem os recursos correspondentes.
 */
class AreaMelhorias {

    static final int VAGAS = 5;
    static final int VAGA_ICONE_MOEDA = 0;
    static final int VAGA_ICONE_ESPADA = 1;

    private final boolean[] ocupada = new boolean[VAGAS];

    AreaMelhorias() {
        reabastecer();
    }

    boolean estaOcupada(int vaga) { return ocupada[vaga]; }
    boolean iconeMoedaVisivel() { return !ocupada[VAGA_ICONE_MOEDA]; }
    boolean iconeEspadaVisivel() { return !ocupada[VAGA_ICONE_ESPADA]; }

    void remover(int vaga) { ocupada[vaga] = false; }

    void reabastecer() {
        for (int i = 0; i < VAGAS; i++) {
            ocupada[i] = true;
        }
    }
}
