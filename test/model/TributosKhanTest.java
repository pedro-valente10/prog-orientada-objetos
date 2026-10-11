package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/** Casos de teste de coleta de tributos, construção de iurtas e uso do Khan. */
public class TributosKhanTest {

    private Partida partida;

    @Before
    public void preparar() {
        partida = Cenarios.partida(2);
    }

    /**
     * CT-06 Coleta restrita a províncias adjacentes a paradas visitadas.
     * Cenário: VERMELHO vai a A (adjacente a P1) com 3 punhos.
     * Entrada: coletarTributo(P1) e depois coletarTributo(P3) (P3 só é adjacente a C).
     * Esperado: P1 aceita (iurta +1, P1 fica com 1); P3 recusada sem gastar punho.
     */
    @Test
    public void coletaSomenteDeProvinciaAdjacenteAParadaVisitada() {
        partida.iniciarTurno(1, 3);
        partida.mover(Arrays.asList("A"));
        partida.coletarTributo("P1");
        assertEquals(1, partida.getJogador(CorJogador.VERMELHO).quantidadeTributo(TipoTributo.IURTA));
        assertEquals(1, partida.getTabuleiro().getProvincia("P1").getTributos());
        try {
            partida.coletarTributo("P3");
            fail("P3 não é adjacente a nenhuma parada visitada");
        } catch (RegraInvalidaException esperado) {
            assertEquals(2, partida.getPunhosRestantes());
        }
    }

    /**
     * CT-07 O número de punhos limita as coletas.
     * Cenário: 2 punhos; P1 tem 2 tributos.
     * Entrada: 3 chamadas a coletarTributo(P1).
     * Esperado: as 2 primeiras funcionam; a 3ª lança exceção; jogador fica com 2 iurtas-tributo.
     */
    @Test
    public void numeroDePunhosLimitaAsColetas() {
        partida.iniciarTurno(1, 2);
        partida.mover(Arrays.asList("A"));
        partida.coletarTributo("P1");
        partida.coletarTributo("P1");
        try {
            partida.coletarTributo("P1");
            fail("Não há mais punhos");
        } catch (RegraInvalidaException esperado) {
            assertEquals(2, partida.getJogador(CorJogador.VERMELHO).quantidadeTributo(TipoTributo.IURTA));
            assertEquals(0, partida.getTabuleiro().getProvincia("P1").getTributos());
        }
    }

    /**
     * CT-08 Capacidade de iurtas por parada.
     * Cenário: jogador tem 3 tributos de iurta e visitou A (simples); KARAKORUM é o ponto de partida.
     * Entrada: construir 2 iurtas em A; construir em KARAKORUM.
     * Esperado: 1ª ok; 2ª recusada (parada simples comporta 1); KARAKORUM recusada
     * (não tem iurtas); só 1 tributo de iurta foi consumido.
     */
    @Test
    public void construirIurtaRespeitaCapacidadeDasParadas() {
        Jogador v = partida.getJogador(CorJogador.VERMELHO);
        v.adicionarTributo(TipoTributo.IURTA, 3);
        partida.iniciarTurno(1, 0);
        partida.mover(Arrays.asList("A"));
        partida.construirIurta("A");
        try {
            partida.construirIurta("A");
            fail("Parada simples comporta apenas 1 iurta");
        } catch (RegraInvalidaException esperado) {
            // ok
        }
        try {
            partida.construirIurta(Tabuleiro.KARAKORUM);
            fail("Karakorum não comporta iurtas");
        } catch (RegraInvalidaException esperado) {
            // ok
        }
        assertEquals(2, v.quantidadeTributo(TipoTributo.IURTA));
        assertEquals(Jogador.MAX_IURTAS - 1, v.getIurtasDisponiveis());
    }

    /**
     * CT-09 Khan em província Khan.
     * Cenário: P1 já tem 3 tributos (máximo); P2 tem 1; KH1 tem 0.
     * Entrada: posicionarKhan(KH1); depois KH1 de novo; depois P1 (não é província Khan); depois KH2.
     * Esperado: KH1=1, P1 continua 3 (teto), P2=2; repetir o local, ou usar província comum, é
     * recusado; KH2 é aceito e dá tributo a KH2, P2 e P3.
     */
    @Test
    public void khanDaTributoATresProvinciasRespeitandoOMaximo() {
        Tabuleiro t = partida.getTabuleiro();
        t.getProvincia("P1").adicionarTributo(); // agora 3
        partida.iniciarTurno(0, 0);

        partida.posicionarKhan("KH1");
        assertEquals(1, t.getProvincia("KH1").getTributos());
        assertEquals(3, t.getProvincia("P1").getTributos());
        assertEquals(2, t.getProvincia("P2").getTributos());

        try {
            partida.posicionarKhan("KH1");
            fail("O Khan não pode permanecer onde está");
        } catch (RegraInvalidaException esperado) {
            // ok
        }
        try {
            partida.posicionarKhan("P1");
            fail("P1 não é província Khan");
        } catch (RegraInvalidaException esperado) {
            // ok
        }
        partida.posicionarKhan("KH2");
        assertEquals(1, t.getProvincia("KH2").getTributos());
        assertEquals(3, t.getProvincia("P2").getTributos());
        assertEquals(2, t.getProvincia("P3").getTributos());
    }

    /**
     * CT-10 Khan na área de melhorias com ícones visíveis.
     * Cenário: vagas de moeda e espada vazias (ícones visíveis); VERMELHO começa com 1 moeda.
     * Entrada: posicionarKhan(MELHORIAS).
     * Esperado: +1 moeda (total 2) e +1 espada; a área é reabastecida (ícones somem).
     */
    @Test
    public void khanNaAreaDeMelhoriasDaMoedaEEspadaQuandoIconesVisiveis() {
        partida.getArea().remover(AreaMelhorias.VAGA_ICONE_MOEDA);
        partida.getArea().remover(AreaMelhorias.VAGA_ICONE_ESPADA);
        partida.iniciarTurno(0, 0);
        partida.posicionarKhan(Khan.AREA_MELHORIAS);
        Jogador v = partida.getJogador(CorJogador.VERMELHO);
        assertEquals(2, v.quantidadeTributo(TipoTributo.MOEDA));
        assertEquals(1, v.quantidadeTributo(TipoTributo.ESPADA));
        assertFalse(partida.getArea().iconeMoedaVisivel());
    }

    /**
     * CT-11 Khan na área de melhorias sem ícones visíveis.
     * Cenário: todas as vagas ocupadas.
     * Entrada: posicionarKhan(MELHORIAS).
     * Esperado: o jogador não ganha moeda nem espada (continua com 1 moeda e 0 espadas).
     */
    @Test
    public void khanNaAreaSemIconesVisiveisNaoDaNada() {
        partida.iniciarTurno(0, 0);
        partida.posicionarKhan(Khan.AREA_MELHORIAS);
        Jogador v = partida.getJogador(CorJogador.VERMELHO);
        assertEquals(1, v.quantidadeTributo(TipoTributo.MOEDA));
        assertEquals(0, v.quantidadeTributo(TipoTributo.ESPADA));
    }
}
