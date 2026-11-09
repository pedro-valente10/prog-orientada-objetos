package view;

import static org.junit.Assert.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import model.Jogo;
import org.junit.Test;

/** Regressão para botões de ataque no painel de 480 pixels. */
public class PainelControleTest {
    @Test public void todasAsCidadesReveladasTemAlvoVisivelEClicavel() {
        Jogo jogo = Jogo.getInstance();
        Set<String> vistas = new HashSet<>();
        for (long semente=0; semente<80; semente++) {
            jogo.novaPartida(3, semente);
            PainelControle painel = new PainelControle(jogo);
            painel.setSize(480, 854);
            BufferedImage imagem = new BufferedImage(480, 854, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = imagem.createGraphics(); painel.paint(g); g.dispose();
            int i=0;
            for (String cidade : jogo.getCidadesReveladas()) {
                int x=14+i*115, y=262+4*49;
                assertTrue("botão excede a largura", x+107 <= 480);
                assertEquals("ATAQUE:"+cidade, painel.acaoEm(x+53,y+21));
                vistas.add(cidade); i++;
            }
            assertTrue("há mais cidades que botões", i<=4);
        }
        assertEquals("todas as nove cidades apareceram", 9, vistas.size());
    }
}
