package model;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import controller.Controlador;
import view.JanelaJogo;

/** Percurso reproduzível de cliques no Swing; os recursos iniciais são preparados só para o teste. */
public final class InterfaceSmoke {
    private static JanelaJogo janela;
    private static Jogo jogo;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() { executar(); }
        });
    }

    private static void executar() {
        jogo = Jogo.getInstance(); jogo.novaPartida(3, 7L);
        // Cenário de integração: uma melhoria de punho e recursos para exercitar todas as ações.
        Partida p = partida();
        Jogador j = p.getJogador(CorJogador.VERMELHO);
        j.getTabuleiroIndividual().colocar(Coluna.RUSSIA,
                new Melhoria(100, Regiao.RUSSIA, Melhoria.Tipo.CAVALOS, null));
        j.adicionarTributo(TipoTributo.MOEDA, 1);
        j.adicionarTributo(TipoTributo.IURTA, 1);
        j.adicionarTributo(TipoTributo.ESPADA, 7);
        janela = new JanelaJogo(jogo) {
            private static final long serialVersionUID = 1L;
            @Override public Map<String,Integer> pedirEntrega(String id, ConselheiroInfo conselheiro,
                                                                List<String> tipos) {
                Map<String,Integer> oferta = new HashMap<>();
                int restante = conselheiro.getQuantidadeQualquer();
                for (String tipo : tipos) {
                    int n = Math.min(restante, jogo.getTesouros(jogo.getJogadorDaVez(), tipo));
                    if (n > 0) oferta.put(tipo,n);
                    restante -= n;
                }
                if (restante != 0) throw new AssertionError("Tesouros insuficientes para o teste");
                return oferta;
            }
            @Override public int pedirTesouroBonus(List<String> opcoes) { return 0; }
            @Override public void mostrarFimDeJogo(List<String> v, Map<String,Integer> pontos) { }
        };
        new Controlador(jogo, janela).iniciar();
        System.out.println("janela principal=" + janela.getBounds());
        Rectangle tela = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        exigir(tela.contains(janela.getBounds()), "janela principal dentro da tela");
        for (String titulo : new String[] { "Cartela", "Cartas", "Melhorias" })
            exigir(tela.contains(janelaDialogo(titulo).getBounds()), titulo + " dentro da tela");
        Rectangle controleNaTela = new Rectangle(janela.getX()+controle().getX(),
                janela.getY()+controle().getY(), controle().getWidth(), controle().getHeight());
        exigir(!controleNaTela.intersects(janelaDialogo("Cartas").getBounds()),
                "cartas não encobrem o painel de controle");
        salvarTela();
        fecharDialogo("Cartas");
        exigir(!jogo.isCartasExibidas(), "fechar cartas pela barra de título");
        clicar(controle(), 297, 528);
        exigir(jogo.isCartasExibidas(), "reabrir cartas pelo painel");
        clicar(dialogo("Cartas"), 105, 330);
        exigir(jogo.getCartaSecreta("VERMELHO") != null, "carta secreta");
        fecharDialogo("Cartela");
        exigir(jogo.getCartelaExibida() == null, "fechar cartela pela barra de título");
        clicar(controle(), 67, 528);
        exigir(jogo.getCartelaExibida() != null, "reabrir cartela pelo painel");
        clicar(dialogo("Cartela"), 297, 61);
        exigir(jogo.isTurnoAtivo() && jogo.isKhanPendente(), "ativar coluna RUSSIA");
        for (ParadaInfo parada : jogo.getParadas()) {
            int x = (int) Math.round(parada.getX() * mapa().getWidth());
            int y = (int) Math.round(parada.getY() * mapa().getHeight());
            exigir(parada.getId().equals(janela.paradaEm(x, y)), "parada acessível " + parada.getId());
        }
        clicarParada("C9");
        exigir("C9".equals(jogo.getPosicao("VERMELHO")), "mover para C9");
        clicar(controle(), 412, 430); // coletar P4 (espada)
        exigir(jogo.getTributos("VERMELHO", "ESPADA") == 8, "coletar tributo");
        clicar(controle(), 182, 332); // Khan K2
        exigir(!jogo.isKhanPendente(), "Khan obrigatório");
        clicar(controle(), 182, 528); // abrir melhorias
        clicar(dialogo("Melhorias"), 57, 60); // comprar a primeira peça
        exigir(jogo.getTributos("VERMELHO", "MOEDA") == 0, "comprar melhoria");
        exigir(!jogo.isMelhoriasExibidas(), "melhorias fecham após compra");
        clicar(controle(), 182, 528);
        fecharDialogo("Melhorias");
        exigir(!jogo.isMelhoriasExibidas(), "fechar melhorias pela barra de título");
        clicar(controle(), 182, 283); // modo Yurt
        clicarParada("C9");
        exigir(jogo.getIurtasDisponiveis("VERMELHO") == 10, "construir iurta");
        exigir(jogo.getCidadesReveladas().contains("CANTAO"), "cidade inicial revelada");
        for (int i = 0; i < 4; i++) {
            clicar(controle(), 67, 479);
            exigir(jogo.getTributos("VERMELHO", "ESPADA") == 8 - (i == 0 ? 1 : 1 + 2*i),
                    "custo do ataque " + (i+1));
        }
        exigir(!jogo.getCidadesReveladas().contains("CANTAO"), "conquista da cidade");
        verificarBotoesCidades();
        clicar(controle(), 297, 283); // modo Entregar
        clicarParada("C9");
        exigir(jogo.getVotos("VERMELHO") >= 1, "entrega e votos");
        clicar(controle(), 67, 283); // encerrar
        exigir("AZUL".equals(jogo.getJogadorDaVez()), "encerrar turno");
        System.out.println("OK: carta, coluna, movimento, tributo, Khan, melhoria, iurta, "
                + "4 ataques (1+2+2+2 espadas), conquista, entrega, votos e fim do turno");
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
    private static JDialog janelaDialogo(String titulo) {
        for (Window w : Window.getWindows())
            if (w instanceof JDialog && titulo.equals(((JDialog) w).getTitle()) && w.getOwner() == janela)
                return (JDialog) w;
        throw new AssertionError("Janela não encontrada: " + titulo);
    }
    private static Component dialogo(String titulo) {
        Component componente = janelaDialogo(titulo).getContentPane().getComponent(0);
        return componente instanceof JTabbedPane
                ? ((JTabbedPane) componente).getSelectedComponent() : componente;
    }
    private static void fecharDialogo(String titulo) {
        JDialog d = janelaDialogo(titulo);
        d.dispatchEvent(new WindowEvent(d, WindowEvent.WINDOW_CLOSING));
    }
    private static void verificarBotoesCidades() {
        Component c = controle();
        BufferedImage imagem = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics(); c.paint(g); g.dispose();
        int i=0;
        for (String cidade : jogo.getCidadesReveladas()) {
            int x=14+i*115+53;
            exigir(x < c.getWidth() && ("ATAQUE:"+cidade).equals(janela.acaoEm(x,479)),
                    "botão visível para " + cidade);
            i++;
        }
    }
    private static void salvarTela() {
        try {
            Component c = janela.getContentPane();
            BufferedImage imagem = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = imagem.createGraphics(); c.paint(g); g.dispose();
            File diretorio = new File("build/screenshots");
            if (!diretorio.exists() && !diretorio.mkdirs())
                throw new AssertionError("Não foi possível criar build/screenshots");
            ImageIO.write(imagem, "png", new File(diretorio, "painel-principal.png"));
            Component cartas = dialogo("Cartas");
            BufferedImage cartasImagem = new BufferedImage(cartas.getWidth(), cartas.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D cg = cartasImagem.createGraphics(); cartas.paint(cg); cg.dispose();
            ImageIO.write(cartasImagem, "png", new File(diretorio, "cartas.png"));
            JTabbedPane abas = (JTabbedPane) janelaDialogo("Cartela")
                    .getContentPane().getComponent(0);
            abas.setSelectedIndex(1);
            Component original = abas.getSelectedComponent();
            BufferedImage originalImagem = new BufferedImage(original.getWidth(), original.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D og = originalImagem.createGraphics(); original.paint(og); og.dispose();
            ImageIO.write(originalImagem, "png", new File(diretorio, "cartela-original.png"));
            abas.setSelectedIndex(0);
        } catch (java.io.IOException e) { throw new AssertionError(e); }
    }
    private static void clicarParada(String id) {
        for (ParadaInfo parada : jogo.getParadas()) if (id.equals(parada.getId())) {
            clicar(mapa(), (int) Math.round(parada.getX() * mapa().getWidth()),
                    (int) Math.round(parada.getY() * mapa().getHeight()));
            return;
        }
        throw new AssertionError("Parada não encontrada: " + id);
    }
    private static void clicar(Component componente, int x, int y) {
        BufferedImage imagem = new BufferedImage(componente.getWidth(), componente.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics(); componente.paint(g); g.dispose();
        componente.dispatchEvent(new MouseEvent(componente, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, x, y, 1, false));
    }
    private static void exigir(boolean condicao, String etapa) {
        if (!condicao) throw new AssertionError("Falha: " + etapa);
        System.out.println("OK " + etapa);
    }
}
