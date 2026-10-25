package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Área onde os dados são exibidos. Seu fundo é pintado com a cor do jogador da vez
 * e as faces dos dados são desenhadas com Graphics2D.drawImage().
 */
class PainelDados extends JPanel {

    private static final long serialVersionUID = 1L;

    private final BufferedImage[] faces = Figuras.criarFacesDosDados();
    private Color corDaVez = Color.LIGHT_GRAY;
    private int dado1;
    private int dado2;

    PainelDados() {
        setPreferredSize(new java.awt.Dimension(260, 150));
    }

    void setJogadorDaVez(String nomeCor) {
        corDaVez = Figuras.corDoJogador(nomeCor);
        repaint();
    }

    /** Valores 1..6; 0 = nenhum dado exibido. */
    void setDados(int d1, int d2) {
        dado1 = d1;
        dado2 = d2;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(corDaVez);
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (dado1 == 0) {
            return;
        }
        int y = (getHeight() - Figuras.LADO_DADO) / 2;
        int x1 = (getWidth() - 2 * Figuras.LADO_DADO) / 3;
        int x2 = 2 * x1 + Figuras.LADO_DADO;
        g2.drawImage(faces[dado1 - 1], x1, y, null);
        g2.drawImage(faces[dado2 - 1], x2, y, null);
    }
}
