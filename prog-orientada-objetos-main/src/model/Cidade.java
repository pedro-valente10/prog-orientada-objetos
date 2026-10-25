package model;

import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/** Cidade: fonte de tesouros que podem ser saqueados com espadas. */
class Cidade {

    private final String id;
    private final Regiao regiao;
    private final Set<String> paradasAdjacentes;
    private final Deque<TipoTesouro> tesouros = new LinkedList<>();
    private boolean conquistada;
    private CorJogador dono; // pode ficar null se o conquistador não tinha iurtas sobrando

    Cidade(String id, Regiao regiao, Set<String> paradasAdjacentes) {
        this.id = id;
        this.regiao = regiao;
        this.paradasAdjacentes = new HashSet<>(paradasAdjacentes);
    }

    String getId() { return id; }
    Regiao getRegiao() { return regiao; }
    Set<String> getParadasAdjacentes() { return Collections.unmodifiableSet(paradasAdjacentes); }
    int quantidadeTesouros() { return tesouros.size(); }
    boolean estaVazia() { return tesouros.isEmpty(); }
    boolean isConquistada() { return conquistada; }
    CorJogador getDono() { return dono; }

    void abastecer(List<TipoTesouro> novos) {
        tesouros.addAll(novos);
    }

    TipoTesouro retirarTesouro() {
        if (tesouros.isEmpty()) {
            throw new RegraInvalidaException("A cidade " + id + " não tem tesouros.");
        }
        return tesouros.pollFirst();
    }

    void marcarConquistada(CorJogador dono) {
        this.conquistada = true;
        this.dono = dono;
    }
}
