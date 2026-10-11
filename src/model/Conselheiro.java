package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Conselheiro: recebe entregas de tesouros em troca de votos no Kurultai.
 * Há três formas de exigência:
 *  - 2 tesouros iguais de um tipo (1 voto);
 *  - 3 tesouros quaisquer (1 voto);
 *  - 3 tesouros específicos, como em Karakorum (2 votos).
 */
class Conselheiro {

    private final String id;
    /** Exigência fixa (tipo -> quantidade). Se null, vale "qualquer combinação". */
    private final Map<TipoTesouro, Integer> exigenciaFixa;
    private final int quantidadeQualquer;
    private final int votosPorEntrega;
    private CorJogador primeiroEntregador;

    private Conselheiro(String id, Map<TipoTesouro, Integer> exigenciaFixa,
                        int quantidadeQualquer, int votosPorEntrega) {
        this.id = id;
        this.exigenciaFixa = exigenciaFixa;
        this.quantidadeQualquer = quantidadeQualquer;
        this.votosPorEntrega = votosPorEntrega;
    }

    static Conselheiro doisIguais(String id, TipoTesouro tipo) {
        Map<TipoTesouro, Integer> exigencia = new HashMap<>();
        exigencia.put(tipo, 2);
        return new Conselheiro(id, exigencia, 0, 1);
    }

    static Conselheiro tresQuaisquer(String id) {
        return new Conselheiro(id, null, 3, 1);
    }

    static Conselheiro tresEspecificos(String id, TipoTesouro a, TipoTesouro b, TipoTesouro c) {
        Map<TipoTesouro, Integer> exigencia = new HashMap<>();
        for (TipoTesouro t : new TipoTesouro[] { a, b, c }) {
            exigencia.merge(t, 1, Integer::sum);
        }
        return new Conselheiro(id, exigencia, 0, 2);
    }

    String getId() { return id; }
    CorJogador getPrimeiroEntregador() { return primeiroEntregador; }

    /**
     * Realiza uma entrega. Para exigência fixa, "oferta" é ignorada; para
     * "3 quaisquer", "oferta" diz quais tesouros o jogador entrega.
     *
     * @return número de votos ganhos
     */
    int entregar(Jogador jogador, Map<TipoTesouro, Integer> oferta) {
        Map<TipoTesouro, Integer> pagamento;
        if (exigenciaFixa != null) {
            pagamento = exigenciaFixa;
        } else {
            pagamento = (oferta == null) ? new HashMap<TipoTesouro, Integer>() : oferta;
            int total = 0;
            for (int q : pagamento.values()) {
                total += q;
            }
            if (total != quantidadeQualquer) {
                throw new RegraInvalidaException(
                        "Este conselheiro exige exatamente " + quantidadeQualquer + " tesouros.");
            }
        }
        if (!jogador.possuiTesouros(pagamento)) {
            throw new RegraInvalidaException("O jogador não possui os tesouros exigidos.");
        }
        jogador.removerTesouros(pagamento);
        if (primeiroEntregador == null) {
            primeiroEntregador = jogador.getCor();
        }
        return votosPorEntrega;
    }

    Map<TipoTesouro, Integer> getExigenciaFixa() {
        return exigenciaFixa == null ? null : Collections.unmodifiableMap(exigenciaFixa);
    }
}
