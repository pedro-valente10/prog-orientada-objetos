package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Conselho Kurultai: trilha de votos dos jogadores + ficha neutra que mede a ocupação.
 * Quando a ocupação atinge o limite (10/14/16/17 votos para 2/3/4/5 jogadores)
 * o fim do jogo é acionado.
 */
class Kurultai {

    private static final Map<Integer, Integer> LIMITES = new HashMap<>();
    static {
        LIMITES.put(2, 10);
        LIMITES.put(3, 14);
        LIMITES.put(4, 16);
        LIMITES.put(5, 17);
    }

    private final Map<CorJogador, Integer> votos = new HashMap<>();
    private final int limite;
    private int ocupacao;

    Kurultai(List<CorJogador> jogadores) {
        Integer l = LIMITES.get(jogadores.size());
        if (l == null) {
            throw new IllegalArgumentException("O jogo é para 2 a 5 jogadores.");
        }
        this.limite = l;
        for (CorJogador c : jogadores) {
            votos.put(c, 0);
        }
    }

    int getLimite() { return limite; }
    int getOcupacao() { return ocupacao; }
    int getVotos(CorJogador cor) { return votos.get(cor); }
    boolean estaCheio() { return ocupacao >= limite; }

    void adicionarVotos(CorJogador cor, int quantidade) {
        votos.put(cor, votos.get(cor) + quantidade);
        ocupacao += quantidade;
    }
}
