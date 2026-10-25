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

/** Tabuleiro individual, oferta e ações. Toda a composição é exibida via drawImage. */
class PainelControle extends JPanel {
    private static final long serialVersionUID = 1L;
    static final int LARGURA = 480;
    static final int ALTURA = 854;

    private final Jogo jogo;
    private final List<Alvo> alvos = new ArrayList<>();
    private String jogadorExibido;
    private String mensagem = "Escolha uma coluna para começar.";
    private boolean modoYurt;

    PainelControle(Jogo jogo) {
        this.jogo = jogo;
        jogadorExibido = jogo.getJogadorDaVez();
        setPreferredSize(new Dimension(LARGURA, ALTURA));
    }

    void atualizar() {
        jogadorExibido = jogo.getJogadorDaVez();
        repaint();
    }

    void setMensagem(String texto) { mensagem = texto; repaint(); }
    void setModoYurt(boolean ativo) { modoYurt = ativo; repaint(); }
    boolean isModoYurt() { return modoYurt; }

    String acaoEm(int x, int y) {
        for (Alvo alvo : alvos)
            if (alvo.area.contains(x, y)) return alvo.acao;
        return null;
    }

    void exibirJogador(String cor) {
        if (jogo.getJogadores().contains(cor)) {
            jogadorExibido = cor;
            repaint();
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage tela = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_ARGB);
        Graphics2D d = tela.createGraphics();
        d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        d.setColor(new Color(31, 37, 52));
        d.fillRect(0, 0, LARGURA, ALTURA);
        alvos.clear();
        desenharCabecalho(d);
        desenharTabuleiroIndividual(d);
        desenharRecursos(d);
        desenharOferta(d);
        desenharAcoes(d);
        d.dispose();
        ((Graphics2D) g).drawImage(tela, 0, 0, null);
    }

    private void desenharCabecalho(Graphics2D d) {
        d.setColor(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.BOLD, 17));
        d.drawString("Herdeiros do Khan", 14, 23);
        int x = 14;
        List<String> jogadores = jogo.getJogadores();
        int largura = Math.min(112, (LARGURA - 28 - 4 * (jogadores.size() - 1))
                / jogadores.size());
        for (String cor : jogadores) {
            d.setColor(Figuras.corDoJogador(cor));
            d.fillRoundRect(x, 30, largura, 25, 8, 8);
            if (cor.equals(jogadorExibido)) {
                d.setColor(Color.WHITE);
                d.setStroke(new BasicStroke(2));
                d.drawRoundRect(x, 30, largura, 25, 8, 8);
            }
            d.setColor(Color.WHITE);
            d.setFont(new Font("SansSerif", Font.BOLD, 11));
            d.drawString(cor + (cor.equals(jogo.getJogadorDaVez()) ? " *" : ""), x + 8, 47);
            alvos.add(new Alvo("VER:" + cor, new Rectangle(x, 30, largura, 25)));
            x += largura + 4;
        }
    }

    private void desenharTabuleiroIndividual(Graphics2D d) {
        boolean vezExibida = jogadorExibido.equals(jogo.getJogadorDaVez());
        Coluna[] colunas = Coluna.values();
        for (int c = 0; c < colunas.length; c++) {
            Coluna coluna = colunas[c];
            int x = 14 + c * 115;
            Color cor = corColuna(coluna);
            d.setColor(cor);
            d.fillRoundRect(x, 68, 107, 263, 10, 10);
            d.setColor(new Color(20, 25, 35));
            d.setFont(new Font("SansSerif", Font.BOLD, 12));
            d.drawString(coluna.name(), x + 6, 85);
            boolean disponivel = jogo.isColunaDisponivel(jogadorExibido, coluna);
            d.setFont(new Font("SansSerif", Font.BOLD, 10));
            d.drawString(disponivel ? "FICHA LIVRE" : "FICHA USADA", x + 6, 100);
            if (vezExibida && !jogo.isTurnoAtivo() && disponivel)
                alvos.add(new Alvo("COLUNA:" + coluna.name(), new Rectangle(x, 68, 107, 36)));
            List<Melhoria> pecas = jogo.getMelhorias(jogadorExibido, coluna);
            for (int linha = 3; linha >= 0; linha--) {
                int y = 106 + (3 - linha) * 43;
                d.setColor(new Color(246, 247, 250, 230));
                d.fillRoundRect(x + 5, y, 97, 39, 6, 6);
                d.setColor(new Color(44, 49, 62));
                d.drawRoundRect(x + 5, y, 97, 39, 6, 6);
                Melhoria peca = pecas.get(linha);
                d.setFont(new Font("SansSerif", Font.BOLD, 12));
                d.drawString(peca == null ? "Espaço " + (linha + 1) : nomePeca(peca), x + 11, y + 23);
            }
            d.setColor(new Color(18, 22, 33, 225));
            d.fillRoundRect(x + 5, 282, 97, 43, 7, 7);
            d.setColor(Color.WHITE);
            d.setFont(new Font("SansSerif", Font.BOLD, 12));
            d.drawString(jogo.getCavalosIniciais(coluna) + " cavalo(s)", x + 11, 299);
            d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String extra = coluna == Coluna.BRANCA ? "recuperar" :
                    jogo.isKhanInicial(coluna) ? "Khan" :
                    jogo.getPunhosIniciais(coluna) + " punho(s)";
            d.drawString(extra, x + 11, 316);
        }
    }

    private void desenharRecursos(Graphics2D d) {
        d.setColor(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.BOLD, 12));
        String vez = jogo.getJogadorDaVez();
        d.drawString("Vez: " + vez + " | " + (jogo.isTurnoAtivo() ?
                "coluna " + jogo.getColunaAtiva() : "escolha coluna"), 14, 352);
        d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        d.drawString("Movimentos " + jogo.getMovimentosRestantes() + "   Punhos "
                + jogo.getPunhosRestantes() + "   Khan " + (jogo.isKhanPendente() ? "obrigatório" : "-"), 14, 370);
        d.drawString("Moedas " + jogo.getTributos(vez, "MOEDA") + " (virtual "
                + jogo.getMoedasVirtuais() + ")   Espadas " + jogo.getTributos(vez, "ESPADA")
                + " (virtual " + jogo.getEspadasVirtuais() + ")", 14, 387);
        d.drawString("Yurts " + jogo.getIurtasDisponiveis(vez) + " | tributo "
                + jogo.getTributos(vez, "IURTA") + " (virtual " + jogo.getYurtsVirtuais()
                + ")   Brancas grátis " + jogo.getBonusBrancosPendentes(vez), 14, 404);
    }

    private void desenharOferta(Graphics2D d) {
        d.setColor(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.BOLD, 13));
        d.drawString("Oferta de melhorias — clique para comprar ou escolher bônus", 14, 426);
        List<Melhoria> oferta = jogo.getOfertaMelhorias();
        for (int i = 0; i < oferta.size(); i++) {
            int x = 14 + i * 92;
            Melhoria peca = oferta.get(i);
            d.setColor(peca == null ? new Color(78, 83, 94) : corColuna(Coluna.valueOf(peca.getCor())));
            d.fillRoundRect(x, 436, 86, 71, 9, 9);
            d.setColor(new Color(24, 29, 40));
            d.setFont(new Font("SansSerif", Font.BOLD, 11));
            d.drawString(peca == null ? "Vazia" : nomePeca(peca), x + 6, 463);
            if (peca != null) {
                d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                d.drawString(peca.getCor(), x + 6, 481);
                d.drawString("#" + peca.getId(), x + 6, 497);
                alvos.add(new Alvo("MELHORIA:" + i, new Rectangle(x, 436, 86, 71)));
            }
        }
        d.setColor(new Color(180, 194, 217));
        d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        d.drawString("Pilha: " + jogo.getMelhoriasNaPilha() + " peças; a oferta só repõe com o Khan.", 14, 523);
        d.drawString(mensagem, 14, 540);
    }

    private void desenharAcoes(Graphics2D d) {
        botao(d, "Encerrar", "FIM", 0, 0, true);
        botao(d, "Pilha", "MELHORIA:-1", 0, 1, jogo.getMelhoriasNaPilha() > 0);
        botao(d, modoYurt ? "Yurt: ON" : "Yurt", "YURT", 0, 2, true);
        botao(d, "Khan área", "KHAN:MELHORIAS", 0, 3, true);
        for (int i = 1; i <= 4; i++) {
            botao(d, "Khan K" + i, "KHAN:K" + i, 1, i - 1, true);
            botao(d, "Coletar K" + i, "COLETA:K" + i, 2, i - 1, true);
            botao(d, "Coletar P" + i, "COLETA:P" + i, 3, i - 1, true);
        }
        List<String> cidades = jogo.getCidadesReveladas();
        for (int i = 0; i < 4; i++) {
            String cidade = i < cidades.size() ? cidades.get(i) : null;
            botao(d, cidade == null ? "-" : "Atacar " + cidade,
                    cidade == null ? null : "ATAQUE:" + cidade, 4, i, cidade != null);
        }
        d.setColor(new Color(200, 211, 230));
        d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        d.drawString("Yurt: ative o modo e clique uma parada visitada.", 14, 812);
        d.drawString("Ataque: 1 tesouro por clique; o Model calcula as espadas.", 14, 829);
    }

    private void botao(Graphics2D d, String texto, String acao, int linha, int coluna, boolean habilitado) {
        int x = 14 + coluna * 115, y = 552 + linha * 49;
        d.setColor(habilitado ? new Color(83, 115, 151) : new Color(72, 76, 85));
        d.fillRoundRect(x, y, 107, 43, 8, 8);
        d.setColor(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.BOLD, 11));
        if (d.getFontMetrics().stringWidth(texto) > 94)
            d.setFont(new Font("SansSerif", Font.BOLD, 9));
        d.drawString(texto, x + 7, y + 25);
        if (habilitado && acao != null)
            alvos.add(new Alvo(acao, new Rectangle(x, y, 107, 43)));
    }

    private static Color corColuna(Coluna coluna) {
        switch (coluna) {
            case CHINA: return new Color(213, 120, 117);
            case PERSIA: return new Color(133, 185, 125);
            case RUSSIA: return new Color(231, 194, 108);
            default: return new Color(219, 224, 234);
        }
    }

    private static String nomePeca(Melhoria peca) {
        switch (peca.getTipo()) {
            case CAVALOS: return "+2 cavalos";
            case PUNHO: return "+1 punho";
            case MOEDA: return "moeda virtual";
            case ESPADA: return "espada virtual";
            case YURT: return "yurt virtual";
            case TESOURO: return "tes. " + peca.getTesouro();
            default: return peca.getTipo().name();
        }
    }

    private static final class Alvo {
        final String acao;
        final Rectangle area;
        Alvo(String acao, Rectangle area) { this.acao = acao; this.area = area; }
    }
}
