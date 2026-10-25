package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import model.Coluna;
import model.Jogo;
import model.Melhoria;
import model.ParadaInfo;
import model.RegraInvalidaException;
import view.JanelaJogo;

/** Traduz cliques em comandos do Model e atualiza ambas as áreas da View. */
public class Controlador {
    private final Jogo jogo;
    private final JanelaJogo janela;

    public Controlador(Jogo jogo, JanelaJogo janela) {
        this.jogo = jogo;
        this.janela = janela;
        janela.addOuvinteTabuleiro(new OuvinteTabuleiro());
        janela.addOuvinteControle(new OuvinteControle());
        atualizarVisao();
    }

    public void iniciar() { janela.setVisible(true); }

    private void executarAcao(String acao) {
        if (acao == null) return;
        if (acao.startsWith("VER:")) {
            janela.exibirJogador(acao.substring(4));
            return;
        }
        String[] partes = acao.split(":", 2);
        String operacao = partes[0];
        String argumento = partes.length > 1 ? partes[1] : "";
        try {
            String mensagem;
            if ("COLUNA".equals(operacao)) {
                jogo.ativarColuna(Coluna.valueOf(argumento));
                mensagem = "Coluna " + argumento + " ativada.";
            } else if ("FIM".equals(operacao)) {
                jogo.encerrarTurno();
                janela.setModoYurt(false);
                mensagem = "Turno encerrado. Vez de " + jogo.getJogadorDaVez() + ".";
            } else if ("YURT".equals(operacao)) {
                if (!jogo.isTurnoAtivo())
                    throw new RegraInvalidaException("Ative uma coluna primeiro.");
                janela.setModoYurt(!janela.isModoYurt());
                mensagem = janela.isModoYurt() ? "Clique uma parada visitada para construir yurt."
                        : "Modo yurt desligado.";
            } else if ("MELHORIA".equals(operacao)) {
                int vaga = Integer.parseInt(argumento);
                Melhoria peca;
                if (jogo.getBonusBrancosPendentes(jogo.getJogadorDaVez()) > 0) {
                    peca = jogo.receberBonusBranco(vaga);
                    mensagem = "Melhoria branca gratuita: " + peca + ".";
                } else {
                    peca = jogo.comprarMelhoria(vaga);
                    mensagem = "Melhoria comprada: " + peca + ".";
                }
            } else if ("KHAN".equals(operacao)) {
                jogo.posicionarKhan(argumento);
                mensagem = "Khan colocado em " + argumento + ".";
            } else if ("COLETA".equals(operacao)) {
                jogo.coletarTributo(argumento);
                mensagem = "Tributo coletado em " + argumento + ".";
            } else if ("ATAQUE".equals(operacao)) {
                jogo.atacarCidade(argumento, 1);
                mensagem = "Um tesouro retirado de " + argumento + ".";
            } else {
                return;
            }
            atualizarVisao();
            janela.mostrarMensagem(mensagem);
        } catch (RegraInvalidaException e) {
            janela.mostrarMensagem(e.getMessage());
            atualizarVisao();
        }
    }

    private void executarCliqueNoMapa(String destino) {
        if (destino == null || !jogo.isTurnoAtivo()) return;
        try {
            if (janela.isModoYurt()) {
                jogo.construirIurta(destino);
                janela.setModoYurt(false);
                atualizarVisao();
                janela.mostrarMensagem("Yurt construída em " + destino + ".");
            } else {
                jogo.moverPara(destino);
                atualizarVisao();
                janela.mostrarMensagem("Peão movido para " + destino + ".");
            }
        } catch (RegraInvalidaException e) {
            // O enunciado da 2ª iteração pede que um clique inválido não faça nada.
        }
    }

    private void atualizarVisao() {
        Set<String> alcancaveis = new HashSet<>();
        if (jogo.isTurnoAtivo()) {
            for (ParadaInfo parada : jogo.getParadas()) {
                try {
                    jogo.getCaminhoMaisBarato(parada.getId());
                    alcancaveis.add(parada.getId());
                } catch (RegraInvalidaException ignorada) {
                    // destino fora do orçamento ou sem vaga
                }
            }
        }
        janela.atualizar(jogo.getParadas(), alcancaveis, jogo.getJogadorDaVez());
        if (jogo.isJogoEncerrado()) janela.mostrarMensagem("Partida encerrada.");
    }

    private class OuvinteTabuleiro extends MouseAdapter {
        @Override public void mouseClicked(MouseEvent e) {
            executarCliqueNoMapa(janela.paradaEm(e.getX(), e.getY()));
        }
    }

    private class OuvinteControle extends MouseAdapter {
        @Override public void mouseClicked(MouseEvent e) {
            executarAcao(janela.acaoEm(e.getX(), e.getY()));
        }
    }
}
