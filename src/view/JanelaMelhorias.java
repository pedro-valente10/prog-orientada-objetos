package view;

import javax.swing.JDialog;
import javax.swing.JFrame;

import model.EventoJogo;
import model.Jogo;
import model.ObservadoIF;
import model.ObservadorIF;

/** Janela da oferta de melhorias. Observador do Jogo, como {@link JanelaCartela}. */
class JanelaMelhorias extends JDialog implements ObservadorIF {
    private static final long serialVersionUID = 1L;

    private final PainelMelhorias painel;

    JanelaMelhorias(JFrame dono, Jogo jogo) {
        super(dono, "Melhorias", false);
        painel = new PainelMelhorias(jogo);
        getContentPane().add(painel);
        setResizable(false);
        pack();
    }

    PainelMelhorias getPainel() { return painel; }

    @Override public void notify(ObservadoIF observado) {
        EventoJogo evento = (EventoJogo) observado.get(Jogo.INFO_EVENTO);
        if (evento == EventoJogo.MELHORIAS_EXIBIDAS) {
            setVisible(true);
            toFront();
        } else if (evento == EventoJogo.MELHORIAS_OCULTADAS || evento == EventoJogo.PARTIDA_INICIADA) {
            setVisible(false);
        }
        painel.repaint();
    }
}
