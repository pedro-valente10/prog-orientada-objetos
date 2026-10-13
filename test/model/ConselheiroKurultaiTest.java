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

    
    
    
    
}
