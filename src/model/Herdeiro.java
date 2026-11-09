package model;

/** Cartelas e habilidades impressas dos cinco herdeiros. */
public enum Herdeiro {
    ALTANI("Altani", CorJogador.VERDE, "cartela1.jpg",
            new int[][] {{1, 2, 0, 1}, {2, 1, 0, 0}, {1, 1, 1, 0}, {2, 0, 0, 1}}),
    JOCHI("Jochi", CorJogador.AMARELO, "cartela2.jpg",
            new int[][] {{1, 1, 1, 0}, {1, 2, 0, 1}, {2, 1, 0, 0}, {2, 0, 0, 1}}),
    CHAGATAI("Chagatai", CorJogador.ROXO, "cartela3.jpg",
            new int[][] {{1, 2, 0, 1}, {1, 1, 1, 0}, {2, 1, 0, 0}, {2, 0, 0, 1}}),
    TOLUI("Tolui", CorJogador.VERMELHO, "cartela4.jpg",
            new int[][] {{1, 1, 1, 0}, {2, 1, 0, 0}, {1, 2, 0, 1}, {2, 0, 0, 1}}),
    OGEDEI("Ogedei", CorJogador.AZUL, "cartela5.jpg",
            new int[][] {{2, 1, 0, 0}, {1, 1, 1, 0}, {1, 1, 0, 1}, {2, 0, 0, 1}});

    private final String nome;
    private final CorJogador cor;
    private final String imagem;
    private final int[][] acoes;

    Herdeiro(String nome, CorJogador cor, String imagem, int[][] acoes) {
        this.nome = nome;
        this.cor = cor;
        this.imagem = imagem;
        this.acoes = acoes;
    }

    public String getNome() { return nome; }
    @Override public String toString() { return nome + " (" + cor + ")"; }
    public String getCor() { return cor.name(); }
    public String getImagem() { return imagem; }
    CorJogador cor() { return cor; }
    int cavalos(Coluna coluna) { return acoes[coluna.ordinal()][0]; }
    int punhos(Coluna coluna) { return acoes[coluna.ordinal()][1]; }
    int moedas(Coluna coluna) { return acoes[coluna.ordinal()][2]; }
    boolean khan(Coluna coluna) { return acoes[coluna.ordinal()][3] == 1; }

    static Herdeiro daCor(CorJogador cor) {
        for (Herdeiro h : values()) if (h.cor == cor) return h;
        throw new IllegalArgumentException("Cor sem herdeiro: " + cor);
    }
}
