package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parada: ponto da rota onde os peões se movem. Pode ser simples (1 peão, 1 iurta),
 * dupla (2 peões, 2 iurtas, tem conselheiro) ou Karakorum (todos os peões, sem iurtas).
 */
class Parada {

    private final String id;
    private final Regiao regiao;      // null em Karakorum
    private final boolean dupla;
    private final boolean karakorum;
    private final double x;
    private final double y;
    private final Set<String> vizinhas = new HashSet<>();
    private final Set<String> provinciasAdjacentes = new HashSet<>();
    private final List<CorJogador> iurtas = new ArrayList<>();
    private final List<CorJogador> peoes = new ArrayList<>();
    private Conselheiro conselheiro;

    Parada(String id, Regiao regiao, boolean dupla, boolean karakorum) {
        this(id, regiao, dupla, karakorum, Double.NaN, Double.NaN);
    }

    Parada(String id, Regiao regiao, boolean dupla, boolean karakorum, double x, double y) {
        this.id = id;
        this.regiao = regiao;
        this.dupla = dupla;
        this.karakorum = karakorum;
        this.x = x;
        this.y = y;
    }

    String getId() { return id; }
    Regiao getRegiao() { return regiao; }
    boolean isDupla() { return dupla; }
    boolean isKarakorum() { return karakorum; }
    double getX() { return x; }
    double getY() { return y; }
    Conselheiro getConselheiro() { return conselheiro; }
    void setConselheiro(Conselheiro conselheiro) { this.conselheiro = conselheiro; }

    Set<String> getVizinhas() { return Collections.unmodifiableSet(vizinhas); }
    void adicionarVizinha(String idOutra) { vizinhas.add(idOutra); }

    Set<String> getProvinciasAdjacentes() { return Collections.unmodifiableSet(provinciasAdjacentes); }
    void adicionarProvinciaAdjacente(String idProvincia) { provinciasAdjacentes.add(idProvincia); }

    // ---- peões ----
    private int capacidadePeoes() {
        if (karakorum) {
            return Integer.MAX_VALUE;
        }
        return dupla ? 2 : 1;
    }

    boolean podeReceberPeao() { return peoes.size() < capacidadePeoes(); }
    void adicionarPeao(CorJogador cor) { peoes.add(cor); }
    void removerPeao(CorJogador cor) { peoes.remove(cor); }
    int quantidadePeoes() { return peoes.size(); }
    List<CorJogador> getPeoes() { return Collections.unmodifiableList(peoes); }

    // ---- iurtas ----
    private int capacidadeIurtas() {
        if (karakorum) {
            return 0;
        }
        return dupla ? 2 : 1;
    }

    boolean podeReceberIurta() { return iurtas.size() < capacidadeIurtas(); }
    void adicionarIurta(CorJogador cor) { iurtas.add(cor); }
    boolean temIurta(CorJogador cor) { return iurtas.contains(cor); }
    List<CorJogador> getIurtas() { return Collections.unmodifiableList(iurtas); }

    int contarIurtas(CorJogador cor) {
        int n = 0;
        for (CorJogador c : iurtas) {
            if (c == cor) {
                n++;
            }
        }
        return n;
    }
}
