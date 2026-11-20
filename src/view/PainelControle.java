package view;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import model.Jogo;
import model.ObservadoIF;
import model.ObservadorIF;

/** Painel principal de controle: situação do turno, Kurultai, botões de ação e mensagens. */
class PainelControle extends JPanel implements ObservadorIF {
    private static final long serialVersionUID = 1L;
    static final int LARGURA = 480;
    static final int ALTURA = 854;
    private static final int TOPO_BOTOES = 270;

    private final Jogo jogo;
    private final List<Alvo> alvos = new ArrayList<>();
    private String mensagem = "Abra a cartela e clique no cabeçalho de uma coluna para começar.";
    private String modo;

    PainelControle(Jogo jogo) {
        this.jogo = jogo;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
    }

    /** Observer: qualquer mudança de estado da partida redesenha o painel. */
    @Override public void notify(ObservadoIF observado) { repaint(); }

    void setMensagem(String texto) { mensagem = texto; repaint(); }
    /** Modo de clique em andamento ("YURT", "ENTREGAR") ou null; só afeta o destaque dos botões. */
    void setModo(String novoModo) { modo = novoModo; repaint(); }

    String acaoEm(int x, int y) { return Alvo.acaoEm(alvos, x, y); }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage tela = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_ARGB);
        Graphics2D d = tela.createGraphics();
        d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        d.setColor(new Color(31, 37, 52));
        d.fillRect(0, 0, LARGURA, ALTURA);
        alvos.clear();
        desenharCabecalho(d);
        desenharTurno(d);
        desenharKurultai(d);
        desenharAcoes(d);
        desenharMensagem(d);
        d.dispose();
        ((Graphics2D) g).drawImage(tela, 0, 0, null);
    }

    private void desenharCabecalho(Graphics2D d) {
        d.setColor(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.BOLD, 17));
        d.drawString("Herdeiros do Khan", 14, 23);
        d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        d.drawString("clique num jogador: ver cartela", 190, 23);
        List<String> jogadores = jogo.getJogadores();
        int largura = Math.min(112, (LARGURA - 28 - 4 * (jogadores.size() - 1)) / jogadores.size());
        int x = 14;
        for (String cor : jogadores) {
            d.setColor(Figuras.corDoJogador(cor));
            d.fillRoundRect(x, 30, largura, 25, 8, 8);
            if (cor.equals(jogo.getJogadorDaVez())) {
                d.setColor(Color.WHITE);
                d.setStroke(new BasicStroke(2));
                d.drawRoundRect(x, 30, largura, 25, 8, 8);
            }
            d.setColor(Color.WHITE);
            d.setFont(new Font("SansSerif", Font.BOLD, 11));
            String nome = jogo.getNomeJogador(cor);
            String rotulo = nome.equals(cor) ? cor : cor.substring(0, 3) + " " + nome;
            while (rotulo.length() > 4 && d.getFontMetrics().stringWidth(rotulo) > largura - 16)
                rotulo = rotulo.substring(0, rotulo.length() - 1);
            d.drawString(rotulo, x + 8, 47);
            alvos.add(new Alvo("VER:" + cor, new Rectangle(x, 30, largura, 25)));
            x += largura + 4;
        }
    }

    private void desenharTurno(Graphics2D d) {
        String vez = jogo.getJogadorDaVez();
        d.setColor(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.BOLD, 13));
        String situacao = jogo.isJogoEncerrado() ? "partida encerrada"
                : jogo.isTurnoAtivo() ? "coluna " + jogo.getColunaAtiva() : "escolha uma coluna";
        String nome = jogo.getNomeJogador(vez);
        String identidade = nome.equals(vez) ? vez : nome + " [" + vez + "]";
        String linha = "Vez: " + identidade + " (" + jogo.getHerdeiro(vez).getNome() + ") | " + situacao;
        if (d.getFontMetrics().stringWidth(linha) > 450) d.setFont(new Font("SansSerif", Font.BOLD, 11));
        d.drawString(linha, 14, 84);
        d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        d.drawString("Movimentos " + jogo.getMovimentosRestantes() + "   Punhos "
                + jogo.getPunhosRestantes() + "   Khan " + (jogo.isKhanPendente() ? "obrigatório" : "-"), 14, 102);
        d.drawString("Moedas " + jogo.getTributos(vez, "MOEDA") + " (virtual "
                + jogo.getMoedasVirtuais() + ")   Espadas " + jogo.getTributos(vez, "ESPADA")
                + " (virtual " + jogo.getEspadasVirtuais() + ")", 14, 119);
        d.drawString("Yurts " + jogo.getIurtasDisponiveis(vez) + " | tributo " + jogo.getTributos(vez, "IURTA")
                + " (virtual " + jogo.getYurtsVirtuais() + ")   Brancas grátis "
                + jogo.getBonusBrancosPendentes(vez), 14, 136);
        StringBuilder tesouros = new StringBuilder("Tesouros:");
        for (String tipo : jogo.getTiposTesouro()) {
            tesouros.append("  ").append(Figuras.abreviar(tipo)).append(" ").append(jogo.getTesouros(vez, tipo));
            int virtual = jogo.getTesouroVirtual(tipo);
            if (virtual > 0) tesouros.append("(+").append(virtual).append(")");
        }
        d.drawString(tesouros.toString(), 14, 153);
        d.drawString("Peão em " + jogo.getPosicao(vez) + "   Khan em "
                + (jogo.getLocalDoKhan() == null ? "- (fora do tabuleiro)" : jogo.getLocalDoKhan()), 14, 170);
        d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        d.drawString(descricaoParadaAtual(vez), 14, 185);
    }

    private void desenharKurultai(Graphics2D d) {
        d.setColor(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.BOLD, 12));
        d.drawString("Kurultai: " + jogo.getOcupacaoKurultai() + "/" + jogo.getLimiteKurultai() + " votos"
                + (jogo.isFimAcionado() ? "  —  FIM DE JOGO ACIONADO" : ""), 14, 203);
        List<String> jogadores = jogo.getJogadores();
        int largura = (LARGURA - 28 - 4 * (jogadores.size() - 1)) / jogadores.size();
        int x = 14;
        for (String cor : jogadores) {
            d.setColor(Figuras.corDoJogador(cor));
            d.fillRoundRect(x, 211, largura, 22, 8, 8);
            d.setColor(Color.WHITE);
            d.setFont(new Font("SansSerif", Font.BOLD, 11));
            d.drawString(cor.substring(0, 3) + " " + jogo.getVotos(cor), x + 8, 226);
            x += largura + 4;
        }
        d.setColor(new Color(180, 194, 217));
        d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        d.drawString("Pilha de melhorias: " + jogo.getMelhoriasNaPilha() + " peças", 14, 253);
        d.drawString("Bônus recebidos: " + jogo.getBonusRecebidos(jogo.getJogadorDaVez()), 286, 253);
    }

    private void desenharAcoes(Graphics2D d) {
        boolean ativo = !jogo.isJogoEncerrado();
        botao(d, "Encerrar", jogo.isKhanPendente() ? "use o Khan" : null,
                "FIM", 0, 0, jogo.podeEncerrarTurno(), false);
        String impedimentoIurta = jogo.getImpedimentoIurta();
        botao(d, "Yurt", "YURT".equals(modo) ? "modo ligado"
                : impedimentoIurta == null ? "construir" : resumoImpedimento(impedimentoIurta),
                "YURT", 0, 1, impedimentoIurta == null, "YURT".equals(modo));
        String impedimentoEntrega = jogo.getImpedimentoEntrega();
        botao(d, "Entregar", "ENTREGAR".equals(modo) ? "modo ligado"
                : impedimentoEntrega == null ? "ao conselheiro" : resumoImpedimento(impedimentoEntrega),
                "ENTREGAR", 0, 2, impedimentoEntrega == null, "ENTREGAR".equals(modo));
        botao(d, "Khan área", "MELHORIAS".equals(jogo.getLocalDoKhan()) ? "Khan aqui" : null,
                "KHAN:MELHORIAS", 0, 3, jogo.podePosicionarKhan("MELHORIAS"), false);
        for (int i = 1; i <= 4; i++) {
            String id = "K" + i;
            botao(d, "Khan " + id, id.equals(jogo.getLocalDoKhan()) ? "Khan aqui" : null, "KHAN:" + id,
                    1, i - 1, jogo.podePosicionarKhan(id), false);
            String impedimentoK = jogo.getImpedimentoColeta(id);
            botao(d, "Coletar " + id, descricaoProvincia(id, impedimentoK), "COLETA:" + id,
                    2, i - 1, impedimentoK == null, false);
            String outra = "P" + i;
            String impedimentoP = jogo.getImpedimentoColeta(outra);
            botao(d, "Coletar " + outra, descricaoProvincia(outra, impedimentoP), "COLETA:" + outra,
                    3, i - 1, impedimentoP == null, false);
        }
        List<String> cidades = jogo.getCidadesReveladas();
        for (int i = 0; i < 4; i++) {
            String cidade = i < cidades.size() ? cidades.get(i) : null;
            String impedimento = cidade == null ? "Sem cidade." : "OGEDEI".equals(modo)
                    ? jogo.getImpedimentoOgedei(cidade) : jogo.getImpedimentoAtaque(cidade);
            botao(d, cidade == null ? "-" : ("OGEDEI".equals(modo) ? "Pegar " : "Atacar ") + cidade,
                    cidade == null ? null : impedimento == null
                            ? jogo.getTesourosDaCidade(cidade) + " tesouro(s)"
                            : resumoImpedimento(impedimento),
                    cidade == null ? null : "ATAQUE:" + cidade, 4, i,
                    cidade != null && impedimento == null, false);
        }
        botao(d, "Salvar", "entre turnos", "SALVAR", 6, 0,
                jogo.podeSalvar() && !jogo.isJogoEncerrado(), false);
        botao(d, "Recuperar", "arquivo .txt", "RECUPERAR", 6, 1, true, false);
        botao(d, "Conselheiros", "ver pedidos", "CONSELHEIROS", 6, 2, true, false);
        botao(d, "Áreas K/P", "ver paradas", "AREAS", 6, 3, true, false);
        botao(d, "Cartela", "abrir janela", "CARTELA", 5, 0, true, false);
        botao(d, "Melhorias", "abrir janela", "MELHORIAS", 5, 1, true, false);
        botao(d, "Cartas", "abrir janela", "CARTAS", 5, 2, true, false);
        boolean bonusPendente = !jogo.getOpcoesTesouroBonus().isEmpty();
        model.Herdeiro herdeiro = jogo.getHerdeiro(jogo.getJogadorDaVez());
        boolean habilidade = jogo.isTurnoAtivo() && !jogo.isHabilidadeUsadaNoTurno()
                && (herdeiro == model.Herdeiro.CHAGATAI && jogo.getMovimentosRestantes() > 0
                    || herdeiro == model.Herdeiro.OGEDEI && jogo.getCidadesReveladas().stream()
                            .anyMatch(c -> jogo.getImpedimentoOgedei(c) == null));
        botao(d, bonusPendente ? "Bônus" : "Habilidade",
                bonusPendente ? "escolher tesouro" : herdeiro == model.Herdeiro.CHAGATAI
                        ? "cavalo → espada" : herdeiro == model.Herdeiro.OGEDEI
                        ? "moeda → tesouro" : "indisponível",
                bonusPendente ? "BONUS" : "HABILIDADE", 5, 3,
                bonusPendente || habilidade, "OGEDEI".equals(modo));
    }

    private String descricaoParadaAtual(String vez) {
        String posicao = jogo.getPosicao(vez);
        for (model.ParadaInfo parada : jogo.getParadas()) {
            if (!parada.getId().equals(posicao)) continue;
            if (parada.isKarakorum()) return "Karakorum: sem província de coleta adjacente.";
            String regiao = posicao.startsWith("R") ? "Rússia"
                    : posicao.startsWith("C") ? "China" : "Pérsia";
            return "Parada " + posicao + " (" + regiao + ") → províncias "
                    + String.join(", ", parada.getProvinciasAdjacentes());
        }
        return "";
    }

    private String resumoImpedimento(String motivo) {
        if (motivo.contains("coluna")) return "aguarde turno";
        if (motivo.contains("parada") || motivo.contains("Pare")) return "longe";
        if (motivo.contains("espada")) return "sem espadas";
        if (motivo.contains("moeda")) return "sem moedas";
        if (motivo.contains("tesouro")) return "sem tesouros";
        if (motivo.contains("iurta")) return "sem iurtas";
        return "indisponível";
    }

    private String descricaoProvincia(String id, String impedimento) {
        String texto = jogo.getTipoDaProvincia(id) + " x" + jogo.getTributosDaProvincia(id);
        if (impedimento == null) return texto;
        if (impedimento.contains("adjacente")) return texto + " | longe";
        if (impedimento.contains("vazia")) return texto + " | vazia";
        if (impedimento.contains("ações")) return texto + " | sem punhos";
        return texto + " | aguarde";
    }

    private void botao(Graphics2D d, String texto, String sub, String acao, int linha, int coluna,
                       boolean habilitado, boolean destaque) {
        int x = 14 + coluna * 115, y = TOPO_BOTOES + linha * 49;
        Composite original = d.getComposite();
        if (!habilitado) d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
        d.setColor(destaque ? new Color(190, 120, 40) : new Color(83, 115, 151));
        d.fillRoundRect(x, y, 107, 43, 8, 8);
        d.setColor(Color.WHITE);
        int tamanho = 11;
        d.setFont(new Font("SansSerif", Font.BOLD, tamanho));
        if (d.getFontMetrics().stringWidth(texto) > 94) d.setFont(new Font("SansSerif", Font.BOLD, 9));
        d.drawString(texto, x + 7, sub == null ? y + 25 : y + 19);
        if (sub != null) {
            d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            if (d.getFontMetrics().stringWidth(sub) > 94)
                d.setFont(new Font("SansSerif", Font.PLAIN, 8));
            d.drawString(sub, x + 7, y + 34);
        }
        d.setComposite(original);
        if (habilitado && acao != null)
            alvos.add(new Alvo(acao, new Rectangle(x, y, 107, 43)));
    }

    private void desenharMensagem(Graphics2D d) {
        boolean compacto = getHeight() < 800;
        d.setColor(new Color(22, 25, 36));
        d.fillRoundRect(14, 610, 452, compacto ? 86 : 106, 8, 8);
        d.setColor(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.BOLD, 12));
        d.drawString("Mensagem", 24, 629);
        d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desenharTextoQuebrado(d, mensagem, 24, 648, 432, 16, compacto ? 3 : 4);

        d.setColor(new Color(200, 211, 230));
        d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String[] ajuda = {
            "Mapa: R=Rússia, P=Pérsia, C=China são PARADAS.",
            "Coleta: K1–K4=áreas Khan; P1–P4=outras PROVÍNCIAS.",
            "P1 no mapa é parada; Coletar P1 é uma província diferente.",
            "Passe o mouse numa parada ou use Áreas K/P para consultar.",
            "Botão claro = disponível; Conselheiros mostra os pedidos.",
            "Yurt / Entregar: ligue o modo e clique numa parada visitada."
        };
        for (int i = 0; i < ajuda.length; i++)
            d.drawString(ajuda[i], 14, (compacto ? 714 : 739) + i * (compacto ? 14 : 16));
    }

    private static void desenharTextoQuebrado(Graphics2D d, String texto, int x, int y, int largura,
                                              int alturaLinha, int maxLinhas) {
        FontMetrics fm = d.getFontMetrics();
        String linha = "";
        int n = 0;
        for (String palavra : texto.split(" ")) {
            String tentativa = linha.isEmpty() ? palavra : linha + " " + palavra;
            if (fm.stringWidth(tentativa) > largura && !linha.isEmpty()) {
                d.drawString(linha, x, y + n * alturaLinha);
                n++;
                if (n >= maxLinhas) return;
                linha = palavra;
            } else {
                linha = tentativa;
            }
        }
        if (!linha.isEmpty() && n < maxLinhas) d.drawString(linha, x, y + n * alturaLinha);
    }
}
