package controller;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.Herdeiro;
import model.Jogo;
import model.Melhoria;
import view.JanelaJogo;

/** Ponto de entrada: nova partida ou recuperacao de um arquivo salvo. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                Jogo jogo = Jogo.getInstance();
                if (!abrirPartidaInicial(jogo)) return;
                JanelaJogo janela = new JanelaJogo(jogo);
                new Controlador(jogo, janela).iniciar();
            }
        });
    }

    private static boolean abrirPartidaInicial(Jogo jogo) {
        Object[] opcoes = { "Nova partida", "Recuperar partida", "Sair" };
        while (true) {
            int escolha = JOptionPane.showOptionDialog(null,
                    "Como deseja começar?", "Herdeiros do Khan",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, opcoes, opcoes[0]);
            if (escolha == 0) return escolherNovaPartida(jogo, null);
            if (escolha != 1) return false;
            JFileChooser seletor = new JFileChooser();
            seletor.setFileFilter(new FileNameExtensionFilter("Partidas em texto (*.txt)", "txt"));
            seletor.setAcceptAllFileFilterUsed(false);
            if (seletor.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) continue;
            try {
                jogo.recuperar(seletor.getSelectedFile().toPath());
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Não foi possível recuperar",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Reabre a escolha de jogadores e herdeiros apos uma partida terminada. */
    static boolean escolherNovaPartida(Jogo jogo, Component dono) {
        Object[] quantidades = { "2", "3", "4", "5" };
        Object escolha = JOptionPane.showInputDialog(dono,
                "Quantos jogadores participarão?", "Herdeiros do Khan",
                JOptionPane.QUESTION_MESSAGE, null, quantidades, "3");
        if (escolha == null) return false;
        int numero = Integer.parseInt(escolha.toString());
        List<Herdeiro> restantes = new ArrayList<>(Arrays.asList(Herdeiro.values()));
        List<Herdeiro> escolhidos = new ArrayList<>();
        List<String> nomes = new ArrayList<>();
        for (int i = 0; i < numero; i++) {
            Herdeiro herdeiro = (Herdeiro) JOptionPane.showInputDialog(dono,
                    "Escolha o herdeiro do jogador " + (i + 1) + ":", "Nova partida",
                    JOptionPane.QUESTION_MESSAGE, null, restantes.toArray(), restantes.get(0));
            if (herdeiro == null) return false;
            escolhidos.add(herdeiro);
            String nome = JOptionPane.showInputDialog(dono,
                    "Nome do jogador " + (i + 1) + " (" + herdeiro.getNome() + "): ",
                    "Jogador " + (i + 1));
            if (nome == null) return false;
            nomes.add(nome.trim().isEmpty() ? "Jogador " + (i + 1) : nome.trim());
            restantes.remove(herdeiro);
        }
        jogo.novaPartidaComHerdeiros(escolhidos);
        for (int i = 0; i < escolhidos.size(); i++)
            jogo.definirNomeJogador(escolhidos.get(i).getCor(), nomes.get(i));
        List<Melhoria> opcoesJochi = jogo.getOpcoesIniciaisJochi();
        if (!opcoesJochi.isEmpty()) {
            Object[] opcoes = { opcoesJochi.get(0).toString(), opcoesJochi.get(1).toString() };
            int indice = JOptionPane.showOptionDialog(dono,
                    "Jochi: escolha uma melhoria inicial para a coluna branca.",
                    "Habilidade de Jochi", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
            if (indice < 0) return false;
            jogo.escolherMelhoriaInicialJochi(indice);
        }
        return true;
    }
}
