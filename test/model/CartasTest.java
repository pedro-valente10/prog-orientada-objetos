package model;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;

/** Cartas abertas, cartas secretas e primeira entrega de bônus. */
public class CartasTest {
    @Test public void quatroCartasAbertasEDuasOpcoesPorJogador() {
        List<CorJogador> ordem = Arrays.asList(CorJogador.VERMELHO, CorJogador.AZUL, CorJogador.VERDE);
        Cartas cartas = new Cartas(ordem, TabuleiroFactory.criarTabuleiroBasico(4L), 4L);
        assertEquals(4, cartas.abertas().size());
        HashSet<String> opcoes = new HashSet<>();
        for (CorJogador cor : ordem) {
            assertEquals(2, cartas.opcoes(cor).size());
            opcoes.addAll(cartas.opcoes(cor));
        }
        assertEquals(6, opcoes.size());
        cartas.escolher(CorJogador.VERMELHO, 1);
        assertNotNull(cartas.escolhida(CorJogador.VERMELHO));
        assertTrue(cartas.opcoes(CorJogador.VERMELHO).isEmpty());
        try { cartas.escolher(CorJogador.VERMELHO, 0); fail("segunda escolha"); }
        catch (RegraInvalidaException esperado) { }
    }

    @Test public void soldadoPontuaQuatroSeteEDezMelhorias() {
        Partida p = Cenarios.partida(2);
        Jogador j = p.getJogador(CorJogador.VERMELHO);
        assertEquals(0, Cartas.Secreta.SOLDADO.votos(p, j));
        int id = 100;
        for (int n : new int[] {4, 3, 3}) {
            Regiao cor = id == 100 ? Regiao.CHINA : id == 104 ? Regiao.PERSIA : Regiao.RUSSIA;
            for (int i=0;i<n;i++) j.getTabuleiroIndividual().colocar(Coluna.valueOf(cor.name()),
                    new Melhoria(id++, cor, Melhoria.Tipo.CAVALOS, null));
            assertEquals(id == 104 ? 1 : id == 107 ? 2 : 3, Cartas.Secreta.SOLDADO.votos(p, j));
        }
    }

    @Test public void cartaAbertaCumpridaDaVotoUmaUnicaVez() {
        for (long semente=0; semente<100; semente++) {
            Partida p = new Partida(Arrays.asList(CorJogador.VERMELHO, CorJogador.AZUL,
                    CorJogador.VERDE), TabuleiroFactory.criarTabuleiroBasico(semente), semente);
            if (!p.getCartasAbertas().contains("Ter 4 espadas")) continue;
            p.getJogador(CorJogador.VERMELHO).adicionarTributo(TipoTributo.ESPADA, 4);
            p.verificarCartasAbertas();
            assertEquals(1, p.getKurultai().getVotos(CorJogador.VERMELHO));
            p.verificarCartasAbertas();
            assertEquals(1, p.getKurultai().getVotos(CorJogador.VERMELHO));
            assertFalse(p.getCartasAbertas().contains("Ter 4 espadas"));
            return;
        }
        fail("semente sem carta de espadas no intervalo de teste");
    }

    @Test public void primeiraEntregaConcedeBonusUmaVez() {
        Partida p = new Partida(Arrays.asList(CorJogador.VERMELHO, CorJogador.AZUL,
                CorJogador.VERDE), TabuleiroFactory.criarTabuleiroBasico(7L), 7L);
        Jogador j = p.getJogador(CorJogador.VERMELHO);
        for (TipoTesouro tipo : new TipoTesouro[] { TipoTesouro.PELES, TipoTesouro.FERRO, TipoTesouro.SEDA })
            j.adicionarTesouro(tipo, 2);
        p.iniciarTurno(0, 0);
        p.entregarTesouros(Tabuleiro.KARAKORUM, new HashMap<TipoTesouro,Integer>());
        assertEquals(1, p.getBonusRecebidos(CorJogador.VERMELHO));
        if (!p.getOpcoesTesouroBonus().isEmpty()) p.escolherTesouroBonus(0);
        p.entregarTesouros(Tabuleiro.KARAKORUM, new HashMap<TipoTesouro,Integer>());
        assertEquals(1, p.getBonusRecebidos(CorJogador.VERMELHO));
    }

    @Test public void bonusDeTesourosExigeEscolhaDeUmDosTres() {
        for (long semente=0; semente<100; semente++) {
            Partida p = new Partida(Arrays.asList(CorJogador.VERMELHO, CorJogador.AZUL,
                    CorJogador.VERDE), TabuleiroFactory.criarTabuleiroBasico(semente), semente);
            Jogador j = p.getJogador(CorJogador.VERMELHO);
            for (TipoTesouro tipo : new TipoTesouro[] { TipoTesouro.PELES, TipoTesouro.FERRO, TipoTesouro.SEDA })
                j.adicionarTesouro(tipo, 1);
            p.iniciarTurno(0, 0);
            p.entregarTesouros(Tabuleiro.KARAKORUM, new HashMap<TipoTesouro,Integer>());
            if (p.getOpcoesTesouroBonus().isEmpty()) continue;
            assertEquals(3, p.getOpcoesTesouroBonus().size());
            try { p.encerrarTurno(); fail("bônus pendente"); }
            catch (RegraInvalidaException esperado) { }
            p.escolherTesouroBonus(1);
            assertTrue(p.getOpcoesTesouroBonus().isEmpty());
            assertEquals(1, j.totalTesouros());
            return;
        }
        fail("bônus de tesouros não encontrado no intervalo de sementes");
    }

    @Test public void cartasSecretasEntramNosVotosFinais() {
        Partida p = null;
        int indice = -1;
        for (long semente=0; semente<100 && indice<0; semente++) {
            Partida candidata = new Partida(Arrays.asList(CorJogador.VERMELHO, CorJogador.AZUL),
                    Cenarios.tabuleiroMini(), semente);
            for (int i=0;i<2;i++) if (candidata.getOpcoesSecretas(CorJogador.VERMELHO).get(i)
                    .startsWith("Peças de melhoria")) { p=candidata; indice=i; break; }
        }
        assertNotNull("carta Soldado não encontrada", p);
        Jogador j = p.getJogador(CorJogador.VERMELHO);
        p.escolherCartaSecreta(CorJogador.VERMELHO, indice);
        int antes = p.votosFinais().get(CorJogador.VERMELHO);
        for (int i=0;i<4;i++) j.getTabuleiroIndividual().colocar(Coluna.CHINA,
                new Melhoria(200+i, Regiao.CHINA, Melhoria.Tipo.CAVALOS, null));
        assertEquals(antes+1, (int) p.votosFinais().get(CorJogador.VERMELHO));
    }
    @Test public void cartasDeCidadeContamLocaisAindaNaoRevelados() {
        Partida p = new Partida(Arrays.asList(CorJogador.VERMELHO, CorJogador.AZUL),
                TabuleiroFactory.criarTabuleiroBasico(17L), 17L);
        java.util.Set<Cidade> reveladas = new java.util.HashSet<>(
                p.getTabuleiro().getCidadesReveladas());
        int preparadas = 0;
        for (Cidade cidade : p.getTabuleiro().getTodasCidades()) {
            if (reveladas.contains(cidade)) continue;
            int i = 0;
            for (String parada : cidade.getParadasAdjacentes()) {
                p.getTabuleiro().getParada(parada).adicionarIurta(CorJogador.VERMELHO);
                if (++i == 2) break;
            }
            if (++preparadas == 3) break;
        }
        assertEquals(3, preparadas);
        Jogador jogador = p.getJogador(CorJogador.VERMELHO);
        assertEquals(1, Cartas.Secreta.CIDADAO.votos(p, jogador));
        assertEquals(2, Cartas.Secreta.PREFEITO.votos(p, jogador));
    }

    @Test public void linhaDeCartaAbertaIncluiColunaBranca() {
        TabuleiroJogador individual = new TabuleiroJogador();
        individual.colocar(Coluna.CHINA, new Melhoria(301, Regiao.CHINA, Melhoria.Tipo.CAVALOS, null));
        individual.colocar(Coluna.PERSIA, new Melhoria(302, Regiao.PERSIA, Melhoria.Tipo.CAVALOS, null));
        individual.colocar(Coluna.RUSSIA, new Melhoria(303, Regiao.RUSSIA, Melhoria.Tipo.CAVALOS, null));
        assertFalse(individual.temLinhaCompleta());
        assertEquals(1, individual.bonusBrancosPendentes());
        individual.colocarBonusBranco(new Melhoria(304, Regiao.CHINA, Melhoria.Tipo.CAVALOS, null));
        assertTrue(individual.temLinhaCompleta());
    }}

\n