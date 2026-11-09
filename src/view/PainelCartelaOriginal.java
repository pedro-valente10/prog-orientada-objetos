package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import model.Herdeiro;
import model.Jogo;

/** Arte original da cartela do herdeiro exibido; a aba Jogo contém os controles. */
class PainelCartelaOriginal extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Jogo jogo;
    private final Map<Herdeiro, BufferedImage> imagens = new EnumMap<>(Herdeiro.class);

    PainelCartelaOriginal(Jogo jogo) {
        this.jogo = jogo;
        setPreferredSize(new Dimension(PainelCartela.LARGURA, PainelCartela.ALTURA));
        for (Herdeiro herdeiro : Herdeiro.values()) {
            try {
                BufferedImage imagem = ImageIO.read(getClass().getResourceAsStream(
                        "img/cartelas/" + herdeiro.getImagem()));
                if (imagem == null) throw new IOException("Imagem inválida.");
                imagens.put(herdeiro, imagem);
            } catch (IOException | NullPointerException e) {
                throw new IllegalStateException("Cartela original ausente: " + herdeiro.getImagem(), e);
            }
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D d = (Graphics2D) g.create();
        d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        d.setColor(new Color(31, 37, 52));
        d.fillRect(0, 0, getWidth(), getHeight());
        String cor = jogo.getCartelaExibida();
        if (cor == null) cor = jogo.getJogadorDaVez();
        Herdeiro herdeiro = jogo.getHerdeiro(cor);
        int alturaImagem = Math.min(getHeight() - 86,
                (int) Math.round(getWidth() * imagens.get(herdeiro).getHeight()
                        / (double) imagens.get(herdeiro).getWidth()));
        d.drawImage(imagens.get(herdeiro), 0, 0, getWidth(), alturaImagem, null);
        d.setColor(Color.WHITE);
        d.setFont(new Font("SansSerif", Font.BOLD, 16));
        d.drawString(herdeiro.getNome() + " — " + cor, 14, alturaImagem + 25);
        d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        d.drawString("Volte à aba Jogo para ativar uma coluna e consultar as melhorias.", 14,
                alturaImagem + 48);
        d.dispose();
    }
}
