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
import model.Jogo;

/** As cartas são pintadas em Java2D; apenas as escolhas secretas recebem cliques. */
class PainelCartas extends JPanel {
    private static final long serialVersionUID = 1L;
    static final int LARGURA = 480;
    static final int ALTURA = 402;
    private final Jogo jogo;
    private final List<Alvo> alvos = new ArrayList<>();

    PainelCartas(Jogo jogo) {
        this.jogo = jogo;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
    }
    String acaoEm(int x, int y) { return Alvo.acaoEm(alvos, x, y); }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage imagem = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_ARGB);
        Graphics2D d = imagem.createGraphics();
        d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        d.setColor(new Color(31, 37, 52)); d.fillRect(0, 0, LARGURA, ALTURA);
        alvos.clear();
        d.setColor(Color.WHITE); d.setFont(new Font("SansSerif", Font.BOLD, 14));
        d.drawString("Cartas de conselheiro", 14, 23);
        d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        d.drawString("Abertas: a primeira pessoa a cumprir recebe 1 voto.", 14, 42);
        List<String> abertas = jogo.getCartasAbertas();
        for (int i = 0; i < 4; i++) {
            int y = 52 + i * 49;
            d.setColor(new Color(220, 227, 235)); d.fillRoundRect(14, y, 452, 44, 7, 7);
            d.setColor(new Color(31, 37, 52)); d.setFont(new Font("SansSerif", Font.BOLD, 12));
            d.drawString(i < abertas.size() ? abertas.get(i) : "Carta já cumprida", 24, y + 27);
        }
        String cor = jogo.getJogadorDaVez();
        d.setColor(Color.WHITE); d.setFont(new Font("SansSerif", Font.BOLD, 12));
        d.drawString("Carta secreta de " + cor + " — pontua no fim", 14, 267);
        String escolhida = jogo.getCartaSecreta(cor);
        if (escolhida != null) {
            d.setColor(new Color(220, 227, 235)); d.fillRoundRect(14, 280, 452, 80, 7, 7);
            d.setColor(new Color(31, 37, 52)); d.setFont(new Font("SansSerif", Font.BOLD, 12));
            d.drawString(escolhida, 24, 307);
            d.drawString("Votos atuais desta carta: " + jogo.getVotosSecretos(cor), 24, 334);
        } else {
            List<String> opcoes = jogo.getOpcoesSecretas(cor);
            for (int i = 0; i < opcoes.size(); i++) {
                int y = 280 + i * 49;
                d.setColor(new Color(83, 115, 151)); d.fillRoundRect(14, y, 452, 44, 7, 7);
                d.setColor(Color.WHITE); d.setFont(new Font("SansSerif", Font.BOLD, 11));
                d.drawString((i + 1) + ") " + opcoes.get(i), 24, y + 27);
                alvos.add(new Alvo("SECRETA:" + i, new Rectangle(14, y, 452, 44)));
            }
            d.setColor(Color.WHITE); d.setFont(new Font("SansSerif", Font.PLAIN, 11));
            d.drawString("Escolha uma das duas antes de ativar uma coluna.", 14, 390);
        }
        d.dispose(); ((Graphics2D) g).drawImage(imagem, 0, 0, null);
    }
}
