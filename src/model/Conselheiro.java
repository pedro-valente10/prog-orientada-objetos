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
            exigencia.put(t, exigencia.getOrDefault(t, 0) + 1);
        }
        return new Conselheiro(id, exigencia, 0, 2);
    }

    String getId() { return id; }
    CorJogador getPrimeiroEntregador() { return primeiroEntregador; }

    /** Compatibilidade com chamadas internas e cenários antigos. */
    int entregar(Jogador jogador, Map<TipoTesouro, Integer> oferta) {
        return entregar(jogador, oferta, new HashMap<TipoTesouro, Integer>());
    }

    /** Paga com tesouros reais e, se disponíveis, virtuais da coluna ativada. */
    int entregar(Jogador jogador, Map<TipoTesouro, Integer> oferta,
                 Map<TipoTesouro, Integer> virtuais) {
        if (jogador == null) {
            throw new RegraInvalidaException("O jogador é obrigatório.");
        }
        if (virtuais == null) throw new RegraInvalidaException("Recursos virtuais inválidos.");
        if (oferta != null) {
            for (Map.Entry<TipoTesouro, Integer> entrada : oferta.entrySet()) {
                if (entrada.getKey() == null || entrada.getValue() == null || entrada.getValue() < 0)
                    throw new RegraInvalidaException("A oferta contém um tesouro ou quantidade inválida.");
            }
        }
        Map<TipoTesouro, Integer> pagamento;
        if (exigenciaFixa != null) {
            if (oferta != null && !oferta.isEmpty() && !oferta.equals(exigenciaFixa))
                throw new RegraInvalidaException("A oferta não atende à exigência fixa.");
            pagamento = exigenciaFixa;
        } else {
            pagamento = (oferta == null) ? new HashMap<TipoTesouro, Integer>() : oferta;
            int total = 0;
            for (Map.Entry<TipoTesouro, Integer> entrada : pagamento.entrySet()) {
                total += entrada.getValue();
            }
            if (total != quantidadeQualquer) {
                throw new RegraInvalidaException(
                        "Este conselheiro exige exatamente " + quantidadeQualquer + " tesouros.");
            }
        }
        Map<TipoTesouro, Integer> pagamentoReal = new HashMap<>();
        Map<TipoTesouro, Integer> usadosVirtuais = new HashMap<>();
        for (Map.Entry<TipoTesouro, Integer> entrada : pagamento.entrySet()) {
            TipoTesouro tipo = entrada.getKey();
            int virtual = Math.min(entrada.getValue(), virtuais.getOrDefault(tipo, 0));
            usadosVirtuais.put(tipo, virtual);
            pagamentoReal.put(tipo, entrada.getValue() - virtual);
        }
        if (!jogador.possuiTesouros(pagamentoReal)) {
            throw new RegraInvalidaException("O jogador não possui os tesouros exigidos.");
        }
        jogador.removerTesouros(pagamentoReal);
        for (Map.Entry<TipoTesouro, Integer> entrada : usadosVirtuais.entrySet())
            virtuais.put(entrada.getKey(), virtuais.getOrDefault(entrada.getKey(), 0) - entrada.getValue());
        if (primeiroEntregador == null) {
            primeiroEntregador = jogador.getCor();
        }
        return votosPorEntrega;
    }

    Map<TipoTesouro, Integer> getExigenciaFixa() {
        return exigenciaFixa == null ? null : Collections.unmodifiableMap(exigenciaFixa);
    }
}
