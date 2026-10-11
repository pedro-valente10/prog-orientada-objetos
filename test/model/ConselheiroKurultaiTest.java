package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/** Casos de teste de entregas a conselheiros e do conselho Kurultai. */
public class ConselheiroKurultaiTest {

    private Partida partida;
    private Jogador vermelho;

    @Before
    public void preparar() {
        partida = Cenarios.partida(2);
        vermelho = partida.getJogador(CorJogador.VERMELHO);
    }

    /**
     * CT-16 Entrega válida a conselheiro de "2 tesouros iguais".
     * Cenário: VERMELHO tem 3 PELES e parou em B (conselheiro pede 2 PELES).
     * Entrada: entregarTesouros(B).
     * Esperado: +1 voto; Kurultai com ocupação 1; sobra 1 PELES; VERMELHO é o primeiro entregador.
     */
    @Test
    public void entregaDeDoisIguaisDaUmVotoEAvancaOKurultai() {
        vermelho.adicionarTesouro(TipoTesouro.PELES, 3);
        partida.iniciarTurno(2, 0);
        partida.mover(Arrays.asList("A", "B"));
        partida.entregarTesouros("B", new HashMap<TipoTesouro, Integer>());

        assertEquals(1, partida.getKurultai().getVotos(CorJogador.VERMELHO));
        assertEquals(1, partida.getKurultai().getOcupacao());
        assertEquals(1, vermelho.quantidadeTesouro(TipoTesouro.PELES));
        assertEquals(CorJogador.VERMELHO,
                partida.getTabuleiro().getParada("B").getConselheiro().getPrimeiroEntregador());
    }

    /**
     * CT-17 Entrega com tesouros insuficientes.
     * Cenário: VERMELHO tem apenas 1 PELES em B.
     * Esperado: RegraInvalidaException; nada é debitado e nenhum voto é dado.
     */
    @Test
    public void entregaSemTesourosSuficientesNaoAlteraEstado() {
        vermelho.adicionarTesouro(TipoTesouro.PELES, 1);
        partida.iniciarTurno(2, 0);
        partida.mover(Arrays.asList("A", "B"));
        try {
            partida.entregarTesouros("B", new HashMap<TipoTesouro, Integer>());
            fail("Faltam tesouros");
        } catch (RegraInvalidaException esperado) {
            assertEquals(1, vermelho.quantidadeTesouro(TipoTesouro.PELES));
            assertEquals(0, partida.getKurultai().getVotos(CorJogador.VERMELHO));
        }
    }

    /**
     * CT-18 Entrega exige ter parado na parada do conselheiro neste turno.
     * Cenário: VERMELHO tem PELES mas está em KARAKORUM.
     * Esperado: RegraInvalidaException.
     */
    @Test(expected = RegraInvalidaException.class)
    public void entregaExigeTerParadoNoConselheiro() {
        vermelho.adicionarTesouro(TipoTesouro.PELES, 2);
        partida.iniciarTurno(2, 0);
        partida.entregarTesouros("B", new HashMap<TipoTesouro, Integer>());
    }

    /**
     * CT-19 Conselheiro de "3 tesouros quaisquer".
     * Entrada: oferta com 2 tesouros (inválida) e depois oferta com 3 (FERRO 2 + SEDA 1).
     * Esperado: a 1ª é recusada; a 2ª é aceita e debita exatamente os tesouros oferecidos.
     */
    @Test
    public void tresQuaisquerExigeExatamenteTresTesouros() {
        Conselheiro c = Conselheiro.tresQuaisquer("CONS_TESTE");
        vermelho.adicionarTesouro(TipoTesouro.FERRO, 2);
        vermelho.adicionarTesouro(TipoTesouro.SEDA, 1);

        Map<TipoTesouro, Integer> doisApenas = new HashMap<>();
        doisApenas.put(TipoTesouro.FERRO, 2);
        try {
            c.entregar(vermelho, doisApenas);
            fail("Oferta com 2 tesouros deve ser recusada");
        } catch (RegraInvalidaException esperado) {
            assertEquals(2, vermelho.quantidadeTesouro(TipoTesouro.FERRO));
        }

        Map<TipoTesouro, Integer> tres = new HashMap<>();
        tres.put(TipoTesouro.FERRO, 2);
        tres.put(TipoTesouro.SEDA, 1);
        assertEquals(1, c.entregar(vermelho, tres));
        assertEquals(0, vermelho.totalTesouros());
    }

    /**
     * CT-20 Karakorum vale 2 votos.
     * Entrada: entrega de PELES+FERRO+SEDA ao conselheiro de Karakorum.
     * Esperado: 2 votos.
     */
    @Test
    public void conselheiroDeKarakorumDaDoisVotos() {
        Conselheiro k = Conselheiro.tresEspecificos("CONS_K", TipoTesouro.PELES, TipoTesouro.FERRO, TipoTesouro.SEDA);
        vermelho.adicionarTesouro(TipoTesouro.PELES, 1);
        vermelho.adicionarTesouro(TipoTesouro.FERRO, 1);
        vermelho.adicionarTesouro(TipoTesouro.SEDA, 1);
        assertEquals(2, k.entregar(vermelho, null));
        assertEquals(0, vermelho.totalTesouros());
    }

    /**
     * CT-21 Limite do Kurultai conforme o número de jogadores.
     * Esperado: 2->10, 3->14, 4->16, 5->17; 1 jogador é inválido; o conselho só
     * enche quando a ocupação atinge o limite.
     */
    @Test
    public void limiteDoKurultaiDependeDoNumeroDeJogadores() {
        assertEquals(10, new Kurultai(cores(2)).getLimite());
        assertEquals(14, new Kurultai(cores(3)).getLimite());
        assertEquals(16, new Kurultai(cores(4)).getLimite());
        assertEquals(17, new Kurultai(cores(5)).getLimite());

        Kurultai k = new Kurultai(cores(2));
        k.adicionarVotos(CorJogador.VERMELHO, 9);
        assertFalse(k.estaCheio());
        k.adicionarVotos(CorJogador.AZUL, 1);
        assertTrue(k.estaCheio());

        try {
            new Kurultai(cores(1));
            fail("1 jogador é inválido");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }

    private static java.util.List<CorJogador> cores(int n) {
        return new java.util.ArrayList<>(Arrays.asList(CorJogador.values()).subList(0, n));
    }
}
