package model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

/** Salvamento, recuperacao e disponibilidade de acoes da quarta iteracao. */
public class QuartaIteracaoTest {
    @Test public void arquivoAsciiRestauraTurnoJogadoresEMapa() throws Exception {
        Jogo jogo = Jogo.getInstance();
        jogo.novaPartida(3, 117L);
        jogo.definirNomeJogador("VERMELHO", "Ana");
        jogo.escolherCartaSecreta(0);
        String carta = jogo.getCartaSecreta("VERMELHO");
        jogo.ativarColuna(Coluna.PERSIA);
        jogo.moverPara("P5");
        jogo.encerrarTurno();
        Path arquivo = Files.createTempFile("khan-partida-", ".txt");
        try {
            jogo.salvar(arquivo);
            byte[] bytes = Files.readAllBytes(arquivo);
            assertTrue(new String(bytes, "US-ASCII").startsWith("HERDEIROS_DO_KHAN_SAVE_V1"));
            for (byte b : bytes) assertTrue("o arquivo deve ser ASCII", (b & 0x80) == 0);
            jogo.novaPartida(2, 999L);
            jogo.recuperar(arquivo);
            assertEquals(EventoJogo.PARTIDA_RECUPERADA, jogo.get(Jogo.INFO_EVENTO));
            assertEquals(3, jogo.getJogadores().size());
            assertEquals("AZUL", jogo.getJogadorDaVez());
            assertEquals("P5", jogo.getPosicao("VERMELHO"));
            assertEquals("Ana", jogo.getNomeJogador("VERMELHO"));
            assertEquals(carta, jogo.getCartaSecreta("VERMELHO"));
            assertFalse(jogo.isTurnoAtivo());
            jogo.ativarColuna(Coluna.PERSIA);
            assertTrue(jogo.isTurnoAtivo());
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    @Test public void salvarSoEntreTurnosEArquivoRuimNaoTrocaPartida() throws Exception {
        Jogo jogo = Jogo.getInstance();
        jogo.novaPartida(2, 123L);
        Path arquivo = Files.createTempFile("khan-invalido-", ".txt");
        try {
            jogo.ativarColuna(Coluna.PERSIA);
            assertFalse(jogo.podeSalvar());
            try {
                jogo.salvar(arquivo);
                fail("salvamento durante turno");
            } catch (IllegalStateException esperado) { }
            jogo.encerrarTurno();
            Files.write(arquivo, "arquivo invalido".getBytes("US-ASCII"));
            try {
                jogo.recuperar(arquivo);
                fail("arquivo invalido");
            } catch (IOException esperado) { }
            assertEquals("AZUL", jogo.getJogadorDaVez());
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    @Test public void coletaEEncerramentoAcompanhamEstadoReal() {
        Jogo jogo = Jogo.getInstance();
        jogo.novaPartida(2, 1L);
        assertFalse(jogo.podeEncerrarTurno());
        assertNotNull(jogo.getImpedimentoColeta("K3"));
        jogo.ativarColuna(Coluna.PERSIA);
        assertTrue(jogo.podeEncerrarTurno());
        jogo.moverPara("P5");
        assertNull(jogo.getImpedimentoColeta("K3"));
        assertNotNull(jogo.getImpedimentoColeta("P1"));
        jogo.coletarTributo("K3");
        assertNotNull(jogo.getImpedimentoColeta("P3"));
    }

    @Test public void khanObrigatorioBloqueiaEncerramento() {
        Jogo jogo = Jogo.getInstance();
        jogo.novaPartida(2, 2L);
        jogo.ativarColuna(Coluna.RUSSIA);
        assertFalse(jogo.podeEncerrarTurno());
        assertTrue(jogo.podePosicionarKhan("K2"));
        jogo.posicionarKhan("K2");
        assertTrue(jogo.podeEncerrarTurno());
        assertFalse(jogo.podePosicionarKhan("K1"));
    }
}