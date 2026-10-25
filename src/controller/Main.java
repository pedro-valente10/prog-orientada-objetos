package controller;

import javax.swing.SwingUtilities;

import model.Jogo;
import view.JanelaJogo;

/** Ponto de entrada. As janelas iniciais não fazem parte da 2ª iteração: o nº de jogadores é fixo. */
public class Main {

    private static final int NUMERO_JOGADORES = 3;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Jogo jogo = new Jogo(NUMERO_JOGADORES);
                JanelaJogo janela = new JanelaJogo(jogo);
                new Controlador(jogo, janela).iniciar();
            }
        });
    }
}
