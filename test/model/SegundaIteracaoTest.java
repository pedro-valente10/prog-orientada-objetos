package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/** Regressões das colunas, melhorias, oferta e escolha de percurso. */
public class SegundaIteracaoTest {

    @Test public void movimentoNaoEncerraTurnoEKhanObrigatorioImpedePassagem() {
        Partida partida = Cenarios.partida(2);
        partida.ativarColuna(Coluna.CHINA);
        assertEquals(2, partida.getMovimentosRestantes());
        partida.mover(Arrays.asList("A"));
        assertEquals(1, partida.getMovimentosRestantes());
        assertEquals(CorJogador.VERMELHO, partida.jogadorDaVez());
        try {
            partida.encerrarTurno();
            fail("Khan obrigatório");
        } catch (RegraInvalidaException esperado) {
            assertTrue(partida.isTurnoAtivo());
            assertEquals(CorJogador.VERMELHO, partida.jogadorDaVez());
        }
        partida.mover(Arrays.asList("B"));
        partida.posicionarKhan("KH1");
        partida.encerrarTurno();
        assertEquals(CorJogador.AZUL, partida.jogadorDaVez());
    }

    @Test public void colunaBrancaRecuperaFichasECompraSoValeDepois() {
        TabuleiroJogador individual = new TabuleiroJogador();
        TabuleiroJogador.Acoes primeira = individual.ativar(Coluna.PERSIA);
        assertEquals(2, primeira.cavalos);
        assertFalse(individual.disponivel(Coluna.PERSIA));
        individual.colocar(Coluna.PERSIA,
                new Melhoria(1, Regiao.PERSIA, Melhoria.Tipo.CAVALOS, null));
        assertEquals("A compra não altera ações já calculadas", 2, primeira.cavalos);
        individual.ativar(Coluna.BRANCA);
        assertTrue(individual.disponivel(Coluna.PERSIA));
        assertEquals(4, individual.ativar(Coluna.PERSIA).cavalos);
    }

    @Test public void ofertaDeslizaERepõeSomenteComKhan() {
        Partida partida = Cenarios.partida(2);
        partida.getJogador(CorJogador.VERMELHO).adicionarTributo(TipoTributo.MOEDA, 4);
        partida.iniciarTurno(0, 0);
        AreaMelhorias area = partida.getArea();
        List<Melhoria> antes = area.oferta();
        Melhoria comprada = partida.comprarMelhoria(2);
        assertEquals(antes.get(2), comprada);
        assertEquals(null, area.oferta().get(0));
        assertEquals(antes.get(0), area.oferta().get(1));
        assertEquals(antes.get(1), area.oferta().get(2));
        assertEquals(antes.get(3), area.oferta().get(3));
        assertEquals(antes.get(4), area.oferta().get(4));
        assertEquals(3, partida.getJogador(CorJogador.VERMELHO).quantidadeTributo(TipoTributo.MOEDA));
        assertEquals(comprada,
                partida.getJogador(CorJogador.VERMELHO).getTabuleiroIndividual()
                        .melhorias(Coluna.valueOf(comprada.getCor())).get(0));
        partida.posicionarKhan(Khan.AREA_MELHORIAS);
        assertNotNull(area.oferta().get(0));
    }

    @Test public void corErradaEQuintaPecaNaoCobramNemRemovemOferta() {
        Partida partida = Cenarios.partida(2);
        Jogador jogador = partida.getJogador(CorJogador.VERMELHO);
        jogador.adicionarTributo(TipoTributo.MOEDA, 20);
        partida.iniciarTurno(0, 0);
        Melhoria peca = partida.getArea().consultar(0);
        Regiao errada = peca.cor() == Regiao.CHINA ? Regiao.PERSIA : Regiao.CHINA;
        int moedas = jogador.quantidadeTributo(TipoTributo.MOEDA);
        try {
            partida.comprarMelhoria(errada, 0);
            fail("Cor incorreta");
        } catch (RegraInvalidaException esperado) {
            assertEquals(moedas, jogador.quantidadeTributo(TipoTributo.MOEDA));
            assertEquals(peca, partida.getArea().consultar(0));
        }
        TabuleiroJogador individual = jogador.getTabuleiroIndividual();
        Coluna coluna = Coluna.valueOf(peca.getCor());
        for (int i = 0; i < 4; i++)
            individual.colocar(coluna, new Melhoria(100 + i, peca.cor(), Melhoria.Tipo.PUNHO, null));
        try {
            partida.comprarMelhoria(0);
            fail("Quinta peça");
        } catch (RegraInvalidaException esperado) {
            assertEquals(moedas, jogador.quantidadeTributo(TipoTributo.MOEDA));
            assertEquals(peca, partida.getArea().consultar(0));
        }
    }

    @Test public void linhaCompletaConcedeUmaPecaBrancaGratuita() {
        Partida partida = Cenarios.partida(2);
        partida.iniciarTurno(0, 0);
        TabuleiroJogador individual = partida.getJogador(CorJogador.VERMELHO)
                .getTabuleiroIndividual();
        for (Regiao cor : Regiao.values())
            individual.colocar(Coluna.valueOf(cor.name()),
                    new Melhoria(cor.ordinal() + 1, cor, Melhoria.Tipo.PUNHO, null));
        assertEquals(1, individual.bonusBrancosPendentes());
        try {
            partida.encerrarTurno();
            fail("Bônus pendente");
        } catch (RegraInvalidaException esperado) {
            assertTrue(partida.isTurnoAtivo());
        }
        int moedas = partida.getJogador(CorJogador.VERMELHO)
                .quantidadeTributo(TipoTributo.MOEDA);
        Melhoria bonus = partida.receberBonusBranco(0);
        assertEquals(bonus, individual.melhorias(Coluna.BRANCA).get(0));
        assertEquals(0, individual.bonusBrancosPendentes());
        assertEquals(moedas, partida.getJogador(CorJogador.VERMELHO)
                .quantidadeTributo(TipoTributo.MOEDA));
    }

    @Test public void rotaMaisBarataPodeTerMaisArestas() {
        Tabuleiro t = new Tabuleiro();
        t.adicionarParada(new Parada(Tabuleiro.KARAKORUM, null, false, true));
        for (String id : Arrays.asList("A", "B", "C", "D"))
            t.adicionarParada(new Parada(id, Regiao.RUSSIA, false, false));
        t.ligar(Tabuleiro.KARAKORUM, "A");
        t.ligar("A", "D");
        t.ligar(Tabuleiro.KARAKORUM, "B");
        t.ligar("B", "C");
        t.ligar("C", "D");
        Partida partida = new Partida(Arrays.asList(CorJogador.VERMELHO, CorJogador.AZUL), t, 1L);
        t.getParada("B").adicionarIurta(CorJogador.VERMELHO);
        t.getParada("C").adicionarIurta(CorJogador.VERMELHO);
        partida.iniciarTurno(1, 0);
        assertEquals(Arrays.asList("B", "C", "D"), partida.caminhoMaisBarato("D"));
        partida.moverPara("D");
        assertEquals(0, partida.getMovimentosRestantes());
        assertEquals("D", partida.getJogador(CorJogador.VERMELHO).getPosicao());
    }

    @Test public void moedaVirtualReduzPrecoSomenteNaColunaAtivada() {
        Partida partida = Cenarios.partida(2);
        Jogador jogador = partida.getJogador(CorJogador.VERMELHO);
        jogador.getTabuleiroIndividual().colocar(Coluna.PERSIA,
                new Melhoria(99, Regiao.PERSIA, Melhoria.Tipo.MOEDA, null));
        partida.ativarColuna(Coluna.PERSIA);
        assertEquals(1, partida.getMoedasVirtuais());
        Melhoria comprada = partida.comprarMelhoria(0);
        assertNotNull(comprada);
        assertEquals(0, partida.getMoedasVirtuais());
        assertEquals(0, jogador.quantidadeTributo(TipoTributo.MOEDA));
    }

    @Test public void acaoInvalidaNaoGastaMovimentoPunhoNemFicha() {
        Partida partida = Cenarios.partida(2);
        partida.ativarColuna(Coluna.PERSIA);
        try {
            partida.mover(Arrays.asList("C"));
            fail("Rota sem ligação direta");
        } catch (RegraInvalidaException esperado) {
            assertEquals(2, partida.getMovimentosRestantes());
            assertEquals(Tabuleiro.KARAKORUM,
                    partida.getJogador(CorJogador.VERMELHO).getPosicao());
        }
        try {
            partida.coletarTributo("P2");
            fail("Província fora das paradas visitadas");
        } catch (RegraInvalidaException esperado) {
            assertEquals(1, partida.getPunhosRestantes());
            assertEquals(1, partida.getTabuleiro().getProvincia("P2").getTributos());
        }
        partida.encerrarTurno();
        assertEquals(0, partida.getMovimentosRestantes());
        assertEquals(0, partida.getPunhosRestantes());
        partida.ativarColuna(Coluna.PERSIA);
        partida.encerrarTurno();
        try {
            partida.ativarColuna(Coluna.PERSIA);
            fail("Ficha usada pelo primeiro jogador");
        } catch (RegraInvalidaException esperado) {
            assertEquals(CorJogador.VERMELHO, partida.jogadorDaVez());
            assertFalse(partida.isTurnoAtivo());
        }
    }

    @Test public void melhoriaVirtualDeEspadaEIurtaSoValeNaColunaAtivada() {
        Partida partida = Cenarios.partida(2);
        Jogador jogador = partida.getJogador(CorJogador.VERMELHO);
        TabuleiroJogador individual = jogador.getTabuleiroIndividual();
        individual.colocar(Coluna.PERSIA,
                new Melhoria(101, Regiao.PERSIA, Melhoria.Tipo.ESPADA, null));
        individual.colocar(Coluna.PERSIA,
                new Melhoria(102, Regiao.PERSIA, Melhoria.Tipo.YURT, null));
        partida.ativarColuna(Coluna.PERSIA);
        partida.mover(Arrays.asList("A"));
        partida.construirIurta("A");
        partida.atacarCidade("X", 1);
        assertEquals(0, partida.getYurtsVirtuais());
        assertEquals(0, partida.getEspadasVirtuais());
        assertEquals(0, jogador.quantidadeTributo(TipoTributo.IURTA));
        assertEquals(0, jogador.quantidadeTributo(TipoTributo.ESPADA));
        assertEquals(1, jogador.quantidadeTesouro(TipoTesouro.PELES));
    }

    @Test public void mapaPublicoTemPontosLigadosESimetricos() {
        Jogo jogo = new Jogo(3, 2L);
        List<ParadaInfo> paradas = jogo.getParadas();
        assertEquals(34, paradas.size());
        Set<String> ids = new HashSet<>();
        for (ParadaInfo parada : paradas) {
            ids.add(parada.getId());
            assertTrue(parada.getX() >= 0 && parada.getX() <= 1);
            assertTrue(parada.getY() >= 0 && parada.getY() <= 1);
        }
        for (ParadaInfo parada : paradas) {
            for (String vizinha : parada.getVizinhas()) {
                assertTrue(ids.contains(vizinha));
                assertTrue(jogo.getParadasVizinhas(vizinha).contains(parada.getId()));
            }
        }
        Set<String> vistos = new HashSet<>();
        Deque<String> fila = new ArrayDeque<>();
        vistos.add(Tabuleiro.KARAKORUM);
        fila.add(Tabuleiro.KARAKORUM);
        while (!fila.isEmpty())
            for (String vizinha : jogo.getParadasVizinhas(fila.removeFirst()))
                if (vistos.add(vizinha)) fila.addLast(vizinha);
        assertEquals(ids, vistos);
    }
}
