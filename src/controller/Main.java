package controller;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.Herdeiro;
import model.Melhoria;

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
                int numero = Integer.parseInt(escolha.toString());
                List<Herdeiro> restantes = new ArrayList<>(Arrays.asList(Herdeiro.values()));
                List<Herdeiro> escolhidos = new ArrayList<>();
                for (int i = 0; i < numero; i++) {
                    Herdeiro herdeiro = (Herdeiro) JOptionPane.showInputDialog(null,
                            "Escolha o herdeiro do jogador " + (i + 1) + ":", "Nova partida",
                            JOptionPane.QUESTION_MESSAGE, null, restantes.toArray(), restantes.get(0));
                    if (herdeiro == null) return;
                    escolhidos.add(herdeiro);
                    restantes.remove(herdeiro);
                }
                Jogo jogo = Jogo.getInstance();
                jogo.novaPartidaComHerdeiros(escolhidos);
                List<Melhoria> opcoesJochi = jogo.getOpcoesIniciaisJochi();
                if (!opcoesJochi.isEmpty()) {
                    Object[] opcoes = { opcoesJochi.get(0).toString(), opcoesJochi.get(1).toString() };
                    int indice = JOptionPane.showOptionDialog(null,
                            "Jochi: escolha uma melhoria inicial para a coluna branca.",
                            "Habilidade de Jochi", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
                    if (indice < 0) return;
                    jogo.escolherMelhoriaInicialJochi(indice);
                }
                JanelaJogo janela = new JanelaJogo(jogo);
                new Controlador(jogo, janela).iniciar();
            }
        });
    }
}
