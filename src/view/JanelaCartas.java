package view;

import javax.swing.JDialog;
import javax.swing.JFrame;
import model.EventoJogo;
import model.Jogo;
import model.ObservadoIF;
import model.ObservadorIF;

/** Janela de cartas atualizada pelas notificações do Jogo. */
class JanelaCartas extends JDialog implements ObservadorIF {
    private static final long serialVersionUID = 1L;
    private final PainelCartas painel;

    JanelaCartas(JFrame dono, Jogo jogo) {
        super(dono, "Cartas", false);
        painel = new PainelCartas(jogo);
        getContentPane().add(painel);
        setResizable(false);
        pack();
    }
    PainelCartas getPainel() { return painel; }
    @Override public void notify(ObservadoIF observado) {
        EventoJogo evento = (EventoJogo) observado.get(Jogo.INFO_EVENTO);
        if (evento == EventoJogo.CARTAS_EXIBIDAS) { setVisible(true); toFront(); }
        else if (evento == EventoJogo.CARTAS_OCULTADAS || evento == EventoJogo.PARTIDA_INICIADA)
            setVisible(false);
        painel.repaint();
    }
}
