package view;

import static org.junit.Assert.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import model.Coluna;
import model.Jogo;
import org.junit.Test;

/** A oferta deve aceitar clique apenas quando a compra e permitida. */
public class PainelMelhoriasTest {
    @Test public void compraSoRecebeCliqueComTurnoERecursos() {
        Jogo jogo = Jogo.getInstance();
        jogo.novaPartida(2, 1L);
        PainelMelhorias painel = new PainelMelhorias(jogo);
        painel.setSize(480, 176);
        redesenhar(painel);
        assertNull(painel.acaoEm(57, 60));
        assertNull(painel.acaoEm(120, 125));
        jogo.ativarColuna(Coluna.CHINA);
        redesenhar(painel);
        assertEquals("MELHORIA:0", painel.acaoEm(57, 60));
        assertEquals("MELHORIA:-1", painel.acaoEm(120, 125));
    }

    private static void redesenhar(PainelMelhorias painel) {
        BufferedImage imagem = new BufferedImage(480, 176, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics();
        painel.paint(g);
        g.dispose();
    }
}
