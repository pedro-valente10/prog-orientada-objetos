package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/** Grafo das paradas circulares visíveis na fotografia usada pela interface. */
class TabuleiroFactory {
    private TabuleiroFactory() { }

    static String idParada(Regiao regiao, int numero) {
        String prefixo = regiao == Regiao.RUSSIA ? "R" :
                regiao == Regiao.PERSIA ? "P" : "C";
        return prefixo + numero;
    }

    static Tabuleiro criarTabuleiroBasico() { return criarTabuleiroBasico(1L); }

    static Tabuleiro criarTabuleiroBasico(long semente) {
        Tabuleiro t = new Tabuleiro();
        t.adicionarParada(new Parada(Tabuleiro.KARAKORUM, null, false, true,
                425d / 850d, 425d / 854d));

        // Coordenadas no painel de 850 x 854, obtidas dos círculos impressos.
        // O terceiro número marca os seis pares de paradas com conselheiro.
        int[][] russia = {
            { 145, 70, 0 }, { 35, 159, 0 }, { 140, 158, 0 }, { 255, 60, 1 },
            { 252, 158, 0 }, { 372, 154, 0 }, { 320, 243, 0 },
            { 515, 66, 0 }, { 458, 187, 0 }, { 559, 175, 0 }, { 660, 145, 1 }
        };
        int[][] persia = {
            { 135, 264, 1 }, { 245, 350, 0 }, { 165, 414, 0 },
            { 280, 445, 0 }, { 375, 520, 0 }, { 65, 440, 0 },
            { 135, 477, 0 }, { 52, 553, 0 }, { 210, 545, 1 },
            { 430, 595, 0 }, { 358, 653, 0 }
        };
        int[][] china = {
            { 730, 190, 0 }, { 815, 258, 0 }, { 735, 316, 0 },
            { 630, 365, 0 }, { 555, 465, 0 }, { 636, 492, 0 },
            { 748, 455, 1 }, { 750, 598, 0 }, { 530, 646, 1 },
            { 656, 646, 0 }, { 760, 715, 0 }
        };
        adicionarParadas(t, Regiao.RUSSIA, russia);
        adicionarParadas(t, Regiao.PERSIA, persia);
        adicionarParadas(t, Regiao.CHINA, china);

        // Kiev e Moscou.
        ligar(t, "R1", "R2", "R2", "R3", "R3", "R1",
                "R1", "R4", "R3", "R5", "R4", "R6",
                "R5", "R6", "R6", "R7", "R7", "R5");
        // Sarai e a ligação norte com a capital.
        ligar(t, "R6", "R9", "R8", "R9", "R9", "R10",
                "R10", "R8", "R10", "R11",
                "R11", Tabuleiro.KARAKORUM, "R9", Tabuleiro.KARAKORUM);
        // Pérsia: rotas ao redor de Samarcanda, Bagdá e Cabul.
        ligar(t, "R3", "P1", "R7", "P2",
                "P1", "P2", "P1", "P3", "P2", "P3", "P2", "P4",
                "P3", "P4", "P3", "P6", "P4", "P5", "P4", "P7",
                "P6", "P7", "P6", "P8", "P7", "P8", "P7", "P9",
                "P8", "P9", "P9", "P5", "P9", "P10",
                "P5", "P10", "P10", "P11", "P11", "P5",
                "P5", Tabuleiro.KARAKORUM);
        // China: Kaesong, Pequim e Cantão.
        ligar(t, "R11", "C1", "C1", "C2", "C2", "C3", "C3", "C1",
                "C3", "C4", "C4", "C5", "C4", "C6", "C4", "C7",
                "C5", "C6", "C5", "C9", "C5", Tabuleiro.KARAKORUM,
                "C6", "C7", "C6", "C10", "C7", "C8",
                "C8", "C10", "C8", "C11", "C9", "C10",
                "C10", "C11", "C9", "P10", "P11", "C9");

        adicionarProvincias(t);
        adicionarConselheiros(t);
        adicionarCidades(t, semente);
        return t;
    }

    private static void adicionarParadas(Tabuleiro t, Regiao regiao, int[][] pontos) {
        for (int i = 0; i < pontos.length; i++) {
            int[] ponto = pontos[i];
            t.adicionarParada(new Parada(idParada(regiao, i + 1), regiao,
                    ponto[2] == 1, false, ponto[0] / 850d, ponto[1] / 854d));
        }
    }

    private static void ligar(Tabuleiro t, String... pares) {
        for (int i = 0; i < pares.length; i += 2) t.ligar(pares[i], pares[i + 1]);
    }

    private static void adicionarProvincias(Tabuleiro t) {
        String[] ids = { "K1", "P1", "K2", "P2", "K3", "P3", "K4", "P4" };
        TipoTributo[] tipos = TipoTributo.values();
        for (int i = 0; i < ids.length; i++) {
            List<String> adj = Arrays.asList(ids[(i + 1) % ids.length],
                    ids[(i + ids.length - 1) % ids.length]);
            Provincia p = new Provincia(ids[i], tipos[i % tipos.length],
                    ids[i].startsWith("K"), adj);
            p.adicionarTributo();
            t.adicionarProvincia(p);
        }
        String[][] porRegiao = {
            { "K1", "P1", "K2", "P2" },
            { "K4", "P4", "K2", "P2" },
            { "K3", "P3", "K1", "P1" }
        };
        for (Parada parada : t.getParadas()) {
            if (parada.isKarakorum()) continue;
            String[] proximas = porRegiao[parada.getRegiao().ordinal()];
            int numero = Integer.parseInt(parada.getId().substring(1)) - 1;
            parada.adicionarProvinciaAdjacente(proximas[numero % 4]);
            parada.adicionarProvinciaAdjacente(proximas[(numero + 1) % 4]);
        }
    }

    private static void adicionarConselheiros(Tabuleiro t) {
        String[] duplas = { "R4", "R11", "P1", "P9", "C7", "C9" };
        TipoTesouro[] tipos = TipoTesouro.values();
        for (int i = 0; i < duplas.length; i++) {
            Parada parada = t.getParada(duplas[i]);
            parada.setConselheiro(i < tipos.length
                    ? Conselheiro.doisIguais("CONS_" + duplas[i], tipos[i])
                    : Conselheiro.tresQuaisquer("CONS_" + duplas[i]));
        }
        t.getParada(Tabuleiro.KARAKORUM)
                .setConselheiro(Conselheiro.tresEspecificos("CONS_KARAKORUM",
                        tipos[0], tipos[1], tipos[2]));
    }

    private static void adicionarCidades(Tabuleiro t, long semente) {
        List<Cidade> cidades = new ArrayList<>();
        cidades.add(cidade("KIEV", Regiao.RUSSIA, "R1", "R2", "R3"));
        cidades.add(cidade("MOSCOU", Regiao.RUSSIA, "R5", "R6", "R7"));
        cidades.add(cidade("SARAI", Regiao.RUSSIA, "R8", "R9", "R10"));
        cidades.add(cidade("SAMARCANDA", Regiao.PERSIA, "P2", "P3", "P4"));
        cidades.add(cidade("BAGDA", Regiao.PERSIA, "P6", "P7", "P8"));
        cidades.add(cidade("CABUL", Regiao.PERSIA, "P5", "P10", "P11"));
        cidades.add(cidade("KAESONG", Regiao.CHINA, "C1", "C2", "C3"));
        cidades.add(cidade("PEQUIM", Regiao.CHINA, "C4", "C5", "C6"));
        cidades.add(cidade("CANTAO", Regiao.CHINA, "C8", "C9", "C10", "C11"));
        Collections.shuffle(cidades, new Random(semente));
        for (int i = 0; i < cidades.size(); i++) {
            if (i < 3) t.adicionarCidadeRevelada(cidades.get(i));
            else t.adicionarCidadeNaPilha(cidades.get(i));
        }
    }

    private static Cidade cidade(String id, Regiao regiao, String... paradas) {
        return new Cidade(id, regiao, new HashSet<>(Arrays.asList(paradas)));
    }
}
