package model;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/** Tabuleiro: grafo de paradas + províncias + cidades. Não conhece jogadores nem turnos. */
class Tabuleiro {

    static final String KARAKORUM = "KARAKORUM";

    private final Map<String, Parada> paradas = new HashMap<>();
    private final Map<String, Provincia> provincias = new HashMap<>();
    private final Map<String, Cidade> cidadesReveladas = new HashMap<>();
    private final Deque<Cidade> pilhaCidades = new ArrayDeque<>();

    // ---- construção ----
    void adicionarParada(Parada p) { paradas.put(p.getId(), p); }
    void adicionarProvincia(Provincia p) { provincias.put(p.getId(), p); }
    void adicionarCidadeRevelada(Cidade c) { cidadesReveladas.put(c.getId(), c); }
    void adicionarCidadeNaPilha(Cidade c) { pilhaCidades.addLast(c); }

    /** Cria uma ligação nos dois sentidos entre duas paradas. */
    void ligar(String a, String b) {
        getParada(a).adicionarVizinha(b);
        getParada(b).adicionarVizinha(a);
    }

    // ---- consulta ----
    Parada getParada(String id) {
        Parada p = paradas.get(id);
        if (p == null) {
            throw new RegraInvalidaException("Parada inexistente: " + id);
        }
        return p;
    }

    Provincia getProvincia(String id) {
        Provincia p = provincias.get(id);
        if (p == null) {
            throw new RegraInvalidaException("Província inexistente: " + id);
        }
        return p;
    }

    Cidade getCidade(String id) {
        Cidade c = cidadesReveladas.get(id);
        if (c == null) {
            throw new RegraInvalidaException("Cidade não está disponível: " + id);
        }
        return c;
    }

    Collection<Cidade> getCidadesReveladas() {
        return Collections.unmodifiableCollection(cidadesReveladas.values());
    }

    /** Vira a próxima cidade da pilha; retorna null se não há mais cidades. */
    Cidade revelarProximaCidade() {
        Cidade c = pilhaCidades.pollFirst();
        if (c != null) {
            cidadesReveladas.put(c.getId(), c);
        }
        return c;
    }

    /** Fim de jogo alternativo: todas as cidades foram conquistadas. */
    boolean todasCidadesConquistadas() {
        if (!pilhaCidades.isEmpty()) {
            return false;
        }
        for (Cidade c : cidadesReveladas.values()) {
            if (!c.isConquistada()) {
                return false;
            }
        }
        return !cidadesReveladas.isEmpty();
    }

    /** Menor número de movimentos entre duas paradas (busca em largura). */
    int distancia(String origem, String destino) {
        if (origem.equals(destino)) {
            return 0;
        }
        Map<String, Integer> dist = new HashMap<>();
        Queue<String> fila = new LinkedList<>();
        Set<String> vistos = new HashSet<>();
        dist.put(origem, 0);
        vistos.add(origem);
        fila.add(origem);
        while (!fila.isEmpty()) {
            String atual = fila.poll();
            for (String viz : getParada(atual).getVizinhas()) {
                if (vistos.add(viz)) {
                    dist.put(viz, dist.get(atual) + 1);
                    if (viz.equals(destino)) {
                        return dist.get(viz);
                    }
                    fila.add(viz);
                }
            }
        }
        return Integer.MAX_VALUE; // inalcançável
    }

    /** Iurtas de um jogador numa região (paradas + cidades conquistadas). */
    int contarIurtas(CorJogador cor, Regiao regiao) {
        int total = 0;
        for (Parada p : paradas.values()) {
            if (p.getRegiao() == regiao) {
                total += p.contarIurtas(cor);
            }
        }
        for (Cidade c : cidadesReveladas.values()) {
            if (c.getRegiao() == regiao && c.getDono() == cor) {
                total++;
            }
        }
        return total;
    }
}
