package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Cartas e peças de bônus do manual. A identidade é estável durante a partida. */
final class Cartas {
    enum Aberta {
        ESPADAS_4("Ter 4 espadas"), IURTAS_6("Ter 6 iurtas no mapa"),
        IURTAS_10("Ter 10 iurtas no mapa"), TESOUROS_4("Ter 4 tesouros"),
        TIPOS_5("Ter 5 tipos de tesouro"), UMA_CADA_REGIAO("Ter 1 iurta em cada região"),
        DUAS_CADA_REGIAO("Ter 2 iurtas em cada região"), BONUS_2("Conseguir 2 bônus"),
        BONUS_3("Conseguir 3 bônus"), CIDADES_2("Conquistar 2 cidades"),
        CIDADES_3("Conquistar 3 cidades"), LINHA("Completar 1 linha de melhorias"),
        DUAS_COLUNAS("Ter 2 colunas com 2 melhorias"), QUATRO_COLUNA("Ter 4 melhorias em 1 coluna");
        final String descricao;
        Aberta(String descricao) { this.descricao = descricao; }
        boolean cumprida(Partida partida, Jogador jogador, int bonusRecebidos) {
            Tabuleiro t = partida.getTabuleiro();
            TabuleiroJogador individual = jogador.getTabuleiroIndividual();
            switch (this) {
                case ESPADAS_4: return jogador.quantidadeTributo(TipoTributo.ESPADA) >= 4;
                case IURTAS_6: return Jogador.MAX_IURTAS - jogador.getIurtasDisponiveis() >= 6;
                case IURTAS_10: return Jogador.MAX_IURTAS - jogador.getIurtasDisponiveis() >= 10;
                case TESOUROS_4: return jogador.totalTesouros() >= 4;
                case TIPOS_5:
                    for (TipoTesouro tipo : TipoTesouro.values())
                        if (jogador.quantidadeTesouro(tipo) == 0) return false;
                    return true;
                case UMA_CADA_REGIAO: case DUAS_CADA_REGIAO:
                    int minimo = this == UMA_CADA_REGIAO ? 1 : 2;
                    for (Regiao r : Regiao.values()) if (t.contarIurtas(jogador.getCor(), r) < minimo) return false;
                    return true;
                case BONUS_2: return bonusRecebidos >= 2;
                case BONUS_3: return bonusRecebidos >= 3;
                case CIDADES_2: return cidadesConquistadas(t, jogador.getCor()) >= 2;
                case CIDADES_3: return cidadesConquistadas(t, jogador.getCor()) >= 3;
                case LINHA: return individual.temLinhaCompleta();
                case DUAS_COLUNAS:
                    int colunas = 0;
                    for (Coluna c : Coluna.values())
                        if (individual.quantidadeMelhorias(c) >= 2) colunas++;
                    return colunas >= 2;
                case QUATRO_COLUNA:
                    for (Coluna c : Coluna.values()) if (individual.quantidadeMelhorias(c) >= 4) return true;
                    return false;
                default: throw new IllegalStateException();
            }
        }
    }

    enum Secreta {
        VIAJANTE("Rede de iurtas conectadas: 3/6/8"),
        SOLDADO("Peças de melhoria: 4/7/10"),
        COLECIONADOR("Símbolos diferentes de melhoria: 4/6/7"),
        SACERDOTE("Pares iguais de melhorias: 1/2/4"),
        GOVERNADOR("Regiões com 2 iurtas e 2 melhorias: 1/2/3"),
        BAJULADOR("Províncias do Khan com iurtas adjacentes: 2/3/4"),
        PREFEITO("Cidades com iurtas adjacentes: 2/3/4"),
        CIDADAO("Cidades com iurta adjacente: 3/6/8"),
        CAVALEIRO("Melhorias de cavalo ou punho: 2/4/5"),
        MENSAGEIRO("Paradas duplas com iurta: 2/4/6");
        final String descricao;
        Secreta(String descricao) { this.descricao = descricao; }
        int votos(Partida p, Jogador j) {
            int n;
            switch (this) {
                case VIAJANTE: n = maiorRede(p.getTabuleiro(), j.getCor()); return pontos(n, 3, 6, 8);
                case SOLDADO: n = melhorias(j).size(); return pontos(n, 4, 7, 10);
                case COLECIONADOR:
                    Set<String> simbolos = new HashSet<>();
                    for (Melhoria m : melhorias(j)) simbolos.add(simbolo(m));
                    return pontos(simbolos.size(), 4, 6, 7);
                case SACERDOTE:
                    Map<String, Integer> grupos = new HashMap<>();
                    for (Melhoria m : melhorias(j)) {
                        String s = simbolo(m); grupos.put(s, grupos.getOrDefault(s, 0) + 1);
                    }
                    n = 0; for (int q : grupos.values()) n += q / 2;
                    return pontos(n, 1, 2, 4);
                case GOVERNADOR:
                    n = 0;
                    for (Regiao r : Regiao.values())
                        if (p.getTabuleiro().contarIurtas(j.getCor(), r) >= 2 && j.quantidadeMelhorias(r) >= 2) n++;
                    return pontos(n, 1, 2, 3);
                case BAJULADOR:
                    Map<String, Integer> iurtasPorProvincia = new HashMap<>();
                    for (Parada parada : p.getTabuleiro().getParadas()) {
                        int iurtas = parada.contarIurtas(j.getCor());
                        for (String id : parada.getProvinciasAdjacentes())
                            if (p.getTabuleiro().getProvincia(id).isProvinciaKhan())
                                iurtasPorProvincia.put(id, iurtasPorProvincia.getOrDefault(id, 0) + iurtas);
                    }
                    n = 0; for (int q : iurtasPorProvincia.values()) if (q >= 2) n++;
                    return pontos(n, 2, 3, 4);
                case PREFEITO: case CIDADAO:
                    n = 0;
                    for (Cidade cidade : p.getTabuleiro().getTodasCidades()) {
                        int iurtas = 0;
                        for (String id : cidade.getParadasAdjacentes())
                            iurtas += p.getTabuleiro().getParada(id).contarIurtas(j.getCor());
                        if (iurtas >= (this == PREFEITO ? 2 : 1)) n++;
                    }
                    return this == PREFEITO ? pontos(n, 2, 3, 4) : pontos(n, 3, 6, 8);
                case CAVALEIRO:
                    n = 0; for (Melhoria m : melhorias(j))
                        if (m.getTipo() == Melhoria.Tipo.CAVALOS || m.getTipo() == Melhoria.Tipo.PUNHO) n++;
                    return pontos(n, 2, 4, 5);
                case MENSAGEIRO:
                    n = 0; for (Parada parada : p.getTabuleiro().getParadas())
                        if (parada.isDupla() && parada.temIurta(j.getCor())) n++;
                    return pontos(n, 2, 4, 6);
                default: throw new IllegalStateException();
            }
        }
    }

    enum Bonus {
        VOTO("1 voto imediato"), ESPADAS("2 espadas"), MOEDAS("2 moedas"),
        IURTAS("2 tributos de iurta"), MOVIMENTOS("3 movimentos e 1 punho"),
        PUNHOS("2 punhos"), TESOUROS("Sacar 3 tesouros e escolher 1");
        final String descricao;
        Bonus(String descricao) { this.descricao = descricao; }
    }

    private final List<Aberta> abertas = new ArrayList<>();
    private final Map<CorJogador, List<Secreta>> opcoes = new EnumMap<>(CorJogador.class);
    private final Map<CorJogador, Secreta> escolhidas = new EnumMap<>(CorJogador.class);
    private final Map<CorJogador, Integer> bonusRecebidos = new EnumMap<>(CorJogador.class);
    private final Map<String, Bonus> bonusPorParada = new HashMap<>();
    private final Set<String> bonusEntregues = new HashSet<>();

    Cartas(List<CorJogador> ordem, Tabuleiro tabuleiro, long semente) {
        Random random = new Random(semente ^ 0xCA47A5L);
        List<Aberta> baralhoAberto = new ArrayList<>(Arrays.asList(Aberta.values()));
        Collections.shuffle(baralhoAberto, random);
        abertas.addAll(baralhoAberto.subList(0, 4));
        List<Secreta> baralhoSecreto = new ArrayList<>(Arrays.asList(Secreta.values()));
        Collections.shuffle(baralhoSecreto, random);
        for (int i = 0; i < ordem.size(); i++) {
            opcoes.put(ordem.get(i), Collections.unmodifiableList(
                    new ArrayList<>(baralhoSecreto.subList(i * 2, i * 2 + 2))));
            bonusRecebidos.put(ordem.get(i), 0);
        }
        List<String> paradas = new ArrayList<>();
        for (Parada parada : tabuleiro.getParadas())
            if (parada.getConselheiro() != null) paradas.add(parada.getId());
        Collections.sort(paradas);
        if (paradas.size() == 7) {
            List<Bonus> pecas = new ArrayList<>(Arrays.asList(Bonus.values()));
            Collections.shuffle(pecas, random);
            for (int i = 0; i < paradas.size(); i++) bonusPorParada.put(paradas.get(i), pecas.get(i));
        }
    }

    List<String> abertas() {
        List<String> resultado = new ArrayList<>();
        for (Aberta c : abertas) resultado.add(c.descricao);
        return Collections.unmodifiableList(resultado);
    }
    List<String> imagensAbertas() {
        List<String> resultado = new ArrayList<>();
        for (Aberta c : abertas) resultado.add(imagem(c));
        return Collections.unmodifiableList(resultado);
    }
    List<String> imagensOpcoes(CorJogador cor) {
        List<String> resultado = new ArrayList<>();
        if (!escolhidas.containsKey(cor))
            for (Secreta c : opcoes.get(cor)) resultado.add(imagem(c));
        return Collections.unmodifiableList(resultado);
    }
    String imagemEscolhida(CorJogador cor) {
        Secreta c = escolhidas.get(cor);
        return c == null ? null : imagem(c);
    }
    List<String> opcoes(CorJogador cor) {
        List<String> resultado = new ArrayList<>();
        if (!escolhidas.containsKey(cor))
            for (Secreta c : opcoes.get(cor)) resultado.add(c.descricao);
        return Collections.unmodifiableList(resultado);
    }
    String escolhida(CorJogador cor) {
        Secreta c = escolhidas.get(cor);
        return c == null ? null : c.descricao;
    }
    void escolher(CorJogador cor, int indice) {
        if (escolhidas.containsKey(cor)) throw new RegraInvalidaException("Carta secreta já escolhida.");
        if (indice < 0 || indice > 1) throw new RegraInvalidaException("Escolha uma das duas cartas.");
        escolhidas.put(cor, opcoes.get(cor).get(indice));
    }
    int votosSecretos(Partida p, CorJogador cor) {
        Secreta c = escolhidas.get(cor);
        return c == null ? 0 : c.votos(p, p.getJogador(cor));
    }
    int verificarAbertas(Partida p, Jogador jogador) {
        int recebidos = 0;
        for (int i = abertas.size() - 1; i >= 0; i--)
            if (abertas.get(i).cumprida(p, jogador, bonusRecebidos.get(jogador.getCor()))) {
                abertas.remove(i); recebidos++;
            }
        return recebidos;
    }
    Bonus receberBonus(String parada, CorJogador cor) {
        if (!bonusEntregues.add(parada)) return null;
        Bonus bonus = bonusPorParada.get(parada);
        if (bonus != null) bonusRecebidos.put(cor, bonusRecebidos.get(cor) + 1);
        return bonus;
    }
    int bonusRecebidos(CorJogador cor) { return bonusRecebidos.get(cor); }

    private static String imagem(Aberta c) {
        switch (c) {
            case ESPADAS_4: return "cartas1-1.jpg";
            case IURTAS_6: return "cartas1-2.jpg";
            case IURTAS_10: return "cartas1-3.jpg";
            case TESOUROS_4: return "cartas1-4.jpg";
            case DUAS_CADA_REGIAO: return "cartas1-5.jpg";
            case UMA_CADA_REGIAO: return "cartas1-6.jpg";
            case BONUS_2: return "cartas1-7.jpg";
            case BONUS_3: return "cartas1-8.jpg";
            case TIPOS_5: return "cartas1-9.jpg";
            case CIDADES_3: return "cartas2-1.jpg";
            case CIDADES_2: return "cartas2-2.jpg";
            case LINHA: return "cartas2-3.jpg";
            case QUATRO_COLUNA: return "cartas3-1.jpg";
            case DUAS_COLUNAS: return "cartas3-2.jpg";
            default: throw new IllegalStateException();
        }
    }
    private static String imagem(Secreta c) {
        switch (c) {
            case SACERDOTE: return "cartas4-1.jpg";
            case COLECIONADOR: return "cartas4-2.jpg";
            case VIAJANTE: return "cartas5-1.jpg";
            case SOLDADO: return "cartas5-2.jpg";
            case CIDADAO: return "cartas6-1.jpg";
            case CAVALEIRO: return "cartas6-2.jpg";
            case MENSAGEIRO: return "cartas7-1.jpg";
            case GOVERNADOR: return "cartas7-2.jpg";
            case PREFEITO: return "cartas8-1.jpg";
            case BAJULADOR: return "cartas8-2.jpg";
            default: throw new IllegalStateException();
        }
    }
    private static int pontos(int n, int a, int b, int c) {
        return n >= c ? 3 : n >= b ? 2 : n >= a ? 1 : 0;
    }
    private static int cidadesConquistadas(Tabuleiro t, CorJogador cor) {
        int n = 0; for (Cidade c : t.getCidadesReveladas()) if (c.getDono() == cor) n++;
        return n;
    }
    private static List<Melhoria> melhorias(Jogador j) {
        List<Melhoria> todas = new ArrayList<>();
        for (Coluna c : Coluna.values())
            for (Melhoria m : j.getTabuleiroIndividual().melhorias(c)) if (m != null) todas.add(m);
        return todas;
    }
    private static String simbolo(Melhoria m) {
        return m.getTipo() == Melhoria.Tipo.TESOURO ? "TESOURO:" + m.tesouro() : m.getTipo().name();
    }
    private static int maiorRede(Tabuleiro t, CorJogador cor) {
        int maior = 0; Set<String> vistos = new HashSet<>();
        for (Parada inicial : t.getParadas()) {
            if (inicial.isKarakorum() || !inicial.temIurta(cor)
                    || !vistos.add(inicial.getId())) continue;
            List<String> fila = new ArrayList<>(); fila.add(inicial.getId());
            for (int i = 0; i < fila.size(); i++)
                for (String vizinha : t.getParada(fila.get(i)).getVizinhas())
                    if (!t.getParada(vizinha).isKarakorum()
                            && t.getParada(vizinha).temIurta(cor) && vistos.add(vizinha))
                        fila.add(vizinha);
            int iurtas = 0;
            for (String id : fila) iurtas += t.getParada(id).contarIurtas(cor);
            maior = Math.max(maior, iurtas);
        }
        return maior;
    }
}
