package model;

import java.util.HashMap;
import java.util.Map;

/** Jogador: guarda recursos (tributos e tesouros), iurtas disponíveis e melhorias. */
class Jogador {

    static final int MAX_IURTAS = 12;
    static final int CUSTO_MELHORIA = 2; // moedas

    private final CorJogador cor;
    private final Map<TipoTributo, Integer> tributos = new HashMap<>();
    private final Map<TipoTesouro, Integer> tesouros = new HashMap<>();
    private final Map<Regiao, Integer> melhorias = new HashMap<>();
    private int iurtasDisponiveis = MAX_IURTAS;
    private String posicao;

    Jogador(CorJogador cor, String posicaoInicial) {
        this.cor = cor;
        this.posicao = posicaoInicial;
    }

    CorJogador getCor() { return cor; }
    String getPosicao() { return posicao; }
    void setPosicao(String posicao) { this.posicao = posicao; }
    int getIurtasDisponiveis() { return iurtasDisponiveis; }

    // ---- tributos ----
    int quantidadeTributo(TipoTributo tipo) {
        return tributos.getOrDefault(tipo, 0);
    }

    void adicionarTributo(TipoTributo tipo, int quantidade) {
        tributos.merge(tipo, quantidade, Integer::sum);
    }

    void gastarTributo(TipoTributo tipo, int quantidade) {
        if (quantidadeTributo(tipo) < quantidade) {
            throw new RegraInvalidaException("Faltam peças de tributo: " + tipo + ".");
        }
        tributos.put(tipo, quantidadeTributo(tipo) - quantidade);
    }

    // ---- tesouros ----
    int quantidadeTesouro(TipoTesouro tipo) {
        return tesouros.getOrDefault(tipo, 0);
    }

    int totalTesouros() {
        int total = 0;
        for (int q : tesouros.values()) {
            total += q;
        }
        return total;
    }

    void adicionarTesouro(TipoTesouro tipo, int quantidade) {
        tesouros.merge(tipo, quantidade, Integer::sum);
    }

    boolean possuiTesouros(Map<TipoTesouro, Integer> pedido) {
        for (Map.Entry<TipoTesouro, Integer> e : pedido.entrySet()) {
            if (quantidadeTesouro(e.getKey()) < e.getValue()) {
                return false;
            }
        }
        return true;
    }

    void removerTesouros(Map<TipoTesouro, Integer> pedido) {
        if (!possuiTesouros(pedido)) {
            throw new RegraInvalidaException("Tesouros insuficientes.");
        }
        for (Map.Entry<TipoTesouro, Integer> e : pedido.entrySet()) {
            tesouros.put(e.getKey(), quantidadeTesouro(e.getKey()) - e.getValue());
        }
    }

    // ---- iurtas ----
    void usarIurta() {
        if (iurtasDisponiveis == 0) {
            throw new RegraInvalidaException("O jogador não tem mais iurtas.");
        }
        iurtasDisponiveis--;
    }

    // ---- melhorias ----
    int quantidadeMelhorias(Regiao coluna) {
        return melhorias.getOrDefault(coluna, 0);
    }

    /** Paga 2 moedas e adiciona uma melhoria na coluna da região. */
    void comprarMelhoria(Regiao coluna) {
        gastarTributo(TipoTributo.MOEDA, CUSTO_MELHORIA);
        melhorias.merge(coluna, 1, Integer::sum);
    }
}
