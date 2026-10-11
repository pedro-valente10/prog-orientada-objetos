package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

/** Caso de teste do encerramento da partida. */
public class FimDeJogoTest {

    /**
     * CT-22 Rodada final após o Kurultai encher.
     * Cenário: 2 jogadores (limite de 10 votos). Na vez de VERMELHO, 10 entregas a B
     * (20 PELES) enchem o conselho.
     * Esperado: o gatilho é acionado, mas o jogo continua; termina a rodada atual (AZUL joga),
     * depois mais UMA rodada completa (VERMELHO e AZUL); só então o jogo encerra e
     * novas ações são recusadas.
     */
    @Test
    public void jogoEncerraDepoisDaRodadaAtualMaisUmaRodadaExtra() {
        Partida partida = Cenarios.partida(2);
        partida.getJogador(CorJogador.VERMELHO).adicionarTesouro(TipoTesouro.PELES, 20);

        partida.iniciarTurno(2, 0);
        partida.mover(Arrays.asList("A", "B"));
        for (int i = 0; i < 10; i++) {
            partida.entregarTesouros("B", new HashMap<TipoTesouro, Integer>());
        }
        assertTrue(partida.isFimAcionado());
        assertFalse(partida.isJogoEncerrado());
        partida.encerrarTurno();                       // fim da vez de VERMELHO

        partida.iniciarTurno(0, 0);                    // vez de AZUL: completa a rodada atual
        partida.encerrarTurno();
        assertFalse(partida.isJogoEncerrado());

        partida.iniciarTurno(0, 0);                    // rodada extra: VERMELHO
        partida.encerrarTurno();
        assertFalse(partida.isJogoEncerrado());
        partida.iniciarTurno(0, 0);                    // rodada extra: AZUL
        partida.encerrarTurno();
        assertTrue(partida.isJogoEncerrado());

        try {
            partida.iniciarTurno(0, 0);
            fail("O jogo já terminou");
        } catch (RegraInvalidaException esperado) {
            assertEquals(10, partida.getKurultai().getVotos(CorJogador.VERMELHO));
        }
    }
}
