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
    @Test public void todasAsCidadesReveladasTemEspacoEIniciamBloqueadas() {
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
                assertNull("ataque sem turno não deve aceitar clique", painel.acaoEm(x+53,y+21));
                vistas.add(cidade); i++;
            }
            assertTrue("há mais cidades que botões", i<=4);
        }
        assertEquals("todas as nove cidades apareceram", 9, vistas.size());
    }

    @Test public void coletaEEncerrarSoSaoClicaveisQuandoPermitidos() {
        Jogo jogo = Jogo.getInstance();
        jogo.novaPartida(2, 1L);
        PainelControle painel = new PainelControle(jogo);
        painel.setSize(480, 854);
        redesenhar(painel);
        assertNull(painel.acaoEm(67, 283)); // Encerrar antes da coluna
        assertNull(painel.acaoEm(297, 381)); // Coletar K3 antes do turno
        jogo.ativarColuna(model.Coluna.PERSIA);
        jogo.moverPara("P5");
        redesenhar(painel);
        assertEquals("FIM", painel.acaoEm(67, 283));
        assertEquals("COLETA:K3", painel.acaoEm(297, 381));
        assertNull(painel.acaoEm(67, 430)); // P1 nao e vizinha de P5
        jogo.coletarTributo("K3");
        redesenhar(painel);
        assertNull(painel.acaoEm(297, 381)); // acabou o unico punho
    }

    @Test public void identificaAreasDaParadaEConsultasPermanentes() {
        Jogo jogo = Jogo.getInstance();
        jogo.novaPartida(2, 1L);
        PainelControle painel = new PainelControle(jogo);
        painel.setSize(480, 854);
        redesenhar(painel);
        assertEquals("AREAS", painel.acaoEm(412, 577));
        assertEquals("CONSELHEIROS", painel.acaoEm(297, 577));
        model.ParadaInfo p5 = null;
        for (model.ParadaInfo parada : jogo.getParadas())
            if ("P5".equals(parada.getId())) p5 = parada;
        assertNotNull(p5);
        assertTrue(p5.getProvinciasAdjacentes().contains("K3"));
        assertTrue(p5.getProvinciasAdjacentes().contains("P3"));
    }

    private static void redesenhar(PainelControle painel) {
        BufferedImage imagem = new BufferedImage(480, 854, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics();
        painel.paint(g);
        g.dispose();
    }}
