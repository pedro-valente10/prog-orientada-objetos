package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import model.Jogo;

/** Cartas oficiais exibidas com Java2D; a escolha secreta é feita por clique na imagem. */
class PainelCartas extends JPanel {
    private static final long serialVersionUID = 1L;
    static final int LARGURA = 680;
    static final int ALTURA = 600;
    private final Jogo jogo;
    private final List<Alvo> alvos = new ArrayList<>();
    private final Map<String, BufferedImage> imagens = new HashMap<>();

    PainelCartas(Jogo jogo) {
        this.jogo = jogo;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
    }
    String acaoEm(int x, int y) { return Alvo.acaoEm(alvos, x, y); }

    private BufferedImage imagem(String nome) {
        if (imagens.containsKey(nome)) return imagens.get(nome);
        try {
            BufferedImage carregada = ImageIO.read(PainelCartas.class.getResource("img/cartas/" + nome));
            if (carregada == null) throw new IOException("Imagem inválida: " + nome);
            imagens.put(nome, carregada);
            return carregada;
        } catch (IOException e) {
            throw new IllegalStateException("Carta sem imagem: " + nome, e);
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage tela = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_ARGB);
        Graphics2D d = tela.createGraphics();
        d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        d.setColor(new Color(31, 37, 52)); d.fillRect(0, 0, LARGURA, ALTURA);
        alvos.clear();
        d.setColor(Color.WHITE); d.setFont(new Font("SansSerif", Font.BOLD, 14));
        d.drawString("Cartas de conselheiro", 14, 23);
        d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        d.drawString("Abertas: a primeira pessoa a cumprir recebe 1 voto.", 14, 42);
        List<String> abertas = jogo.getCartasAbertas();
        List<String> imagensAbertas = jogo.getImagensCartasAbertas();
        for (int i = 0; i < 4; i++) {
            int x = 22 + i * 165;
            if (i < abertas.size()) {
                d.drawImage(imagem(imagensAbertas.get(i)), x + 19, 52, 118, 184, null);
                descricao(d, abertas.get(i), x, 250, 154, 2);
            } else {
                d.setColor(new Color(79, 85, 98)); d.fillRoundRect(x, 52, 154, 184, 8, 8);
                d.setColor(Color.WHITE); d.drawString("Carta cumprida", x + 20, 146);
            }
        }
        String cor = jogo.getJogadorDaVez();
        d.setColor(Color.WHITE); d.setFont(new Font("SansSerif", Font.BOLD, 13));
        d.drawString("Carta secreta de " + cor + " — pontua no fim", 14, 298);
        String escolhida = jogo.getCartaSecreta(cor);
        if (escolhida != null) {
            d.drawImage(imagem(jogo.getImagemCartaSecreta(cor)), 267, 310, 146, 225, null);
            descricao(d, escolhida + " • " + jogo.getVotosSecretos(cor) + " voto(s) atuais",
                    190, 551, 300, 2);
        } else {
            List<String> opcoes = jogo.getOpcoesSecretas(cor);
            List<String> imagensOpcoes = jogo.getImagensOpcoesSecretas(cor);
            for (int i = 0; i < opcoes.size(); i++) {
                int x = 90 + i * 310;
                d.drawImage(imagem(imagensOpcoes.get(i)), x, 310, 146, 225, null);
                alvos.add(new Alvo("SECRETA:" + i, new Rectangle(x, 310, 146, 225)));
                descricao(d, (i + 1) + ") " + opcoes.get(i), x - 40, 550, 225, 2);
            }
            d.setColor(Color.WHITE); d.setFont(new Font("SansSerif", Font.PLAIN, 11));
            d.drawString("Clique numa carta secreta antes de ativar uma coluna.", 14, 587);
        }
        d.dispose(); ((Graphics2D) g).drawImage(tela, 0, 0, null);
    }

    private static void descricao(Graphics2D d, String texto, int x, int y, int largura, int maxLinhas) {
        d.setColor(Color.WHITE); d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fm = d.getFontMetrics();
        String linha = "";
        int n = 0;
        for (String palavra : texto.split(" ")) {
            String tentativa = linha.isEmpty() ? palavra : linha + " " + palavra;
            if (!linha.isEmpty() && fm.stringWidth(tentativa) > largura) {
                d.drawString(linha, x, y + n * 13);
                if (++n >= maxLinhas) return;
                linha = palavra;
            } else linha = tentativa;
        }
        if (!linha.isEmpty() && n < maxLinhas) d.drawString(linha, x, y + n * 13);
    }
}
