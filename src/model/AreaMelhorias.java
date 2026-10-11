package model;

/**
 * Área de melhorias (5 vagas). Quando o Khan vai para cá, as vagas são repostas
 * e o jogador ganha uma moeda/espada se os ícones correspondentes estiverem visíveis
 * (vaga vazia). ATENÇÃO: as posições dos ícones abaixo são hipóteses; confira no tabuleiro.
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
