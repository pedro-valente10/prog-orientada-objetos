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

    
    
}
