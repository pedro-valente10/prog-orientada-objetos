package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/** Casos de teste do ataque e da conquista de cidades. */
public class CidadeTest {

    private Partida partida;
    private Jogador vermelho;

    @Before
    public void preparar() {
        partida = Cenarios.partida(2);
        vermelho = partida.getJogador(CorJogador.VERMELHO);
    }

    /**
     * CT-12 Custo progressivo das espadas.
     * Cenário: VERMELHO tem 3 espadas e está em A (adjacente à cidade X, que tem 3 tesouros).
     * Entrada: atacarCidade(X,1) e depois atacarCidade(X,1) no mesmo turno; por fim mais um ataque.
     * Esperado: 1º tesouro custa 1 espada, o seguinte custa 2 (total 3, restam 0);
     * o terceiro ataque é recusado por falta de espadas.
     */
    @Test
    public void primeiroTesouroCustaUmaEspadaOsSeguintesDuas() {
        vermelho.adicionarTributo(TipoTributo.ESPADA, 3);
        partida.iniciarTurno(1, 0);
        partida.mover(Arrays.asList("A"));

        partida.atacarCidade("X", 1);
        assertEquals(2, vermelho.quantidadeTributo(TipoTributo.ESPADA));
        partida.atacarCidade("X", 1);
        assertEquals(0, vermelho.quantidadeTributo(TipoTributo.ESPADA));
        assertEquals(2, vermelho.totalTesouros());
        try {
            partida.atacarCidade("X", 1);
            fail("Sem espadas");
        } catch (RegraInvalidaException esperado) {
            assertEquals(1, partida.getTabuleiro().getCidade("X").quantidadeTesouros());
        }
    }

    /**
     * CT-13 Conquista de cidade.
     * Cenário: VERMELHO tem 5 espadas (1+2+2) e está em A; X tem 3 tesouros; Y é a próxima da pilha.
     * Entrada: atacarCidade(X,3).
     * Esperado: X conquistada por VERMELHO, com uma iurta dele nela (restam 11); a cidade Y é
     * revelada com 4 tesouros; VERMELHO fica com 3 tesouros e 0 espadas;
     * a iurta na cidade conta na região RUSSIA.
     */
    @Test
    public void conquistaColocaIurtaERevelaNovaCidadeComQuatroTesouros() {
        vermelho.adicionarTributo(TipoTributo.ESPADA, 5);
        partida.iniciarTurno(1, 0);
        partida.mover(Arrays.asList("A"));
        partida.atacarCidade("X", 3);

        Cidade x = partida.getTabuleiro().getCidade("X");
        assertTrue(x.isConquistada());
        assertEquals(CorJogador.VERMELHO, x.getDono());
        assertEquals(Jogador.MAX_IURTAS - 1, vermelho.getIurtasDisponiveis());
        assertEquals(4, partida.getTabuleiro().getCidade("Y").quantidadeTesouros());
        assertEquals(3, vermelho.totalTesouros());
        assertEquals(0, vermelho.quantidadeTributo(TipoTributo.ESPADA));
        assertEquals(1, partida.getTabuleiro().contarIurtas(CorJogador.VERMELHO, Regiao.RUSSIA));
    }

    /**
     * CT-14 Ataque sem espadas suficientes não altera o estado.
     * Cenário: 2 espadas; atacar 3 tesouros custaria 5.
     * Esperado: RegraInvalidaException; X continua com 3 tesouros; jogador sem tesouros e com 2 espadas.
     */
    @Test
    public void ataqueSemEspadasSuficientesNaoAlteraEstado() {
        vermelho.adicionarTributo(TipoTributo.ESPADA, 2);
        partida.iniciarTurno(1, 0);
        partida.mover(Arrays.asList("A"));
        try {
            partida.atacarCidade("X", 3);
            fail("Faltam espadas");
        } catch (RegraInvalidaException esperado) {
            assertEquals(3, partida.getTabuleiro().getCidade("X").quantidadeTesouros());
            assertEquals(0, vermelho.totalTesouros());
            assertEquals(2, vermelho.quantidadeTributo(TipoTributo.ESPADA));
            assertFalse(partida.getTabuleiro().getCidade("X").isConquistada());
        }
    }

    /**
     * CT-15 O ataque exige ter parado ao lado da cidade.
     * Cenário: VERMELHO continua em KARAKORUM (não adjacente a X) e tem espadas.
     * Esperado: RegraInvalidaException.
     */
    @Test(expected = RegraInvalidaException.class)
    public void ataqueExigeParadaAdjacenteVisitada() {
        vermelho.adicionarTributo(TipoTributo.ESPADA, 5);
        partida.iniciarTurno(1, 0);
        partida.atacarCidade("X", 1);
    }
}
