package view;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import model.EventoJogo;
import model.Jogo;
import model.ObservadoIF;
import model.ObservadorIF;

/**
 * Janela da cartela (tabuleiro individual com as colunas). É um observador do Jogo:
 * aparece e some conforme o estado de exibição mantido pelo Model e se redesenha a cada mudança.
 */
class JanelaCartela extends JDialog implements ObservadorIF {
    private static final long serialVersionUID = 1L;

    private final PainelCartela painel;
    private final PainelCartelaOriginal original;

    JanelaCartela(JFrame dono, Jogo jogo) {
        super(dono, "Cartela", false);
        painel = new PainelCartela(jogo);
        original = new PainelCartelaOriginal(jogo);
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Jogo", painel);
        abas.addTab("Cartela original", original);
        getContentPane().add(abas);
        setResizable(false);
        pack();
    }

    PainelCartela getPainel() { return painel; }

    @Override public void notify(ObservadoIF observado) {
        EventoJogo evento = (EventoJogo) observado.get(Jogo.INFO_EVENTO);
        if (evento == EventoJogo.CARTELA_EXIBIDA) {
            setVisible(true);
            toFront();
        } else if (evento == EventoJogo.CARTELA_OCULTADA || evento == EventoJogo.PARTIDA_INICIADA) {
            setVisible(false);
        }
        painel.repaint();
        original.repaint();
    }
}
