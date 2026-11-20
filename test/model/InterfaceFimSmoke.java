package model;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import controller.Controlador;
import view.JanelaJogo;

/** Confirma que o Controller mostra a pontuacao e encerra a janela ao fim. */
public final class InterfaceFimSmoke {
    private static boolean resultadoExibido;
    private static boolean novaPartidaPerguntada;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() { executar(); }
        });
    }

    private static void executar() {
        try {
            Jogo jogo = Jogo.getInstance();
            jogo.novaPartida(2, 5L);
            Field campoPartida = Jogo.class.getDeclaredField("partida");
            campoPartida.setAccessible(true);
            Partida partida = (Partida) campoPartida.get(jogo);
            marcar(partida, "indiceVez", 1);
            marcar(partida, "fimAcionado", true);
            marcar(partida, "viradasDeRodadaAposFim", 1);
            jogo.ativarColuna(Coluna.PERSIA);
            JanelaJogo janela = new JanelaJogo(jogo) {
                private static final long serialVersionUID = 1L;
                @Override public void mostrarFimDeJogo(List<String> vencedores,
                        Map<String, Integer> votos) {
                    resultadoExibido = !vencedores.isEmpty() && !votos.isEmpty();
                }
                @Override public boolean perguntarNovaPartida() {
                    novaPartidaPerguntada = true;
                    return false;
                }
            };
            new Controlador(jogo, janela).iniciar();
            Component painel = ((BorderLayout) janela.getContentPane().getLayout())
                    .getLayoutComponent(BorderLayout.EAST);
            BufferedImage imagem = new BufferedImage(painel.getWidth(), painel.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = imagem.createGraphics();
            painel.paint(g);
            g.dispose();
            painel.dispatchEvent(new MouseEvent(painel, MouseEvent.MOUSE_CLICKED,
                    System.currentTimeMillis(), 0, 67, 291, 1, false));
            if (!jogo.isJogoEncerrado() || !resultadoExibido || !novaPartidaPerguntada)
                throw new AssertionError("Fluxo de resultado e nova partida incompleto.");
            if (janela.isDisplayable()) throw new AssertionError("Janela principal ainda aberta.");
            for (Window w : Window.getWindows()) if (w.isDisplayable()) w.dispose();
            System.out.println("OK resultado, pergunta por nova partida e fechamento");
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    private static void marcar(Partida partida, String nome, Object valor)
            throws ReflectiveOperationException {
        Field campo = Partida.class.getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(partida, valor);
    }
}
