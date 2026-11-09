package model;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/** Regras das cinco cartelas originais e suas habilidades. */
public class HerdeirosTest {
    private Partida partida(long seed, CorJogador... cores) {
        return new Partida(Arrays.asList(cores), TabuleiroFactory.criarTabuleiroBasico(seed), seed, true);
    }

    @Test public void perfisDasCincoCartelas() {
        int[][][] esperado = {
            {{1,2,0,1},{2,1,0,0},{1,1,1,0},{2,0,0,1}},
            {{1,1,1,0},{1,2,0,1},{2,1,0,0},{2,0,0,1}},
            {{1,2,0,1},{1,1,1,0},{2,1,0,0},{2,0,0,1}},
            {{1,1,1,0},{2,1,0,0},{1,2,0,1},{2,0,0,1}},
            {{2,1,0,0},{1,1,1,0},{1,1,0,1},{2,0,0,1}}
        };
        Herdeiro[] herdeiros = Herdeiro.values();
        for (int h = 0; h < herdeiros.length; h++) {
            for (Coluna coluna : Coluna.values()) {
                int[] e = esperado[h][coluna.ordinal()];
                assertEquals(e[0], TabuleiroJogador.cavalosIniciais(herdeiros[h], coluna));
                assertEquals(e[1], TabuleiroJogador.punhosIniciais(herdeiros[h], coluna));
                assertEquals(e[2], TabuleiroJogador.moedasIniciais(herdeiros[h], coluna));
                assertEquals(e[3] == 1, TabuleiroJogador.khanInicial(herdeiros[h], coluna));
            }
        }
    }

    @Test public void altaniEscolheUmDosDoisPeoesPorTurno() {
        Partida p = partida(7L, CorJogador.VERDE, CorJogador.VERMELHO);
        Jogador altani = p.getJogador(CorJogador.VERDE);
        assertEquals(Arrays.asList("KARAKORUM", "KARAKORUM"), altani.getPosicoes());
        try {
            p.ativarColuna(Coluna.PERSIA);
            fail("A escolha do peão deve ser obrigatória.");
        } catch (RegraInvalidaException esperado) { }
        p.escolherPeao(1);
        p.ativarColuna(Coluna.PERSIA);
        p.moverPara("P5");
        assertEquals(Arrays.asList("KARAKORUM", "P5"), altani.getPosicoes());
        p.encerrarTurno();
        p.ativarColuna(Coluna.CHINA);
        p.encerrarTurno();
        assertTrue(p.isEscolhaPeaoPendente());
        p.escolherPeao(0);
        p.ativarColuna(Coluna.RUSSIA);
        assertEquals("KARAKORUM", altani.getPosicao());
    }

    @Test public void jochiEscolheMelhoriaInicialECompraEmQualquerColuna() {
        Partida p = partida(8L, CorJogador.AMARELO, CorJogador.VERMELHO);
        assertEquals(2, p.getOpcoesIniciaisJochi().size());
        try {
            p.ativarColuna(Coluna.RUSSIA);
            fail("A melhoria inicial deve ser escolhida.");
        } catch (RegraInvalidaException esperado) { }
        Melhoria primeira = p.getOpcoesIniciaisJochi().get(0);
        p.escolherMelhoriaInicialJochi(0);
        assertEquals(primeira, p.getJogador(CorJogador.AMARELO)
                .getTabuleiroIndividual().melhorias(Coluna.BRANCA).get(0));
        p.ativarColuna(Coluna.RUSSIA);
        Melhoria oferta = p.getArea().consultar(0);
        Coluna outra = oferta.cor() == Regiao.CHINA ? Coluna.PERSIA : Coluna.CHINA;
        p.getJogador(CorJogador.AMARELO).adicionarTributo(TipoTributo.MOEDA, 1);
        p.comprarMelhoriaJochi(0, outra);
        assertEquals(oferta, p.getJogador(CorJogador.AMARELO)
                .getTabuleiroIndividual().melhorias(outra).get(0));
        assertEquals(0, p.getJogador(CorJogador.AMARELO).quantidadeTributo(TipoTributo.MOEDA));
    }

    @Test public void chagataiTrocaUmCavaloPorEspadaPersistenteUmaVez() {
        Partida p = partida(9L, CorJogador.ROXO, CorJogador.VERMELHO);
        p.ativarColuna(Coluna.PERSIA);
        assertEquals(1, p.getMovimentosRestantes());
        p.usarHabilidadeChagatai();
        assertEquals(0, p.getMovimentosRestantes());
        assertEquals(1, p.getJogador(CorJogador.ROXO).quantidadeTributo(TipoTributo.ESPADA));
        try {
            p.usarHabilidadeChagatai();
            fail("A habilidade só pode ser usada uma vez por turno.");
        } catch (RegraInvalidaException esperado) { }
        p.encerrarTurno();
        assertEquals(1, p.getJogador(CorJogador.ROXO).quantidadeTributo(TipoTributo.ESPADA));
    }

    @Test public void toluiIniciaComIurtaECriaEmParadaAdjacenteOcupada() {
        Partida p = partida(10L, CorJogador.VERMELHO, CorJogador.AZUL);
        Jogador tolui = p.getJogador(CorJogador.VERMELHO);
        assertEquals(11, tolui.getIurtasDisponiveis());
        assertTrue(p.getTabuleiro().getParada(Tabuleiro.KARAKORUM).temIurta(CorJogador.VERMELHO));
        for (Regiao regiao : Regiao.values())
            assertEquals(0, p.getTabuleiro().contarIurtas(CorJogador.VERMELHO, regiao));
        p.ativarColuna(Coluna.CHINA);
        p.getTabuleiro().getParada("C5").adicionarPeao(CorJogador.AZUL);
        tolui.adicionarTributo(TipoTributo.IURTA, 1);
        p.construirIurta("C5");
        assertTrue(p.getTabuleiro().getParada("C5").temIurta(CorJogador.VERMELHO));
        assertEquals(10, tolui.getIurtasDisponiveis());
    }

    @Test public void ogedeiPagaMoedaRemotaEDescontaAtaqueSeguinte() {
        Partida p = partida(11L, CorJogador.AZUL, CorJogador.VERMELHO);
        Cidade cidade = p.getTabuleiro().getCidadesReveladas().iterator().next();
        Jogador ogedei = p.getJogador(CorJogador.AZUL);
        p.iniciarTurno(20, 0);
        int antes = cidade.quantidadeTesouros();
        p.usarHabilidadeOgedei(cidade.getId());
        assertEquals(antes - 1, cidade.quantidadeTesouros());
        assertEquals(0, ogedei.quantidadeTributo(TipoTributo.MOEDA));
        assertEquals(1, ogedei.totalTesouros());
        try {
            p.usarHabilidadeOgedei(cidade.getId());
            fail("A habilidade só pode ser usada uma vez por turno.");
        } catch (RegraInvalidaException esperado) { }
        String parada = cidade.getParadasAdjacentes().iterator().next();
        p.moverPara(parada);
        ogedei.adicionarTributo(TipoTributo.ESPADA, 1);
        p.atacarCidade(cidade.getId(), 1);
        assertEquals(0, ogedei.quantidadeTributo(TipoTributo.ESPADA));
        assertEquals(2, ogedei.totalTesouros());
    }
}
