package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Janela do jogo: tabuleiro (Java2D) à esquerda e controles dos dados à direita. */
public class JanelaJogo extends JFrame {

    private static final long serialVersionUID = 1L;

    private final PainelTabuleiro painelTabuleiro;
    private final PainelDados painelDados = new PainelDados();
    private final JButton botaoJogarDados = new JButton("Jogar Dados");
    private final JButton botaoEscolherDados = new JButton("Escolher Dados");
    private final JComboBox<Integer> comboDado1 = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5, 6 });
    private final JComboBox<Integer> comboDado2 = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5, 6 });

    public JanelaJogo(List<String> jogadores) {
        super("Herdeiros do Khan - Jogo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        painelTabuleiro = new PainelTabuleiro(jogadores);

        JPanel lateral = new JPanel(new GridLayout(0, 1, 0, 8));
        lateral.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        lateral.add(painelDados);
        lateral.add(botaoJogarDados);
        JPanel escolha = new JPanel(new GridLayout(1, 2, 8, 0));
        escolha.add(comboDado1);
        escolha.add(comboDado2);
        lateral.add(escolha);
        lateral.add(botaoEscolherDados);

        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setBackground(Color.DARK_GRAY);
        envoltorio.add(lateral, BorderLayout.NORTH);
        envoltorio.add(new JLabel(), BorderLayout.CENTER);
        envoltorio.setPreferredSize(new java.awt.Dimension(280, PainelTabuleiro.ALTURA));

        getContentPane().add(painelTabuleiro, BorderLayout.CENTER);
        getContentPane().add(envoltorio, BorderLayout.EAST);
        pack();
        setLocationRelativeTo(null);
    }

    // ---- registro de ouvintes (o Controller é quem trata os eventos) ----
    public void addOuvinteJogarDados(ActionListener ouvinte) { botaoJogarDados.addActionListener(ouvinte); }
    public void addOuvinteEscolherDados(ActionListener ouvinte) { botaoEscolherDados.addActionListener(ouvinte); }
    public void addOuvinteTabuleiro(MouseListener ouvinte) { painelTabuleiro.addMouseListener(ouvinte); }

    // ---- consultas à interface ----
    public int getDadoEscolhido1() { return (Integer) comboDado1.getSelectedItem(); }
    public int getDadoEscolhido2() { return (Integer) comboDado2.getSelectedItem(); }

    /** Parada sob o ponto clicado no tabuleiro (ou null). */
    public String paradaEm(int x, int y) { return painelTabuleiro.paradaEm(x, y); }

    // ---- atualizações ----
    public void setPosicoes(Map<String, String> posicoes) { painelTabuleiro.setPosicoes(posicoes); }
    public void setJogadorDaVez(String cor) { painelDados.setJogadorDaVez(cor); }
    public void mostrarDados(int d1, int d2) { painelDados.setDados(d1, d2); }
    public void limparDados() { painelDados.setDados(0, 0); }

    public void setBotoesDadosHabilitados(boolean habilitado) {
        botaoJogarDados.setEnabled(habilitado);
        botaoEscolherDados.setEnabled(habilitado);
        comboDado1.setEnabled(habilitado);
        comboDado2.setEnabled(habilitado);
    }
}
