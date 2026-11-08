package model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Fotografia imutável do conselheiro de uma parada, para a View montar a entrega de tesouros. */
public final class ConselheiroInfo {
    private final String id;
    private final int votos;
    private final int quantidadeQualquer;
    private final Map<String, Integer> exigenciaFixa;

    ConselheiroInfo(Conselheiro conselheiro) {
        id = conselheiro.getId();
        votos = conselheiro.getVotosPorEntrega();
        quantidadeQualquer = conselheiro.getQuantidadeQualquer();
        Map<TipoTesouro, Integer> fixa = conselheiro.getExigenciaFixa();
        if (fixa == null) {
            exigenciaFixa = null;
        } else {
            Map<String, Integer> copia = new LinkedHashMap<>();
            for (TipoTesouro tipo : TipoTesouro.values())
                if (fixa.containsKey(tipo)) copia.put(tipo.name(), fixa.get(tipo));
            exigenciaFixa = Collections.unmodifiableMap(copia);
        }
    }

    public String getId() { return id; }
    public int getVotos() { return votos; }
    /** true se a exigência é "n tesouros quaisquer" (o jogador escolhe quais). */
    public boolean isQuaisquer() { return exigenciaFixa == null; }
    /** Quantidade exigida quando {@link #isQuaisquer()}; 0 caso contrário. */
    public int getQuantidadeQualquer() { return quantidadeQualquer; }
    /** Exigência fixa (tipo -> quantidade) ou null quando {@link #isQuaisquer()}. */
    public Map<String, Integer> getExigenciaFixa() { return exigenciaFixa; }

    /** Texto curto para mensagens e diálogos. */
    public String descricao() {
        if (exigenciaFixa == null) return quantidadeQualquer + " tesouros quaisquer";
        StringBuilder texto = new StringBuilder();
        for (Map.Entry<String, Integer> e : exigenciaFixa.entrySet()) {
            if (texto.length() > 0) texto.append(" + ");
            texto.append(e.getValue()).append(" ").append(e.getKey());
        }
        return texto.toString();
    }
}
