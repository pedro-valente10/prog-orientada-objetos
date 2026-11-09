package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/** Jogador: guarda recursos (tributos e tesouros), iurtas disponíveis e melhorias. */
class Jogador {

    static final int MAX_IURTAS = 12;
    static final int CUSTO_MELHORIA = 2; // moedas

    private final CorJogador cor;
    private final Herdeiro herdeiro;
    private final Map<TipoTributo, Integer> tributos = new HashMap<>();
    private final Map<TipoTesouro, Integer> tesouros = new HashMap<>();
    private final TabuleiroJogador tabuleiroIndividual;
    private int iurtasDisponiveis = MAX_IURTAS;
    private final List<String> posicoes = new ArrayList<>();
    private int peaoAtivo;
    private boolean peaoEscolhido;

    Jogador(CorJogador cor, String posicaoInicial) { this(cor, null, posicaoInicial); }
    Jogador(CorJogador cor, Herdeiro herdeiro, String posicaoInicial) {
        this.cor = cor;
        this.herdeiro = herdeiro;
        this.tabuleiroIndividual = new TabuleiroJogador(herdeiro);
        posicoes.add(posicaoInicial);
        if (herdeiro == Herdeiro.ALTANI) posicoes.add(posicaoInicial);
        peaoEscolhido = herdeiro != Herdeiro.ALTANI;
    }

    CorJogador getCor() { return cor; }
    Herdeiro getHerdeiro() { return herdeiro; }
    String getPosicao() { return posicoes.get(peaoAtivo); }
    List<String> getPosicoes() { return Collections.unmodifiableList(posicoes); }
    int getPeaoAtivo() { return peaoAtivo; }
    boolean isEscolhaPeaoPendente() { return !peaoEscolhido; }
    void escolherPeao(int indice) {
        if (herdeiro != Herdeiro.ALTANI || indice < 0 || indice >= posicoes.size() || peaoEscolhido)
            throw new RegraInvalidaException("Escolha inválida de peão.");
        peaoAtivo = indice;
        peaoEscolhido = true;
    }
    void reiniciarEscolhaPeao() { if (herdeiro == Herdeiro.ALTANI) peaoEscolhido = false; }
    void setPosicao(String posicao) { posicoes.set(peaoAtivo, posicao); }
    int getIurtasDisponiveis() { return iurtasDisponiveis; }
    TabuleiroJogador getTabuleiroIndividual() { return tabuleiroIndividual; }

    // ---- tributos ----
    int quantidadeTributo(TipoTributo tipo) {
        return tributos.getOrDefault(tipo, 0);
    }

    void adicionarTributo(TipoTributo tipo, int quantidade) {
        if (tipo == null || quantidade < 0) {
            throw new RegraInvalidaException("Tipo ou quantidade de tributo inválido.");
        }
        tributos.put(tipo, tributos.getOrDefault(tipo, 0) + quantidade);
    }

    void gastarTributo(TipoTributo tipo, int quantidade) {
        if (tipo == null || quantidade < 0) {
            throw new RegraInvalidaException("Tipo ou quantidade de tributo inválido.");
        }
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
        if (tipo == null || quantidade < 0) {
            throw new RegraInvalidaException("Tipo ou quantidade de tesouro inválido.");
        }
        tesouros.put(tipo, tesouros.getOrDefault(tipo, 0) + quantidade);
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
        if (pedido == null) {
            throw new RegraInvalidaException("A entrega de tesouros é obrigatória.");
        }
        for (Map.Entry<TipoTesouro, Integer> e : pedido.entrySet()) {
            if (e.getKey() == null || e.getValue() == null || e.getValue() < 0) {
                throw new RegraInvalidaException("A entrega contém um tesouro ou quantidade inválida.");
            }
        }
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
        return tabuleiroIndividual.quantidadeMelhorias(coluna);
    }

    /** Compatibilidade com os cenários de unidade da primeira iteração. */
    void comprarMelhoria(Regiao coluna) {
        Coluna destino = Coluna.valueOf(coluna.name());
        if (!tabuleiroIndividual.temEspaco(destino))
            throw new RegraInvalidaException("Coluna de melhorias cheia.");
        gastarTributo(TipoTributo.MOEDA, CUSTO_MELHORIA);
        tabuleiroIndividual.colocar(destino,
                new Melhoria(-1 - tabuleiroIndividual.quantidadeMelhorias(destino),
                        coluna, Melhoria.Tipo.CAVALOS, null));
    }
}
