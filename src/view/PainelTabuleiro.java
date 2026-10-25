package view;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import model.ParadaInfo;

/** Fotografia do tabuleiro com o grafo jogável destacado e derivado do Model. */
class PainelTabuleiro extends JPanel {
    private static final long serialVersionUID = 1L;
    static final int LARGURA = 850;
    static final int ALTURA = 854;

    private final BufferedImage imagemTabuleiro;
    private final Map<String, BufferedImage> piaos = new HashMap<>();
    private final BufferedImage yurt = Figuras.criarYurt();
    private List<ParadaInfo> paradas = new ArrayList<>();
    private Set<String> alcancaveis = new HashSet<>();
    private String jogadorDaVez;

    PainelTabuleiro(List<String> jogadores) {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        try {
            java.io.InputStream entrada = PainelTabuleiro.class.getResourceAsStream("img/tabuleiro.jpg");
            if (entrada == null) throw new IOException("Recurso ausente.");
            BufferedImage original = ImageIO.read(entrada);
            imagemTabuleiro = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = imagemTabuleiro.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(original, 0, 0, LARGURA, ALTURA, null);
            g.dispose();
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível carregar view/img/tabuleiro.jpg", e);
        }
        for (String jogador : jogadores)
            piaos.put(jogador, Figuras.criarPiao(Figuras.corDoJogador(jogador)));
    }

    void setEstado(List<ParadaInfo> novasParadas, Set<String> novosAlcancaveis, String vez) {
        paradas = new ArrayList<>(novasParadas);
        alcancaveis = new HashSet<>(novosAlcancaveis);
        jogadorDaVez = vez;
        repaint();
    }

    String paradaEm(int x, int y) {
        ParadaInfo maisProxima = null;
        double melhor = Double.MAX_VALUE;
        for (ParadaInfo parada : paradas) {
            double dx = x - parada.getX() * LARGURA;
            double dy = y - parada.getY() * ALTURA;
            double distancia = dx * dx + dy * dy;
            int raio = parada.isKarakorum() ? 45 : parada.isDupla() ? 27 : 23;
            if (distancia <= raio * raio && distancia < melhor) {
                melhor = distancia;
                maisProxima = parada;
            }
        }
        return maisProxima == null ? null : maisProxima.getId();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.drawImage(imagemTabuleiro, 0, 0, null);
        g2.setComposite(AlphaComposite.SrcOver.derive(.48f));
        g2.setColor(new Color(20, 22, 31));
        g2.fillRect(0, 0, LARGURA, ALTURA);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Map<String, ParadaInfo> porId = new HashMap<>();
        for (ParadaInfo parada : paradas) porId.put(parada.getId(), parada);
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (ParadaInfo parada : paradas) {
            for (String vizinhaId : parada.getVizinhas()) {
                if (parada.getId().compareTo(vizinhaId) >= 0) continue;
                ParadaInfo vizinha = porId.get(vizinhaId);
                if (vizinha == null) continue;
                g2.setColor(new Color(255, 246, 208, 220));
                g2.drawLine(px(parada), py(parada), px(vizinha), py(vizinha));
            }
        }

        for (ParadaInfo parada : paradas) {
            int x = px(parada), y = py(parada);
            int raio = parada.isKarakorum() ? 33 : parada.isDupla() ? 22 : 17;
            Color cor = alcancaveis.contains(parada.getId())
                    ? new Color(255, 221, 82) : new Color(244, 247, 251);
            g2.setColor(new Color(30, 31, 48, 220));
            g2.fillOval(x - raio - 3, y - raio - 3, 2 * raio + 6, 2 * raio + 6);
            g2.setColor(cor);
            g2.fillOval(x - raio, y - raio, 2 * raio, 2 * raio);
            g2.setColor(new Color(53, 63, 83));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x - raio, y - raio, 2 * raio, 2 * raio);
            g2.setFont(new Font("SansSerif", Font.BOLD, parada.isKarakorum() ? 10 : 9));
            String rotulo = parada.isKarakorum() ? "K" : parada.getId().replace("RUSSIA", "R")
                    .replace("CHINA", "C").replace("PERSIA", "P");
            int larguraTexto = g2.getFontMetrics().stringWidth(rotulo);
            g2.drawString(rotulo, x - larguraTexto / 2, y + 3);

            for (int i = 0; i < parada.getIurtas().size(); i++)
                g2.drawImage(yurt, x - 8 + i * 14, y - raio - 15, 17, 17, null);
            List<String> ocupantes = parada.getPeoes();
            for (int i = 0; i < ocupantes.size(); i++) {
                String corPeao = ocupantes.get(i);
                int deslocamento = (i * 23) - ((ocupantes.size() - 1) * 23 / 2);
                BufferedImage imagem = piaos.get(corPeao);
                if (imagem != null)
                    g2.drawImage(imagem, x + deslocamento - 12, y - 39, 24, 34, null);
                if (corPeao.equals(jogadorDaVez)) {
                    g2.setColor(new Color(255, 230, 76));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(x + deslocamento - 14, y - 41, 28, 38);
                }
            }
        }
        g2.setColor(new Color(22, 25, 36, 215));
        g2.fillRoundRect(10, ALTURA - 30, 320, 23, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("Círculos e rotas claros: trechos jogáveis", 19, ALTURA - 14);
        g2.dispose();
    }

    private int px(ParadaInfo parada) { return (int) Math.round(parada.getX() * LARGURA); }
    private int py(ParadaInfo parada) { return (int) Math.round(parada.getY() * ALTURA); }
}
