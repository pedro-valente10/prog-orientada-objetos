package view;

import java.awt.BorderLayout;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import model.Jogo;
import model.ParadaInfo;

/** Janela única da demonstração: mapa e tabuleiro individual do jogador. */
public class JanelaJogo extends JFrame {
    private static final long serialVersionUID = 1L;

    private final PainelTabuleiro painelTabuleiro;
    private final PainelControle painelControle;

    public JanelaJogo(Jogo jogo) {
        super("Herdeiros do Khan");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        painelTabuleiro = new PainelTabuleiro(jogo.getJogadores());
        painelControle = new PainelControle(jogo);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(painelTabuleiro, BorderLayout.CENTER);
        getContentPane().add(painelControle, BorderLayout.EAST);
        pack();
        if (getWidth() > 1400 || getHeight() > 1050)
            throw new IllegalStateException("Janela acima do limite de 1400 x 1050.");
        setLocationRelativeTo(null);
    }

    public void addOuvinteTabuleiro(MouseListener ouvinte) {
        painelTabuleiro.addMouseListener(ouvinte);
    }
    public void addOuvinteControle(MouseListener ouvinte) {
        painelControle.addMouseListener(ouvinte);
    }

    public String paradaEm(int x, int y) { return painelTabuleiro.paradaEm(x, y); }
    public String acaoEm(int x, int y) { return painelControle.acaoEm(x, y); }
    public boolean isModoYurt() { return painelControle.isModoYurt(); }
    public void setModoYurt(boolean ativo) { painelControle.setModoYurt(ativo); }
    public void exibirJogador(String cor) { painelControle.exibirJogador(cor); }
    public void mostrarMensagem(String mensagem) { painelControle.setMensagem(mensagem); }

    public void atualizar(List<ParadaInfo> paradas, Set<String> alcancaveis, String vez) {
        painelTabuleiro.setEstado(paradas, alcancaveis, vez);
        painelControle.atualizar();
    }
}
