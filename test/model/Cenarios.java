package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Cenários reutilizáveis pelos testes: um tabuleiro minúsculo e fácil de raciocinar.
 *
 * <pre>
 *   KARAKORUM -- A -- B(dupla, conselheiro: 2 PELES) -- C -- D
 *   Províncias: P1(IURTA, 2 tributos) adj. a A | P2(MOEDA, 1) adj. a B | P3(ESPADA, 1) adj. a C
 *               KH1(Khan, ESPADA) -> P1,P2 | KH2(Khan, MOEDA) -> P2,P3
 *   Cidades:    X (adj. a A, 3 tesouros: PELES, FERRO, SEDA)  |  Y (adj. a C, na pilha, vazia)
 * </pre>
 */
final class Cenarios {

    private Cenarios() { }

    static Tabuleiro tabuleiroMini() {
        Tabuleiro t = new Tabuleiro();
        t.adicionarParada(new Parada(Tabuleiro.KARAKORUM, null, false, true));
        t.adicionarParada(new Parada("A", Regiao.RUSSIA, false, false));
        t.adicionarParada(new Parada("B", Regiao.RUSSIA, true, false));
        t.adicionarParada(new Parada("C", Regiao.RUSSIA, false, false));
        t.adicionarParada(new Parada("D", Regiao.RUSSIA, false, false));
        t.ligar(Tabuleiro.KARAKORUM, "A");
        t.ligar("A", "B");
        t.ligar("B", "C");
        t.ligar("C", "D");
        t.getParada("B").setConselheiro(Conselheiro.doisIguais("CONS_B", TipoTesouro.PELES));

        List<String> nenhuma = Collections.emptyList();
        Provincia p1 = new Provincia("P1", TipoTributo.IURTA, false, nenhuma);
        p1.adicionarTributo();
        p1.adicionarTributo();
        Provincia p2 = new Provincia("P2", TipoTributo.MOEDA, false, nenhuma);
        p2.adicionarTributo();
        Provincia p3 = new Provincia("P3", TipoTributo.ESPADA, false, nenhuma);
        p3.adicionarTributo();
        t.adicionarProvincia(p1);
        t.adicionarProvincia(p2);
        t.adicionarProvincia(p3);
        t.adicionarProvincia(new Provincia("KH1", TipoTributo.ESPADA, true, Arrays.asList("P1", "P2")));
        t.adicionarProvincia(new Provincia("KH2", TipoTributo.MOEDA, true, Arrays.asList("P2", "P3")));
        t.getParada("A").adicionarProvinciaAdjacente("P1");
        t.getParada("B").adicionarProvinciaAdjacente("P2");
        t.getParada("C").adicionarProvinciaAdjacente("P3");

        Cidade x = new Cidade("X", Regiao.RUSSIA, new HashSet<>(Arrays.asList("A")));
        x.abastecer(Arrays.asList(TipoTesouro.PELES, TipoTesouro.FERRO, TipoTesouro.SEDA));
        t.adicionarCidadeRevelada(x);
        t.adicionarCidadeNaPilha(new Cidade("Y", Regiao.RUSSIA, new HashSet<>(Arrays.asList("C"))));
        return t;
    }

    /** Partida com 2 a 5 jogadores (VERMELHO é o primeiro a jogar), sementes fixas. */
    static Partida partida(int numeroJogadores) {
        List<CorJogador> cores = new ArrayList<>(
                Arrays.asList(CorJogador.values()).subList(0, numeroJogadores));
        return new Partida(cores, tabuleiroMini(), 1L);
    }

    /** Teletransporta o peão de um jogador (só para montar cenários de teste). */
    static void colocarPeao(Partida partida, CorJogador cor, String paradaId) {
        Jogador j = partida.getJogador(cor);
        partida.getTabuleiro().getParada(j.getPosicao()).removerPeao(cor);
        partida.getTabuleiro().getParada(paradaId).adicionarPeao(cor);
        j.setPosicao(paradaId);
    }
}
