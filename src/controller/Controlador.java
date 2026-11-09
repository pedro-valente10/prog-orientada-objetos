package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import model.Coluna;
import model.ConselheiroInfo;
import model.Jogo;
import model.Melhoria;
import model.ParadaInfo;
import model.RegraInvalidaException;
import view.JanelaJogo;

/**
 * Controller: organiza a sequência de eventos de uma partida (ativar coluna, mover, agir,
 * encerrar turno, fim de jogo) traduzindo cliques em comandos para a fachada {@link Jogo}.
 *
 * Não desenha nada: a View se atualiza sozinha porque é observadora do Jogo (padrão Observer).
 * O Controller só guarda o que é seu, o modo de clique em andamento, e controla quando a
 * partida avança de fase.
 */
public class Controlador {

    /** O que o próximo clique em uma parada do mapa significa. */
    private enum Modo { MOVER, YURT, ENTREGAR, OGEDEI }

    private final Jogo jogo;
    private final JanelaJogo janela;
    private Modo modo = Modo.MOVER;
    private boolean fimAcionadoAvisado;
    private boolean fimDeJogoExibido;

    public Controlador(Jogo jogo, JanelaJogo janela) {
        this.jogo = jogo;
        this.janela = janela;
        janela.addOuvinteTabuleiro(new OuvinteTabuleiro());
        janela.addOuvinteControle(new OuvinteControle());
        janela.addOuvinteCartela(new OuvinteCartela());
        janela.addOuvinteMelhorias(new OuvinteMelhorias());
        janela.addOuvinteCartas(new OuvinteCartas());
        janela.addOuvinteFechamentoCartela(new FechamentoCartela());
        janela.addOuvinteFechamentoMelhorias(new FechamentoMelhorias());
        janela.addOuvinteFechamentoCartas(new FechamentoCartas());
    }

    /** Abre a janela principal e a cartela do primeiro jogador: a partida começa aqui. */
    public void iniciar() {
        janela.setVisible(true);
        janela.posicionarJanelas();
        jogo.exibirCartela(jogo.getJogadorDaVez());
        jogo.exibirCartas();
        janela.mostrarMensagem("Vez de " + jogo.getJogadorDaVez() + ". " + proximoPasso());
    }

    // ------------------------------------------------------------------ ações dos botões
    private void executarAcao(String acao) {
        if (acao == null) return;
        String[] partes = acao.split(":", 2);
        String operacao = partes[0];
        String argumento = partes.length > 1 ? partes[1] : "";
        try {
            if ("VER".equals(operacao)) {
                jogo.exibirCartela(argumento);
            } else if ("CARTELA".equals(operacao)) {
                String cor = jogo.getCartelaExibida();
                jogo.exibirCartela(cor == null ? jogo.getJogadorDaVez() : cor);
            } else if ("MELHORIAS".equals(operacao)) {
                jogo.exibirMelhorias();
            } else if ("CARTAS".equals(operacao)) {
                jogo.exibirCartas();
            } else if ("SECRETA".equals(operacao)) {
                jogo.escolherCartaSecreta(Integer.parseInt(argumento));
                jogo.ocultarCartas();
                concluir("Carta secreta escolhida.");
            } else if ("BONUS".equals(operacao)) {
                resolverBonus();
                concluir("Bônus de tesouro resolvido.");
            } else if ("HABILIDADE".equals(operacao)) {
                model.Herdeiro herdeiro = jogo.getHerdeiro(jogo.getJogadorDaVez());
                if (herdeiro == model.Herdeiro.CHAGATAI) {
                    jogo.usarHabilidadeChagatai();
                    concluir("Chagatai trocou 1 movimento por 1 espada.");
                } else if (herdeiro == model.Herdeiro.OGEDEI) {
                    alternarModo(Modo.OGEDEI, "Clique em Pegar na cidade desejada (mínimo de 2 tesouros). ");
                }
            } else if ("COLUNA".equals(operacao)) {
                if (jogo.getCartaSecreta(jogo.getJogadorDaVez()) == null)
                    throw new RegraInvalidaException("Escolha sua carta secreta na janela Cartas.");
                if (jogo.isEscolhaPeaoPendente()) {
                    int peao = janela.pedirPeao(jogo.getPosicoesPeoes(jogo.getJogadorDaVez()));
                    if (peao < 0) return;
                    jogo.escolherPeao(peao);
                }
                jogo.ativarColuna(Coluna.valueOf(argumento));
                jogo.ocultarCartela();
                definirModo(Modo.MOVER);
                concluir("Coluna " + argumento + " ativada.");
            } else if ("FIM".equals(operacao)) {
                jogo.encerrarTurno();
                definirModo(Modo.MOVER);
                concluir("Turno encerrado. Vez de " + jogo.getJogadorDaVez() + ".");
            } else if ("YURT".equals(operacao)) {
                alternarModo(Modo.YURT, "Clique em uma parada visitada para construir a yurt.");
            } else if ("ENTREGAR".equals(operacao)) {
                alternarModo(Modo.ENTREGAR, "Clique em uma parada visitada que tenha conselheiro.");
            } else if ("MELHORIA".equals(operacao)) {
                comprarOuReceberMelhoria(Integer.parseInt(argumento));
            } else if ("KHAN".equals(operacao)) {
                jogo.posicionarKhan(argumento);
                concluir("Khan colocado em " + argumento + ".");
            } else if ("COLETA".equals(operacao)) {
                jogo.coletarTributo(argumento);
                concluir("Tributo coletado em " + argumento + ".");
            } else if ("ATAQUE".equals(operacao)) {
                if (modo == Modo.OGEDEI) {
                    jogo.usarHabilidadeOgedei(argumento);
                    definirModo(Modo.MOVER);
                    concluir("Ogedei tomou um tesouro de " + argumento + " por 1 moeda.");
                } else {
                    jogo.atacarCidade(argumento, 1);
                    concluir("Um tesouro retirado de " + argumento + ".");
                }
            }
        } catch (RegraInvalidaException e) {
            janela.mostrarMensagem(e.getMessage());
        }
    }

    private void comprarOuReceberMelhoria(int vaga) {
        Melhoria peca;
        String mensagem;
        if (jogo.getBonusBrancosPendentes(jogo.getJogadorDaVez()) > 0) {
            peca = jogo.receberBonusBranco(vaga);
            mensagem = "Melhoria branca gratuita: " + peca + ".";
        } else if (jogo.getHerdeiro(jogo.getJogadorDaVez()) == model.Herdeiro.JOCHI) {
            java.util.List<Coluna> disponiveis = new java.util.ArrayList<>();
            for (Coluna coluna : Coluna.values())
                if (jogo.getMelhorias(jogo.getJogadorDaVez(), coluna).contains(null))
                    disponiveis.add(coluna);
            Coluna destino = janela.pedirColunaMelhoria(disponiveis);
            if (destino == null) return;
            peca = jogo.comprarMelhoriaJochi(vaga, destino);
            mensagem = "Melhoria colocada em " + destino + ": " + peca + ".";
        } else {
            peca = jogo.comprarMelhoria(vaga);
            mensagem = "Melhoria comprada: " + peca + ".";
        }
        jogo.ocultarMelhorias();
        concluir(mensagem);
    }

    // ------------------------------------------------------------------ cliques no mapa
    private void executarCliqueNoMapa(String destino) {
        if (destino == null || !jogo.isTurnoAtivo()) return;
        try {
            if (modo == Modo.YURT) {
                jogo.construirIurta(destino);
                definirModo(Modo.MOVER);
                concluir("Yurt construída em " + destino + ".");
            } else if (modo == Modo.ENTREGAR) {
                entregarTesouros(destino);
            } else if (modo == Modo.OGEDEI) {
                return; // a habilidade escolhe uma cidade pelos botões do painel
            } else {
                jogo.moverPara(destino);
                concluir("Peão movido para " + destino + ".");
            }
        } catch (RegraInvalidaException e) {
            // O enunciado da 2ª iteração pede que um clique inválido de movimento não faça nada;
            // nos modos Yurt e Entregar o jogador pediu algo explícito, então ele recebe o motivo.
            if (modo != Modo.MOVER) janela.mostrarMensagem(e.getMessage());
        }
    }

    private void entregarTesouros(String paradaId) {
        ConselheiroInfo conselheiro = conselheiroDe(paradaId);
        if (conselheiro == null) throw new RegraInvalidaException("Não há conselheiro em " + paradaId + ".");
        Map<String, Integer> oferta = janela.pedirEntrega(paradaId, conselheiro, jogo.getTiposTesouro());
        if (oferta == null) return; // o jogador desistiu
        String cor = jogo.getJogadorDaVez();
        int antes = jogo.getVotos(cor);
        jogo.entregarTesouros(paradaId, oferta);
        definirModo(Modo.MOVER);
        String bonus = jogo.getUltimoBonus();
        concluir("Tesouros entregues em " + paradaId + ": +" + (jogo.getVotos(cor) - antes)
                + " voto(s)." + (bonus == null ? "" : " Bônus: " + bonus + "."));
    }

    private ConselheiroInfo conselheiroDe(String paradaId) {
        for (ParadaInfo parada : jogo.getParadas())
            if (parada.getId().equals(paradaId)) return parada.getConselheiro();
        return null;
    }

    // ------------------------------------------------------------------ sequência da partida
    private void alternarModo(Modo desejado, String instrucao) {
        if (!jogo.isTurnoAtivo()) throw new RegraInvalidaException("Ative uma coluna primeiro.");
        if (modo == desejado) {
            definirModo(Modo.MOVER);
            janela.mostrarMensagem("Modo desligado.");
        } else {
            definirModo(desejado);
            janela.mostrarMensagem(instrucao);
        }
    }

    private void definirModo(Modo novo) {
        modo = novo;
        janela.setModo(novo == Modo.MOVER ? null : novo.name());
    }

    /**
     * Fecha uma ação bem-sucedida: mostra a mensagem com a orientação do próximo passo e avança
     * as fases da partida (bônus branco pendente, aviso de fim acionado, fim de jogo).
     */
    private void concluir(String mensagem) {
        if (!jogo.getOpcoesTesouroBonus().isEmpty()) resolverBonus();
        if (!jogo.isTurnoAtivo() && !jogo.isJogoEncerrado()
                && jogo.getCartaSecreta(jogo.getJogadorDaVez()) == null) jogo.exibirCartas();
        String fase = avancarFase();
        janela.mostrarMensagem(mensagem + fase + " " + proximoPasso());
        if (jogo.isJogoEncerrado() && !fimDeJogoExibido) {
            fimDeJogoExibido = true;
            janela.mostrarFimDeJogo(jogo.getVencedores(), jogo.getVotosFinais());
        }
    }

    private void resolverBonus() {
        java.util.List<String> opcoes = jogo.getOpcoesTesouroBonus();
        if (opcoes.isEmpty()) return;
        int escolha = janela.pedirTesouroBonus(opcoes);
        if (escolha >= 0) jogo.escolherTesouroBonus(escolha);
    }

    private String avancarFase() {
        String aviso = "";
        if (jogo.getBonusBrancosPendentes(jogo.getJogadorDaVez()) > 0) {
            jogo.exibirMelhorias(); // a escolha da peça gratuita é feita na janela de melhorias
            aviso += " Linha completa: escolha uma melhoria gratuita para a coluna branca.";
        }
        if (jogo.isFimAcionado() && !fimAcionadoAvisado && !jogo.isJogoEncerrado()) {
            fimAcionadoAvisado = true;
            aviso += " O fim de jogo foi acionado: termina a rodada e mais uma.";
        }
        return aviso;
    }

    private String proximoPasso() {
        if (jogo.isJogoEncerrado()) return "Partida encerrada.";
        String vez = jogo.getJogadorDaVez();
        if (!jogo.getOpcoesTesouroBonus().isEmpty()) return "Escolha o tesouro do bônus.";
        if (!jogo.isTurnoAtivo()) {
            if (jogo.getCartaSecreta(vez) == null) return "Escolha sua carta secreta na janela Cartas.";
            return jogo.getBonusBrancosPendentes(vez) > 0
                    ? "Escolha a melhoria branca pendente."
                    : "Ative uma coluna na cartela.";
        }
        if (jogo.isKhanPendente()) return "Use o Khan, que é obrigatório nesta coluna.";
        return "Mova, aja ou encerre o turno.";
    }

    // ------------------------------------------------------------------ ouvintes
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

    private class OuvinteCartela extends MouseAdapter {
        @Override public void mouseClicked(MouseEvent e) {
            executarAcao(janela.acaoNaCartela(e.getX(), e.getY()));
        }
    }

    private class OuvinteMelhorias extends MouseAdapter {
        @Override public void mouseClicked(MouseEvent e) {
            executarAcao(janela.acaoNasMelhorias(e.getX(), e.getY()));
        }
    }

    private class OuvinteCartas extends MouseAdapter {
        @Override public void mouseClicked(MouseEvent e) {
            executarAcao(janela.acaoNasCartas(e.getX(), e.getY()));
        }
    }

    /** Fechar a janela pelo botão da barra de título também é uma mudança de estado do Model. */
    private class FechamentoCartela extends WindowAdapter {
        @Override public void windowClosing(WindowEvent e) { jogo.ocultarCartela(); }
    }

    private class FechamentoMelhorias extends WindowAdapter {
        @Override public void windowClosing(WindowEvent e) { jogo.ocultarMelhorias(); }
    }

    private class FechamentoCartas extends WindowAdapter {
        @Override public void windowClosing(WindowEvent e) { jogo.ocultarCartas(); }
    }
}
