package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Desenha o tabuleiro e os piões. Tudo é exibido com Graphics2D.drawImage().
 * Também converte um clique do mouse na parada correspondente.
 */
class PainelTabuleiro extends JPanel {

    private static final long serialVersionUID = 1L;

    static final int LARGURA = 970;
    static final int ALTURA = 975;
    private static final double RAIO_CLIQUE = 0.027; // fração da largura

    /** Posição (fração da imagem) de cada parada. Paradas duplas têm dois pontos. */
    private static final Map<String, double[][]> PARADAS = new LinkedHashMap<>();
    static {
        PARADAS.put("KARAKORUM", new double[][] { { 0.4991, 0.4950 } });
        PARADAS.put("RUSSIA1", new double[][] { { 0.5339, 0.2233 } });
        PARADAS.put("RUSSIA2", new double[][] { { 0.7527, 0.1686 }, { 0.7857, 0.1686 } });
        PARADAS.put("RUSSIA3", new double[][] { { 0.4377, 0.1677 } });
        PARADAS.put("RUSSIA4", new double[][] { { 0.2912, 0.0620 }, { 0.3205, 0.0820 } });
        PARADAS.put("CHINA1", new double[][] { { 0.7399, 0.4248 } });
        PARADAS.put("CHINA2", new double[][] { { 0.8810, 0.5123 }, { 0.8810, 0.5488 } });
        PARADAS.put("CHINA3", new double[][] { { 0.8660, 0.6980 } });
        PARADAS.put("CHINA4", new double[][] { { 0.6154, 0.7566 }, { 0.6502, 0.7593 } });
        PARADAS.put("PERSIA1", new double[][] { { 0.3297, 0.5324 } });
        PARADAS.put("PERSIA2", new double[][] { { 0.1621, 0.2881 }, { 0.1612, 0.3209 } });
        PARADAS.put("PERSIA3", new double[][] { { 0.0623, 0.6527 } });
        PARADAS.put("PERSIA4", new double[][] { { 0.2289, 0.6454 }, { 0.2683, 0.6472 } });
    }

    // Karakorum aceita qualquer clique dentro do escudo (fração x1, y1, x2, y2)
    private static final double[] AREA_KARAKORUM = { 0.414, 0.356, 0.586, 0.538 };

    private final BufferedImage imagemTabuleiro;
    private final Map<String, BufferedImage> piaos = new HashMap<>();
    private final List<String> jogadores = new ArrayList<>();
    private final Map<String, String> posicoes = new HashMap<>();

    PainelTabuleiro(List<String> nomesJogadores) {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        imagemTabuleiro = carregarTabuleiro();
        for (String nome : nomesJogadores) {
            jogadores.add(nome);
            piaos.put(nome, Figuras.criarPiao(Figuras.corDoJogador(nome)));
        }
    }

    private static BufferedImage carregarTabuleiro() {
        try {
            BufferedImage original = ImageIO.read(PainelTabuleiro.class.getResourceAsStream("img/tabuleiro.jpg"));
            BufferedImage ajustada = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = ajustada.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(original, 0, 0, LARGURA, ALTURA, null);
            g.dispose();
            return ajustada;
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Não foi possível carregar view/img/tabuleiro.jpg", e);
        }
    }

    /** Atualiza a parada de cada jogador (chave: nome da cor) e redesenha. */
    void setPosicoes(Map<String, String> novasPosicoes) {
        posicoes.clear();
        posicoes.putAll(novasPosicoes);
        repaint();
    }

    /** Retorna o id da parada sob o ponto (x, y) do painel, ou null se não houver. */
    String paradaEm(int x, int y) {
        double fx = (double) x / LARGURA;
        double fy = (double) y / ALTURA;
        if (fx >= AREA_KARAKORUM[0] && fx <= AREA_KARAKORUM[2]
                && fy >= AREA_KARAKORUM[1] && fy <= AREA_KARAKORUM[3]) {
            return "KARAKORUM";
        }
        for (Map.Entry<String, double[][]> e : PARADAS.entrySet()) {
            for (double[] ponto : e.getValue()) {
                double dx = (fx - ponto[0]) * LARGURA;
                double dy = (fy - ponto[1]) * ALTURA;
                double raio = RAIO_CLIQUE * LARGURA;
                if (dx * dx + dy * dy <= raio * raio) {
                    return e.getKey();
                }
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(imagemTabuleiro, 0, 0, null);

        // agrupa os piões por parada, na ordem dos jogadores
        Map<String, List<String>> porParada = new HashMap<>();
        for (String nome : jogadores) {
            String parada = posicoes.get(nome);
            if (parada != null) {
                if (!porParada.containsKey(parada)) {
                    porParada.put(parada, new ArrayList<String>());
                }
                porParada.get(parada).add(nome);
            }
        }
        for (Map.Entry<String, List<String>> e : porParada.entrySet()) {
            double[][] pontos = PARADAS.get(e.getKey());
            List<String> ocupantes = e.getValue();
            for (int i = 0; i < ocupantes.size(); i++) {
                double fx;
                double fy;
                if (pontos.length == 1) {
                    // espalha lado a lado (Karakorum comporta vários piões)
                    fx = pontos[0][0] + (i - (ocupantes.size() - 1) / 2.0) * 0.032;
                    fy = pontos[0][1];
                } else {
                    fx = pontos[Math.min(i, pontos.length - 1)][0];
                    fy = pontos[Math.min(i, pontos.length - 1)][1];
                }
                int px = (int) Math.round(fx * LARGURA) - Figuras.LARGURA_PIAO / 2;
                int py = (int) Math.round(fy * ALTURA) - Figuras.ALTURA_PIAO + 10;
                g2.drawImage(piaos.get(ocupantes.get(i)), px, py, null);
            }
        }
    }
}
