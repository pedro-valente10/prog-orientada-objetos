package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import model.ConselheiroInfo;
import model.Jogo;

/**
 * Janela principal (mapa + painel de controle) e, como janelas auxiliares, a cartela e as melhorias.
 * Todos os componentes que refletem o estado da partida são registrados como observadores do Jogo.
 */
public class JanelaJogo extends JFrame {
    private static final long serialVersionUID = 1L;

    private final PainelTabuleiro painelTabuleiro;
    private final PainelControle painelControle;
    private final JanelaCartela janelaCartela;
    private final JanelaMelhorias janelaMelhorias;

    public JanelaJogo(Jogo jogo) {
        super("Herdeiros do Khan");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        painelTabuleiro = new PainelTabuleiro(jogo);
        painelControle = new PainelControle(jogo);
        janelaCartela = new JanelaCartela(this, jogo);
        janelaMelhorias = new JanelaMelhorias(this, jogo);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(painelTabuleiro, BorderLayout.CENTER);
        getContentPane().add(painelControle, BorderLayout.EAST);
        pack();
        if (getWidth() > 1400 || getHeight() > 1050)
            throw new IllegalStateException("Janela acima do limite de 1400 x 1050.");
        setLocationRelativeTo(null);

        // Observer: a View se registra no Model para ser avisada das mudanças de estado.
        jogo.add(painelTabuleiro);
        jogo.add(painelControle);
        jogo.add(janelaCartela);
        jogo.add(janelaMelhorias);
    }

    // ---- ouvintes (instalados pelo Controller) ----
    public void addOuvinteTabuleiro(MouseListener ouvinte) { painelTabuleiro.addMouseListener(ouvinte); }
    public void addOuvinteControle(MouseListener ouvinte) { painelControle.addMouseListener(ouvinte); }
    public void addOuvinteCartela(MouseListener ouvinte) { janelaCartela.getPainel().addMouseListener(ouvinte); }
    public void addOuvinteMelhorias(MouseListener ouvinte) { janelaMelhorias.getPainel().addMouseListener(ouvinte); }
    public void addOuvinteFechamentoCartela(WindowListener ouvinte) { janelaCartela.addWindowListener(ouvinte); }
    public void addOuvinteFechamentoMelhorias(WindowListener ouvinte) { janelaMelhorias.addWindowListener(ouvinte); }

    // ---- consultas de cliques ----
    public String paradaEm(int x, int y) { return painelTabuleiro.paradaEm(x, y); }
    public String acaoEm(int x, int y) { return painelControle.acaoEm(x, y); }
    public String acaoNaCartela(int x, int y) { return janelaCartela.getPainel().acaoEm(x, y); }
    public String acaoNasMelhorias(int x, int y) { return janelaMelhorias.getPainel().acaoEm(x, y); }

    // ---- estado puramente visual ----
    public void setModo(String modo) { painelControle.setModo(modo); }
    public void mostrarMensagem(String mensagem) { painelControle.setMensagem(mensagem); }

    /** Acomoda as janelas auxiliares ao lado da principal quando a tela comporta; senão, sobre ela. */
    public void posicionarJanelas() {
        Rectangle tela = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        Dimension cartela = janelaCartela.getSize();
        Dimension melhorias = janelaMelhorias.getSize();
        int larguraDireita = Math.max(cartela.width, melhorias.width);
        if (tela.width >= getWidth() + larguraDireita) {
            setLocation(tela.x + (tela.width - getWidth() - larguraDireita) / 2, getY());
        }
        int xDireita = getX() + getWidth();
        if (xDireita + larguraDireita > tela.x + tela.width) xDireita = tela.x + tela.width - larguraDireita;
        janelaCartela.setLocation(xDireita, getY());
        janelaMelhorias.setLocation(xDireita, getY() + cartela.height);
    }

    // ---- diálogos ----
    /**
     * Pergunta o que entregar ao conselheiro. Devolve a oferta (vazia quando a exigência é fixa e o
     * Model a aplica sozinho) ou null se o jogador desistir.
     */
    public Map<String, Integer> pedirEntrega(String paradaId, ConselheiroInfo conselheiro, List<String> tipos) {
        String titulo = "Conselheiro de " + paradaId;
        if (!conselheiro.isQuaisquer()) {
            int resposta = JOptionPane.showConfirmDialog(this,
                    "Entregar " + conselheiro.descricao() + " em troca de " + conselheiro.getVotos() + " voto(s)?",
                    titulo, JOptionPane.YES_NO_OPTION);
            return resposta == JOptionPane.YES_OPTION ? new HashMap<String, Integer>() : null;
        }
        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 4));
        List<JSpinner> campos = new ArrayList<>();
        for (String tipo : tipos) {
            JSpinner campo = new JSpinner(new SpinnerNumberModel(0, 0, conselheiro.getQuantidadeQualquer(), 1));
            campos.add(campo);
            formulario.add(new JLabel(tipo));
            formulario.add(campo);
        }
        int resposta = JOptionPane.showConfirmDialog(this, new Object[] {
                "Escolha exatamente " + conselheiro.getQuantidadeQualquer() + " tesouros ("
                        + conselheiro.getVotos() + " voto):", formulario },
                titulo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resposta != JOptionPane.OK_OPTION) return null;
        Map<String, Integer> oferta = new HashMap<>();
        for (int i = 0; i < tipos.size(); i++) {
            int quantidade = ((Integer) campos.get(i).getValue()).intValue();
            if (quantidade > 0) oferta.put(tipos.get(i), quantidade);
        }
        return oferta;
    }

    public void mostrarFimDeJogo(List<String> vencedores, Map<String, Integer> votos) {
        StringBuilder texto = new StringBuilder("A partida terminou.\n\nVotos finais:\n");
        for (Map.Entry<String, Integer> e : votos.entrySet())
            texto.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        texto.append("\nVencedor(es): ");
        for (int i = 0; i < vencedores.size(); i++)
            texto.append(i > 0 ? ", " : "").append(vencedores.get(i));
        JOptionPane.showMessageDialog(this, texto.toString(), "Fim de jogo", JOptionPane.INFORMATION_MESSAGE);
    }
}
