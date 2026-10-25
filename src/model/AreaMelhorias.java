package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/** Oferta de cinco peças identificáveis e pilha finita de 44 melhorias. */
class AreaMelhorias {
    static final int VAGAS = 5;
    static final int VAGA_ICONE_MOEDA = 0;
    static final int VAGA_ICONE_ESPADA = 1;

    private final Melhoria[] oferta = new Melhoria[VAGAS];
    private final Deque<Melhoria> pilha = new ArrayDeque<>();

    AreaMelhorias(long semente) {
        List<Melhoria> pecas = new ArrayList<>();
        int id = 1;
        id = adicionar(pecas, id, Melhoria.Tipo.MOEDA, 8, null);
        id = adicionar(pecas, id, Melhoria.Tipo.ESPADA, 8, null);
        id = adicionar(pecas, id, Melhoria.Tipo.YURT, 8, null);
        id = adicionar(pecas, id, Melhoria.Tipo.PUNHO, 6, null);
        id = adicionar(pecas, id, Melhoria.Tipo.CAVALOS, 9, null);
        for (TipoTesouro tipo : TipoTesouro.values())
            id = adicionar(pecas, id, Melhoria.Tipo.TESOURO, 1, tipo);
        Collections.shuffle(pecas, new Random(semente));
        pilha.addAll(pecas);
        reabastecer();
    }

    private static int adicionar(List<Melhoria> pecas, int id, Melhoria.Tipo tipo,
                                 int quantidade, TipoTesouro tesouro) {
        for (int i = 0; i < quantidade; i++) {
            // Três cores distribuídas pela reserva sem alterar a quantidade de cada efeito.
            Regiao cor = Regiao.values()[(id - 1) % Regiao.values().length];
            pecas.add(new Melhoria(id++, cor, tipo, tesouro));
        }
        return id;
    }

    boolean estaOcupada(int vaga) { return vaga >= 0 && vaga < VAGAS && oferta[vaga] != null; }
    boolean iconeMoedaVisivel() { return !estaOcupada(VAGA_ICONE_MOEDA); }
    boolean iconeEspadaVisivel() { return !estaOcupada(VAGA_ICONE_ESPADA); }
    int quantidadeNaPilha() { return pilha.size(); }

    Melhoria consultar(int vaga) {
        if (!estaOcupada(vaga)) throw new RegraInvalidaException("Vaga de melhoria vazia.");
        return oferta[vaga];
    }

    Melhoria consultarTopo() {
        if (pilha.isEmpty()) throw new RegraInvalidaException("A pilha de melhorias acabou.");
        return pilha.peekFirst();
    }

    List<Melhoria> oferta() {
        List<Melhoria> copia = new ArrayList<>();
        Collections.addAll(copia, oferta);
        return Collections.unmodifiableList(copia);
    }

    Melhoria retirar(int vaga) {
        Melhoria escolhida = consultar(vaga);
        for (int i = vaga; i > 0; i--) oferta[i] = oferta[i - 1];
        oferta[0] = null;
        return escolhida;
    }

    Melhoria retirarTopo() {
        Melhoria escolhida = consultarTopo();
        pilha.removeFirst();
        return escolhida;
    }

    void reabastecer() {
        for (int i = 0; i < VAGAS && !pilha.isEmpty(); i++)
            if (oferta[i] == null) oferta[i] = pilha.removeFirst();
    }
}
