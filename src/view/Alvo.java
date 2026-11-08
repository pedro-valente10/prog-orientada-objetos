package view;

import java.awt.Rectangle;

/** Região clicável de uma tela, associada ao comando textual que o Controller interpreta. */
final class Alvo {
    final String acao;
    final Rectangle area;

    Alvo(String acao, Rectangle area) {
        this.acao = acao;
        this.area = area;
    }

    /** Procura, entre os alvos, a ação que está sob o ponto (x, y); null se não houver. */
    static String acaoEm(java.util.List<Alvo> alvos, int x, int y) {
        for (Alvo alvo : alvos)
            if (alvo.area.contains(x, y)) return alvo.acao;
        return null;
    }
}
