package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** Fotografia imutável de uma parada para desenho e seleção na View. */
public final class ParadaInfo {
    private final String id;
    private final double x, y;
    private final boolean dupla, karakorum;
    private final List<String> vizinhas;
    private final List<String> peoes;
    private final List<String> iurtas;

    ParadaInfo(Parada parada) {
        id = parada.getId();
        x = parada.getX();
        y = parada.getY();
        dupla = parada.isDupla();
        karakorum = parada.isKarakorum();
        vizinhas = copiarOrdenado(parada.getVizinhas());
        peoes = new ArrayList<>();
        iurtas = new ArrayList<>();
        for (CorJogador cor : parada.getPeoes()) peoes.add(cor.name());
        for (CorJogador cor : parada.getIurtas()) iurtas.add(cor.name());
    }

    private static List<String> copiarOrdenado(Set<String> originais) {
        List<String> copia = new ArrayList<>(originais);
        Collections.sort(copia);
        return Collections.unmodifiableList(copia);
    }

    public String getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isDupla() { return dupla; }
    public boolean isKarakorum() { return karakorum; }
    public List<String> getVizinhas() { return vizinhas; }
    public List<String> getPeoes() { return Collections.unmodifiableList(peoes); }
    public List<String> getIurtas() { return Collections.unmodifiableList(iurtas); }
}
