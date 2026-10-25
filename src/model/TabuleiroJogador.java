package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Quatro fichas de ativação e quatro espaços de melhoria por coluna. */
class TabuleiroJogador {
    static final int LINHAS = 4;
    private static final int[] CAVALOS_INICIAIS = { 2, 2, 2, 2 };
    private static final int[] PUNHOS_INICIAIS = { 0, 1, 0, 0 };
    private static final boolean[] KHAN_INICIAL = { true, false, true, false };

    private final Melhoria[][] pecas = new Melhoria[Coluna.values().length][LINHAS];
    private final boolean[] fichasUsadas = new boolean[Coluna.values().length];
    private final boolean[] linhasPremiadas = new boolean[LINHAS];
    private int bonusBrancosPendentes;

    boolean disponivel(Coluna coluna) { return !fichasUsadas[coluna.ordinal()]; }

    int quantidadeMelhorias(Coluna coluna) {
        int n = 0;
        for (Melhoria peca : pecas[coluna.ordinal()]) if (peca != null) n++;
        return n;
    }

    int quantidadeMelhorias(Regiao regiao) {
        return quantidadeMelhorias(Coluna.valueOf(regiao.name()));
    }

    boolean temEspaco(Coluna coluna) { return quantidadeMelhorias(coluna) < LINHAS; }
    int bonusBrancosPendentes() { return bonusBrancosPendentes; }

    List<Melhoria> melhorias(Coluna coluna) {
        List<Melhoria> copia = new ArrayList<>();
        for (Melhoria peca : pecas[coluna.ordinal()]) copia.add(peca);
        return Collections.unmodifiableList(copia);
    }

    void colocar(Coluna coluna, Melhoria peca) {
        if (peca == null || !temEspaco(coluna))
            throw new RegraInvalidaException("Não há espaço para a melhoria.");
        if (coluna != Coluna.BRANCA && peca.cor() != coluna.regiao())
            throw new RegraInvalidaException("A cor da melhoria não corresponde à coluna.");
        int linha = quantidadeMelhorias(coluna);
        pecas[coluna.ordinal()][linha] = peca;
        if (coluna != Coluna.BRANCA && !linhasPremiadas[linha]
                && pecas[0][linha] != null && pecas[1][linha] != null && pecas[2][linha] != null) {
            linhasPremiadas[linha] = true;
            bonusBrancosPendentes++;
        }
    }

    void colocarBonusBranco(Melhoria peca) {
        if (bonusBrancosPendentes == 0)
            throw new RegraInvalidaException("Não há melhoria branca pendente.");
        colocar(Coluna.BRANCA, peca);
        bonusBrancosPendentes--;
    }

    Acoes ativar(Coluna coluna) {
        if (!disponivel(coluna))
            throw new RegraInvalidaException("A ficha dessa coluna já foi usada.");
        int indice = coluna.ordinal();
        int cavalos = CAVALOS_INICIAIS[indice];
        int punhos = PUNHOS_INICIAIS[indice];
        int moedas = 0, espadas = 0, yurts = 0;
        int[] tesouros = new int[TipoTesouro.values().length];
        for (Melhoria peca : pecas[indice]) {
            if (peca == null) continue;
            switch (peca.getTipo()) {
                case CAVALOS: cavalos += 2; break;
                case PUNHO: punhos++; break;
                case MOEDA: moedas++; break;
                case ESPADA: espadas++; break;
                case YURT: yurts++; break;
                case TESOURO: tesouros[peca.tesouro().ordinal()]++; break;
                default: throw new IllegalStateException("Melhoria desconhecida.");
            }
        }
        if (coluna == Coluna.BRANCA) Arrays.fill(fichasUsadas, false);
        else fichasUsadas[indice] = true;
        return new Acoes(cavalos, punhos, KHAN_INICIAL[indice], moedas, espadas, yurts, tesouros);
    }

    static int cavalosIniciais(Coluna coluna) { return CAVALOS_INICIAIS[coluna.ordinal()]; }
    static int punhosIniciais(Coluna coluna) { return PUNHOS_INICIAIS[coluna.ordinal()]; }
    static boolean khanInicial(Coluna coluna) { return KHAN_INICIAL[coluna.ordinal()]; }

    static final class Acoes {
        final int cavalos, punhos, moedas, espadas, yurts;
        final boolean khan;
        final int[] tesouros;

        Acoes(int cavalos, int punhos, boolean khan, int moedas, int espadas,
              int yurts, int[] tesouros) {
            this.cavalos = cavalos;
            this.punhos = punhos;
            this.khan = khan;
            this.moedas = moedas;
            this.espadas = espadas;
            this.yurts = yurts;
            this.tesouros = tesouros;
        }
    }
}
