package view;

import static org.junit.Assert.*;
import java.awt.event.MouseEvent;
import model.Jogo;
import org.junit.Test;

/** A dica do mapa deve identificar a parada, a coleta e o conselheiro. */
public class PainelTabuleiroTest {
    @Test public void dicaDaParadaMostraAreaEConselheiro() {
        Jogo jogo = Jogo.getInstance();
        jogo.novaPartida(2, 1L);
        PainelTabuleiro mapa = new PainelTabuleiro(jogo);
        mapa.setSize(850, 854);
        String p5 = dica(mapa, 375, 520);
        assertTrue(p5.contains("Parada P5 (Pérsia)"));
        assertTrue(p5.contains("K3, P3"));
        String r4 = dica(mapa, 255, 60);
        assertTrue(r4.contains("Parada R4 (Rússia)"));
        assertTrue(r4.contains("Conselheiro: 2 PELES"));
    }

    private static String dica(PainelTabuleiro mapa, int x, int y) {
        MouseEvent evento = new MouseEvent(mapa, MouseEvent.MOUSE_MOVED,
                System.currentTimeMillis(), 0, x, y, 0, false);
        return mapa.getToolTipText(evento);
    }
}
