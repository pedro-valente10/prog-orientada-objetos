package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import model.Coluna;
import model.Jogo;
import model.Melhoria;

/** Oferta de cinco peças de melhoria e pilha de reposição. */
class PainelMelhorias extends JPanel {
    private static final long serialVersionUID = 1L;
    static final int LARGURA = 480;
    static final int ALTURA = 176;

    private final Jogo jogo;
    private final List<Alvo> alvos = new ArrayList<>();

    PainelMelhorias(Jogo jogo) {
        this.jogo = jogo;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
    }

    String acaoEm(int x, int y) { return Alvo.acaoEm(alvos, x, y); }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage tela = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_ARGB);
        Graphics2D d = tela.createGraphics();
        d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        d.setColor(new Color(31, 37, 52));
        d.fillRect(0, 0, LARGURA, ALTURA);
        alvos.clear();

        d.setColor(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.BOLD, 13));
        d.drawString("Oferta de melhorias — clique para comprar (2 moedas)", 14, 20);
        List<Melhoria> oferta = jogo.getOfertaMelhorias();
        for (int i = 0; i < oferta.size(); i++) {
            int x = 14 + i * 92;
            Melhoria peca = oferta.get(i);
            d.setColor(peca == null ? new Color(78, 83, 94) : Figuras.corColuna(Coluna.valueOf(peca.getCor())));
            d.fillRoundRect(x, 30, 86, 71, 9, 9);
            d.setColor(new Color(24, 29, 40));
            d.setFont(new Font("SansSerif", Font.BOLD, 11));
            d.drawString(peca == null ? "Vazia" : Figuras.nomePeca(peca), x + 6, 57);
            if (peca != null) {
                d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                d.drawString(peca.getCor(), x + 6, 75);
                d.drawString("#" + peca.getId(), x + 6, 91);
                alvos.add(new Alvo("MELHORIA:" + i, new Rectangle(x, 30, 86, 71)));
            }
        }
        boolean haPilha = jogo.getMelhoriasNaPilha() > 0;
        d.setColor(haPilha ? new Color(83, 115, 151) : new Color(72, 76, 85));
        d.fillRoundRect(14, 110, 220, 30, 8, 8);
        d.setColor(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.BOLD, 11));
        d.drawString("Comprar o topo da pilha", 24, 129);
        if (haPilha) alvos.add(new Alvo("MELHORIA:-1", new Rectangle(14, 110, 220, 30)));
        d.setColor(new Color(180, 194, 217));
        d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        d.drawString("Pilha: " + jogo.getMelhoriasNaPilha() + " peças", 246, 129);
        d.drawString("A oferta só é reposta quando o Khan vai à área de melhorias.", 14, 158);
        d.dispose();
        ((Graphics2D) g).drawImage(tela, 0, 0, null);
    }
}
