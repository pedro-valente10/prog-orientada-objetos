package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Gera, em memória, as imagens dos piões e das faces dos dados (não há arquivos de imagem
 * dessas peças). Elas são exibidas depois com Graphics2D.drawImage().
 */
class Figuras {

    static final int LARGURA_PIAO = 30;
    static final int ALTURA_PIAO = 42;
    static final int LADO_DADO = 100;

    private Figuras() { }

    static Color corDoJogador(String nome) {
        switch (nome) {
            case "VERMELHO": return new Color(210, 30, 30);
            case "AZUL":     return new Color(30, 80, 210);
            case "VERDE":    return new Color(30, 150, 50);
            case "AMARELO":  return new Color(245, 205, 20);
            case "ROXO":     return new Color(140, 40, 170);
            default:         return Color.GRAY;
        }
    }

    static BufferedImage criarPiao(Color cor) {
        BufferedImage img = new BufferedImage(LARGURA_PIAO, ALTURA_PIAO, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2f));
        // corpo
        java.awt.geom.Path2D corpo = new java.awt.geom.Path2D.Double();
        corpo.moveTo(15, 14);
        corpo.curveTo(24, 22, 27, 32, 28, 40);
        corpo.lineTo(2, 40);
        corpo.curveTo(3, 32, 6, 22, 15, 14);
        corpo.closePath();
        g.setColor(cor);
        g.fill(corpo);
        g.setColor(Color.BLACK);
        g.draw(corpo);
        // cabeça
        Ellipse2D cabeca = new Ellipse2D.Double(7, 2, 16, 16);
        g.setColor(cor);
        g.fill(cabeca);
        g.setColor(Color.BLACK);
        g.draw(cabeca);
        g.dispose();
        return img;
    }

    static BufferedImage criarYurt() {
        BufferedImage img = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.Polygon teto = new java.awt.Polygon(
                new int[] { 2, 12, 22 }, new int[] { 11, 2, 11 }, 3);
        g.setColor(new Color(245, 226, 175));
        g.fill(teto);
        g.fillRoundRect(3, 10, 18, 12, 3, 3);
        g.setColor(new Color(87, 54, 39));
        g.setStroke(new BasicStroke(2f));
        g.draw(teto);
        g.drawRoundRect(3, 10, 18, 12, 3, 3);
        g.dispose();
        return img;
    }

    static BufferedImage[] criarFacesDosDados() {
        BufferedImage[] faces = new BufferedImage[6];
        for (int valor = 1; valor <= 6; valor++) {
            faces[valor - 1] = criarFace(valor);
        }
        return faces;
    }

    private static BufferedImage criarFace(int valor) {
        BufferedImage img = new BufferedImage(LADO_DADO, LADO_DADO, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RoundRectangle2D forma = new RoundRectangle2D.Double(3, 3, LADO_DADO - 7, LADO_DADO - 7, 22, 22);
        g.setColor(Color.WHITE);
        g.fill(forma);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(3f));
        g.draw(forma);

        int[] c = { 27, 50, 73 };
        boolean[][] tabela = {
            { false, false, false, false, true, false, false, false, false },
            { true, false, false, false, false, false, false, false, true },
            { true, false, false, false, true, false, false, false, true },
            { true, false, true, false, false, false, true, false, true },
            { true, false, true, false, true, false, true, false, true },
            { true, false, true, true, false, true, true, false, true }
        };
        boolean[] pontos = tabela[valor - 1];
        g.setColor(Color.BLACK);
        for (int i = 0; i < 9; i++) {
            if (pontos[i]) {
                int col = i % 3;
                int lin = i / 3;
                g.fill(new Ellipse2D.Double(c[col] - 8, c[lin] - 8, 16, 16));
            }
        }
        g.dispose();
        return img;
    }
}
