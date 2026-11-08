package view;

import java.awt.BasicStroke;
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

/** Cartela: tabuleiro individual de um jogador, com as quatro colunas e seus recursos. */
class PainelCartela extends JPanel {
    private static final long serialVersionUID = 1L;
    static final int LARGURA = 480;
    static final int ALTURA = 396;
    private static final int TOPO = 44;

    private final Jogo jogo;
    private final List<Alvo> alvos = new ArrayList<>();

    PainelCartela(Jogo jogo) {
        this.jogo = jogo;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
    }

    String acaoEm(int x, int y) { return Alvo.acaoEm(alvos, x, y); }

    private String jogadorExibido() {
        String cor = jogo.getCartelaExibida();
        return cor == null ? jogo.getJogadorDaVez() : cor;
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage tela = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_ARGB);
        Graphics2D d = tela.createGraphics();
        d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        d.setColor(new Color(31, 37, 52));
        d.fillRect(0, 0, LARGURA, ALTURA);
        alvos.clear();
        String exibido = jogadorExibido();
        desenharAbas(d, exibido);
        desenharColunas(d, exibido);
        desenharResumo(d, exibido);
        d.dispose();
        ((Graphics2D) g).drawImage(tela, 0, 0, null);
    }

    private void desenharAbas(Graphics2D d, String exibido) {
        List<String> jogadores = jogo.getJogadores();
        int largura = Math.min(112, (LARGURA - 28 - 4 * (jogadores.size() - 1)) / jogadores.size());
        int x = 14;
        for (String cor : jogadores) {
            d.setColor(Figuras.corDoJogador(cor));
            d.fillRoundRect(x, 10, largura, 25, 8, 8);
            if (cor.equals(exibido)) {
                d.setColor(Color.WHITE);
                d.setStroke(new BasicStroke(2));
                d.drawRoundRect(x, 10, largura, 25, 8, 8);
            }
            d.setColor(Color.WHITE);
            d.setFont(new Font("SansSerif", Font.BOLD, 11));
            d.drawString(cor + (cor.equals(jogo.getJogadorDaVez()) ? " *" : ""), x + 8, 27);
            alvos.add(new Alvo("VER:" + cor, new Rectangle(x, 10, largura, 25)));
            x += largura + 4;
        }
    }

    private void desenharColunas(Graphics2D d, String exibido) {
        boolean vezExibida = exibido.equals(jogo.getJogadorDaVez());
        Coluna[] colunas = Coluna.values();
        for (int c = 0; c < colunas.length; c++) {
            Coluna coluna = colunas[c];
            int x = 14 + c * 115;
            d.setColor(Figuras.corColuna(coluna));
            d.fillRoundRect(x, TOPO, 107, 263, 10, 10);
            d.setColor(new Color(20, 25, 35));
            d.setFont(new Font("SansSerif", Font.BOLD, 12));
            d.drawString(coluna.name(), x + 6, TOPO + 17);
            boolean disponivel = jogo.isColunaDisponivel(exibido, coluna);
            d.setFont(new Font("SansSerif", Font.BOLD, 10));
            d.drawString(disponivel ? "FICHA LIVRE" : "FICHA USADA", x + 6, TOPO + 32);
            if (vezExibida && !jogo.isTurnoAtivo() && !jogo.isJogoEncerrado() && disponivel)
                alvos.add(new Alvo("COLUNA:" + coluna.name(), new Rectangle(x, TOPO, 107, 36)));
            List<Melhoria> pecas = jogo.getMelhorias(exibido, coluna);
            for (int linha = 3; linha >= 0; linha--) {
                int y = TOPO + 38 + (3 - linha) * 43;
                d.setColor(new Color(246, 247, 250, 230));
                d.fillRoundRect(x + 5, y, 97, 39, 6, 6);
                d.setColor(new Color(44, 49, 62));
                d.drawRoundRect(x + 5, y, 97, 39, 6, 6);
                Melhoria peca = pecas.get(linha);
                d.setFont(new Font("SansSerif", Font.BOLD, 12));
                d.drawString(peca == null ? "Espaço " + (linha + 1) : Figuras.nomePeca(peca), x + 11, y + 23);
            }
            d.setColor(new Color(18, 22, 33, 225));
            d.fillRoundRect(x + 5, TOPO + 214, 97, 43, 7, 7);
            d.setColor(Color.WHITE);
            d.setFont(new Font("SansSerif", Font.BOLD, 12));
            d.drawString(jogo.getCavalosIniciais(coluna) + " cavalo(s)", x + 11, TOPO + 231);
            d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String extra = coluna == Coluna.BRANCA ? "recuperar" :
                    jogo.isKhanInicial(coluna) ? "Khan" :
                    jogo.getPunhosIniciais(coluna) + " punho(s)";
            d.drawString(extra, x + 11, TOPO + 248);
        }
    }

    private void desenharResumo(Graphics2D d, String exibido) {
        d.setColor(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.BOLD, 13));
        d.drawString("Cartela de " + exibido + (exibido.equals(jogo.getJogadorDaVez()) ? " (jogador da vez)" : ""),
                14, 328);
        d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        d.drawString("Moedas " + jogo.getTributos(exibido, "MOEDA") + "   Espadas "
                + jogo.getTributos(exibido, "ESPADA") + "   Tributos de yurt "
                + jogo.getTributos(exibido, "IURTA") + "   Yurts na reserva "
                + jogo.getIurtasDisponiveis(exibido), 14, 346);
        StringBuilder tesouros = new StringBuilder("Tesouros:");
        for (String tipo : jogo.getTiposTesouro())
            tesouros.append("  ").append(Figuras.abreviar(tipo)).append(" ").append(jogo.getTesouros(exibido, tipo));
        d.drawString(tesouros.toString(), 14, 364);
        d.drawString("Votos no Kurultai " + jogo.getVotos(exibido) + "   Peão em " + jogo.getPosicao(exibido), 14, 382);
    }
}
