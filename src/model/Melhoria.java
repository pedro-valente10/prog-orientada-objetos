package model;

/** Peça física de melhoria. A identidade permanece a mesma na pilha, oferta e tabuleiro. */
public final class Melhoria {
    public enum Tipo { MOEDA, ESPADA, YURT, TESOURO, PUNHO, CAVALOS }

    private final int id;
    private final Regiao cor;
    private final Tipo tipo;
    private final TipoTesouro tesouro;

    Melhoria(int id, Regiao cor, Tipo tipo, TipoTesouro tesouro) {
        this.id = id;
        this.cor = cor;
        this.tipo = tipo;
        this.tesouro = tesouro;
    }

    public int getId() { return id; }
    public String getCor() { return cor.name(); }
    public Tipo getTipo() { return tipo; }
    public String getTesouro() { return tesouro == null ? null : tesouro.name(); }

    Regiao cor() { return cor; }
    TipoTesouro tesouro() { return tesouro; }

    @Override public String toString() {
        return "#" + id + " " + tipo + (tesouro == null ? "" : " " + tesouro) + " (" + cor + ")";
    }
}
