package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.Jogo;
import model.RegraInvalidaException;
import view.JanelaJogo;

/**
 * Controller: liga a View ao Model. Os eventos são tratados por classes ouvintes
 * (sem expressões lambda).
 */
public class Controlador {

    private final Jogo jogo;
    private final JanelaJogo janela;
    private final Random sorteio = new Random();
    private boolean dadosLancados;

    public Controlador(Jogo jogo, JanelaJogo janela) {
        this.jogo = jogo;
        this.janela = janela;
        janela.addOuvinteJogarDados(new OuvinteJogarDados());
        janela.addOuvinteEscolherDados(new OuvinteEscolherDados());
        janela.addOuvinteTabuleiro(new OuvinteTabuleiro());
        atualizarVisao();
    }

    public void iniciar() {
        janela.setVisible(true);
    }

    // ---------------------------------------------------------------- ações
    /** Os dados definem quantos movimentos (cavalos) o jogador da vez tem neste turno. */
    private void lancar(int dado1, int dado2) {
        if (dadosLancados) {
            return;
        }
        jogo.iniciarTurno(dado1 + dado2, 0);
        dadosLancados = true;
        janela.mostrarDados(dado1, dado2);
        janela.setBotoesDadosHabilitados(false);
    }

    private void tentarMover(String destino) {
        if (!dadosLancados || destino == null) {
            return;
        }
        List<String> caminho = caminhoMaisCurto(jogo.getPosicao(jogo.getJogadorDaVez()), destino);
        if (caminho == null) {
            return;
        }
        try {
            jogo.mover(caminho);
        } catch (RegraInvalidaException e) {
            return; // movimento impossível: nada acontece, nenhuma mensagem, mesmo jogador
        }
        jogo.encerrarTurno();
        dadosLancados = false;
        janela.limparDados();
        janela.setBotoesDadosHabilitados(true);
        atualizarVisao();
    }

    /** Busca em largura sobre as paradas vizinhas; devolve as paradas após a origem (ou null). */
    private List<String> caminhoMaisCurto(String origem, String destino) {
        if (origem.equals(destino)) {
            return null;
        }
        Map<String, String> anterior = new HashMap<>();
        Deque<String> fila = new ArrayDeque<>();
        anterior.put(origem, null);
        fila.add(origem);
        while (!fila.isEmpty()) {
            String atual = fila.poll();
            if (atual.equals(destino)) {
                break;
            }
            for (String vizinha : jogo.getParadasVizinhas(atual)) {
                if (!anterior.containsKey(vizinha)) {
                    anterior.put(vizinha, atual);
                    fila.add(vizinha);
                }
            }
        }
        if (!anterior.containsKey(destino)) {
            return null;
        }
        LinkedList<String> caminho = new LinkedList<>();
        for (String p = destino; p != null && !p.equals(origem); p = anterior.get(p)) {
            caminho.addFirst(p);
        }
        return caminho;
    }

    private void atualizarVisao() {
        Map<String, String> posicoes = new HashMap<>();
        for (String cor : jogo.getJogadores()) {
            posicoes.put(cor, jogo.getPosicao(cor));
        }
        janela.setPosicoes(Collections.unmodifiableMap(posicoes));
        janela.setJogadorDaVez(jogo.getJogadorDaVez());
    }

    // ---------------------------------------------------------------- ouvintes
    private class OuvinteJogarDados implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            lancar(sorteio.nextInt(6) + 1, sorteio.nextInt(6) + 1);
        }
    }

    /** Permite ao testador definir os valores dos dados. */
    private class OuvinteEscolherDados implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            lancar(janela.getDadoEscolhido1(), janela.getDadoEscolhido2());
        }
    }

    private class OuvinteTabuleiro extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            tentarMover(janela.paradaEm(e.getX(), e.getY()));
        }
    }
}
