package model;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import controller.Controlador;
import view.JanelaJogo;

/** Exercita pela interface os comandos próprios das cinco cartelas. */
public final class InterfaceHerdeirosSmoke {
    private static Jogo jogo;
    private static JanelaJogo janela;
    private static String caso;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Informe o herdeiro.");
        caso = args[0];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() { executar(); }
        });
    }

    private static void executar() {
        Herdeiro herdeiro = Herdeiro.valueOf(caso);
        jogo = Jogo.getInstance();
        jogo.novaPartidaComHerdeiros(Arrays.asList(herdeiro,
                herdeiro == Herdeiro.TOLUI ? Herdeiro.OGEDEI : Herdeiro.TOLUI));
        if (herdeiro == Herdeiro.JOCHI) jogo.escolherMelhoriaInicialJochi(0);
        janela = new JanelaJogo(jogo) {
            private static final long serialVersionUID = 1L;
            @Override public int pedirPeao(List<String> posicoes) { return 1; }
            @Override public Coluna pedirColunaMelhoria(List<Coluna> disponiveis) {
                return Coluna.BRANCA;
            }
        };
        new Controlador(jogo, janela).iniciar();
        clicar(dialogo("Cartas"), 105, 330);
        if (jogo.getCartaSecreta(jogo.getJogadorDaVez()) == null)
            throw new AssertionError("Carta secreta não escolhida.");
        if (herdeiro == Herdeiro.JOCHI) {
            clicar(dialogo("Cartela"), 67, 61); // China: moeda virtual
            if (!jogo.isTurnoAtivo()) throw new AssertionError("Coluna de Jochi");
            clicar(controle(), 182, 528); // Melhorias
            clicar(dialogo("Melhorias"), 57, 60); // escolha de coluna pela View
            if (jogo.getMelhorias("AMARELO", Coluna.BRANCA).get(1) == null)
                throw new AssertionError("Compra livre na coluna branca.");
        } else if (herdeiro == Herdeiro.OGEDEI) {
            clicar(dialogo("Cartela"), 182, 61); // Persia: moeda virtual
            int antes = jogo.getTesourosDaCidade(jogo.getCidadesReveladas().get(0));
            clicar(controle(), 412, 528); // habilidade
            clicar(controle(), 67, 479); // cidade
            if (!jogo.isHabilidadeUsadaNoTurno() || jogo.getTesouros("AZUL", jogo.getTiposTesouro().get(0))
                    + jogo.getTesouros("AZUL", jogo.getTiposTesouro().get(1))
                    + jogo.getTesouros("AZUL", jogo.getTiposTesouro().get(2))
                    + jogo.getTesouros("AZUL", jogo.getTiposTesouro().get(3))
                    + jogo.getTesouros("AZUL", jogo.getTiposTesouro().get(4)) != 1)
                throw new AssertionError("Tesouro remoto de Ogedei.");
            if (jogo.getTesourosDaCidade(jogo.getCidadesReveladas().get(0)) != antes - 1)
                throw new AssertionError("Estoque da cidade.");
        } else if (herdeiro == Herdeiro.ALTANI) {
            clicar(dialogo("Cartela"), 182, 61); // Persia
            clicarParada("P5");
            if (!jogo.getPosicoesPeoes("VERDE").equals(Arrays.asList("KARAKORUM", "P5")))
                throw new AssertionError("Escolha e movimento do segundo peão.");
        } else if (herdeiro == Herdeiro.CHAGATAI) {
            clicar(dialogo("Cartela"), 182, 61); // Persia
            clicar(controle(), 412, 528);
            if (jogo.getTributos("ROXO", "ESPADA") != 1 || jogo.getMovimentosRestantes() != 0)
                throw new AssertionError("Troca de cavalo por espada.");
        } else if (herdeiro == Herdeiro.TOLUI) {
            partida().getJogador(CorJogador.VERMELHO).adicionarTributo(TipoTributo.IURTA, 1);
            clicar(dialogo("Cartela"), 67, 61); // China
            clicar(controle(), 182, 283); // yurt
            clicarParada("C5");
            ParadaInfo c5 = null;
            for (ParadaInfo parada : jogo.getParadas())
                if ("C5".equals(parada.getId())) c5 = parada;
            if (c5 == null || !c5.getIurtas().contains("VERMELHO"))
                throw new AssertionError("Iurta adjacente de Tolui.");
        }
        System.out.println("OK interface " + caso);
        janela.dispose();
        for (Window w : Window.getWindows()) if (w.isDisplayable()) w.dispose();
    }

    private static Partida partida() {
        try {
            java.lang.reflect.Field f = Jogo.class.getDeclaredField("partida");
            f.setAccessible(true); return (Partida) f.get(jogo);
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    private static Component controle() {
        return ((BorderLayout) janela.getContentPane().getLayout()).getLayoutComponent(BorderLayout.EAST);
    }
    private static Component mapa() {
        return ((BorderLayout) janela.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
    }
    private static Component dialogo(String titulo) {
        for (Window w : Window.getWindows())
            if (w instanceof JDialog && titulo.equals(((JDialog) w).getTitle()) && w.getOwner() == janela) {
                Component componente = ((JDialog) w).getContentPane().getComponent(0);
                return componente instanceof JTabbedPane
                        ? ((JTabbedPane) componente).getSelectedComponent() : componente;
            }
        throw new AssertionError("Janela ausente: " + titulo);
    }
    private static void clicarParada(String id) {
        for (ParadaInfo parada : jogo.getParadas()) if (id.equals(parada.getId())) {
            clicar(mapa(), (int) Math.round(parada.getX() * mapa().getWidth()),
                    (int) Math.round(parada.getY() * mapa().getHeight()));
            return;
        }
        throw new AssertionError("Parada ausente: " + id);
    }
    private static void clicar(Component componente, int x, int y) {
        BufferedImage imagem = new BufferedImage(componente.getWidth(), componente.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics(); componente.paint(g); g.dispose();
        componente.dispatchEvent(new MouseEvent(componente, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, x, y, 1, false));
    }
}
