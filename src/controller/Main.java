package controller;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import model.Jogo;
import view.JanelaJogo;

/** Ponto de entrada: escolhe 2 a 5 jogadores e abre a partida Swing. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                Object[] quantidades = { "2", "3", "4", "5" };
                Object escolha = JOptionPane.showInputDialog(null,
                        "Quantos jogadores participarão?", "Herdeiros do Khan",
                        JOptionPane.QUESTION_MESSAGE, null, quantidades, "3");
                if (escolha == null) return;
                Jogo jogo = Jogo.getInstance();
                jogo.novaPartida(Integer.parseInt(escolha.toString()));
                JanelaJogo janela = new JanelaJogo(jogo);
                new Controlador(jogo, janela).iniciar();
            }
        });
    }
}
