package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/** Casos de teste da regra de movimento dos peões. */
public class MovimentoTest {

    private Partida partida;

    @Before
    public void preparar() {
        partida = Cenarios.partida(2);
    }

    /**
     * CT-01 Passar por parada simples ocupada.
     * Objetivo: o peão pode atravessar uma parada ocupada por outro jogador.
     * Cenário: AZUL está em A; VERMELHO está em KARAKORUM com 3 movimentos.
     * Entrada: mover([A, B]).
     * Esperado: VERMELHO termina em B (livre) e restam 1 movimento (gastou 2).
     */
    @Test
    public void passaPorParadaOcupadaETerminaEmParadaLivre() {
        Cenarios.colocarPeao(partida, CorJogador.AZUL, "A");
        partida.iniciarTurno(3, 0);
        partida.mover(Arrays.asList("A", "B"));
        assertEquals("B", partida.getJogador(CorJogador.VERMELHO).getPosicao());
        assertEquals(1, partida.getMovimentosRestantes());
    }

    /**
     * CT-02 Não terminar em parada simples ocupada.
     * Cenário: AZUL está em A; VERMELHO em KARAKORUM com 3 movimentos.
     * Entrada: mover([A]).
     * Esperado: RegraInvalidaException e NENHUMA mudança de estado
     * (posição, movimentos e ocupação das paradas permanecem).
     */
    @Test
    public void naoTerminaEmParadaSimplesOcupadaENadaMuda() {
        Cenarios.colocarPeao(partida, CorJogador.AZUL, "A");
        partida.iniciarTurno(3, 0);
        try {
            partida.mover(Arrays.asList("A"));
            fail("Deveria lançar RegraInvalidaException");
        } catch (RegraInvalidaException esperado) {
            assertEquals(Tabuleiro.KARAKORUM, partida.getJogador(CorJogador.VERMELHO).getPosicao());
            assertEquals(3, partida.getMovimentosRestantes());
            assertEquals(1, partida.getTabuleiro().getParada("A").quantidadePeoes());
            assertEquals(1, partida.getTabuleiro().getParada(Tabuleiro.KARAKORUM).quantidadePeoes());
        }
    }

    /**
     * CT-03 Parada dupla comporta 2 peões.
     * Cenário: AZUL está em B (parada dupla).
     * Entrada: VERMELHO move [A, B].
     * Esperado: movimento permitido; B passa a ter 2 peões.
     */
    @Test
    public void podeTerminarEmParadaDuplaComUmOcupante() {
        Cenarios.colocarPeao(partida, CorJogador.AZUL, "B");
        partida.iniciarTurno(2, 0);
        partida.mover(Arrays.asList("A", "B"));
        assertEquals(2, partida.getTabuleiro().getParada("B").quantidadePeoes());
    }

    /**
     * CT-04 Saltar iurta própria.
     * Cenário: VERMELHO tem iurta em A; turno com 1 movimento e 1 punho.
     * Entrada: mover([A, B]); depois coletarTributo(P1) (adjacente só a A) e coletarTributo(P2) (adjacente a B).
     * Esperado: o salto sobre A não gasta movimento (restam 0); A não conta como visitada,
     * então P1 é recusada, e P2 é aceita.
     */
    @Test
    public void saltaIurtaPropriaSemGastarMovimentoENaoVisitaAParada() {
        partida.getTabuleiro().getParada("A").adicionarIurta(CorJogador.VERMELHO);
        partida.iniciarTurno(1, 1);
        partida.mover(Arrays.asList("A", "B"));
        assertEquals(0, partida.getMovimentosRestantes());
        try {
            partida.coletarTributo("P1");
            fail("A parada A foi apenas saltada; não pode coletar de P1");
        } catch (RegraInvalidaException esperado) {
            // ok
        }
        partida.coletarTributo("P2");
        assertEquals(2, partida.getJogador(CorJogador.VERMELHO).quantidadeTributo(TipoTributo.MOEDA));
    }

    /**
     * CT-05 Movimentos insuficientes.
     * Entrada: mover([A, B, C]) com apenas 2 movimentos.
     * Esperado: RegraInvalidaException; o peão continua em KARAKORUM.
     */
    @Test(expected = RegraInvalidaException.class)
    public void movimentosInsuficientesImpedemOMovimento() {
        partida.iniciarTurno(2, 0);
        try {
            partida.mover(Arrays.asList("A", "B", "C"));
        } finally {
            assertEquals(Tabuleiro.KARAKORUM, partida.getJogador(CorJogador.VERMELHO).getPosicao());
        }
    }
}
