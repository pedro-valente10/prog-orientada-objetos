package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Singleton, Façade e Observer da 3ª iteração. */
public class TerceiraIteracaoTest {

    /** Observador de teste: guarda os eventos recebidos lendo-os por {@link ObservadoIF#get(int)}. */
    private static final class Gravador implements ObservadorIF {
        final List<EventoJogo> eventos = new ArrayList<>();
        final List<String> detalhes = new ArrayList<>();

        @Override public void notify(ObservadoIF observado) {
            eventos.add((EventoJogo) observado.get(Jogo.INFO_EVENTO));
            detalhes.add((String) observado.get(Jogo.INFO_DETALHE));
        }

        EventoJogo ultimo() { return eventos.get(eventos.size() - 1); }
        String ultimoDetalhe() { return detalhes.get(detalhes.size() - 1); }
    }

    private Jogo jogo;
    private Gravador gravador;

    @Before public void iniciar() {
        jogo = Jogo.getInstance();
        jogo.novaPartida(3, 7L);
        gravador = new Gravador();
        jogo.add(gravador);
    }

    @After public void limpar() { jogo.remove(gravador); }

    @Test public void singletonDevolveSempreAMesmaInstancia() {
        assertSame(Jogo.getInstance(), Jogo.getInstance());
        assertSame(jogo, Jogo.getInstance());
    }

    @Test public void novaPartidaMantemObservadoresENotifica() {
        jogo.novaPartida(2, 1L);
        assertEquals(EventoJogo.PARTIDA_INICIADA, gravador.ultimo());
        assertEquals(2, jogo.getJogadores().size());
        jogo.ativarColuna(Coluna.PERSIA);
        assertEquals(EventoJogo.COLUNA_ATIVADA, gravador.ultimo());
    }

    @Test public void movimentoDoPiaoNotificaComODestino() {
        jogo.ativarColuna(Coluna.PERSIA);
        assertEquals(EventoJogo.COLUNA_ATIVADA, gravador.ultimo());
        assertEquals("PERSIA", gravador.ultimoDetalhe());
        jogo.moverPara("P5");
        assertEquals(EventoJogo.PEAO_MOVIDO, gravador.ultimo());
        assertEquals("P5", gravador.ultimoDetalhe());
        assertEquals("P5", jogo.getPosicao("VERMELHO"));
        assertEquals(2, gravador.eventos.size());
    }

    @Test public void caminhoExplicitoTambemNotifica() {
        jogo.ativarColuna(Coluna.PERSIA);
        jogo.mover(Arrays.asList("P5"));
        assertEquals(EventoJogo.PEAO_MOVIDO, gravador.ultimo());
        assertEquals("P5", gravador.ultimoDetalhe());
    }

    @Test public void acaoInvalidaNaoNotifica() {
        try {
            jogo.moverPara("P5"); // nenhuma coluna ativada
            fail("sem turno ativo");
        } catch (RegraInvalidaException esperado) {
            assertTrue(gravador.eventos.isEmpty());
        }
        jogo.ativarColuna(Coluna.PERSIA);
        int antes = gravador.eventos.size();
        try {
            jogo.moverPara(Tabuleiro.KARAKORUM); // já está lá
            fail("destino inválido");
        } catch (RegraInvalidaException esperado) {
            assertEquals(antes, gravador.eventos.size());
        }
    }

    @Test public void observadorRemovidoNaoRecebeMais() {
        jogo.remove(gravador);
        jogo.ativarColuna(Coluna.PERSIA);
        assertTrue(gravador.eventos.isEmpty());
        jogo.add(gravador);
        jogo.add(gravador); // registro repetido é ignorado
        jogo.moverPara("P5");
        assertEquals(1, gravador.eventos.size());
    }

    @Test public void encerrarTurnoNotificaProximoJogador() {
        jogo.ativarColuna(Coluna.PERSIA);
        jogo.encerrarTurno();
        assertEquals(EventoJogo.TURNO_ENCERRADO, gravador.ultimo());
        assertEquals("AZUL", gravador.ultimoDetalhe());
        assertFalse(jogo.isTurnoAtivo());
    }

    @Test public void cartelaEMelhoriasSaoEstadoObservavel() {
        assertNull(jogo.getCartelaExibida());
        jogo.exibirCartela("AZUL");
        assertEquals(EventoJogo.CARTELA_EXIBIDA, gravador.ultimo());
        assertEquals("AZUL", jogo.getCartelaExibida());
        jogo.exibirMelhorias();
        assertEquals(EventoJogo.MELHORIAS_EXIBIDAS, gravador.ultimo());
        assertTrue(jogo.isMelhoriasExibidas());
        jogo.ocultarMelhorias();
        assertEquals(EventoJogo.MELHORIAS_OCULTADAS, gravador.ultimo());
        jogo.ocultarCartela();
        assertEquals(EventoJogo.CARTELA_OCULTADA, gravador.ultimo());
        assertNull(jogo.getCartelaExibida());
        int total = gravador.eventos.size();
        jogo.ocultarCartela(); // já oculta: nada muda, ninguém é avisado
        jogo.ocultarMelhorias();
        assertEquals(total, gravador.eventos.size());
    }

    @Test public void janelaDeCartasEEscolhaSecretaNotificam() {
        jogo.exibirCartas();
        assertEquals(EventoJogo.CARTAS_EXIBIDAS, gravador.ultimo());
        assertTrue(jogo.isCartasExibidas());
        assertEquals(2, jogo.getOpcoesSecretas("VERMELHO").size());
        jogo.escolherCartaSecreta(0);
        assertEquals(EventoJogo.CARTA_SECRETA_ESCOLHIDA, gravador.ultimo());
        assertNotNull(jogo.getCartaSecreta("VERMELHO"));
        jogo.ocultarCartas();
        assertEquals(EventoJogo.CARTAS_OCULTADAS, gravador.ultimo());
        assertFalse(jogo.isCartasExibidas());
    }

    @Test public void cartelaDeJogadorInexistenteELancaExcecao() {
        try {
            jogo.exibirCartela("PRETO");
            fail("jogador inexistente");
        } catch (IllegalArgumentException esperado) {
            assertNull(jogo.getCartelaExibida());
            assertTrue(gravador.eventos.isEmpty());
        }
    }

    @Test public void cartelaAcompanhaAVezAoEncerrarOTurno() {
        jogo.exibirCartela("VERMELHO");
        jogo.ativarColuna(Coluna.PERSIA);
        jogo.encerrarTurno();
        assertEquals("AZUL", jogo.getCartelaExibida());
        jogo.ocultarCartela();
        jogo.ativarColuna(Coluna.PERSIA);
        jogo.encerrarTurno();
        assertNull("janela oculta continua oculta", jogo.getCartelaExibida());
    }

    @Test public void informacaoDoEventoEValidaSoParaIndicesConhecidos() {
        jogo.ativarColuna(Coluna.PERSIA);
        assertEquals(EventoJogo.COLUNA_ATIVADA, jogo.get(Jogo.INFO_EVENTO));
        assertEquals("PERSIA", jogo.get(Jogo.INFO_DETALHE));
        try {
            jogo.get(2);
            fail("índice inválido");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }

    @Test public void paradasAlcancaveisSoExistemComTurnoAtivo() {
        assertTrue(jogo.getParadasAlcancaveis().isEmpty());
        jogo.ativarColuna(Coluna.PERSIA);
        assertTrue(jogo.getParadasAlcancaveis().contains("P5"));
        assertFalse(jogo.getParadasAlcancaveis().contains(Tabuleiro.KARAKORUM));
        jogo.moverPara("P5");
        jogo.moverPara("P4");
        assertTrue("sem cavalos, nada é alcançável", jogo.getParadasAlcancaveis().isEmpty());
    }

    @Test public void paradaInfoExpoeOConselheiro() {
        ConselheiroInfo fixo = null, quaisquer = null, karakorum = null;
        for (ParadaInfo parada : jogo.getParadas()) {
            if (parada.getId().equals("R4")) fixo = parada.getConselheiro();
            if (parada.getId().equals("C9")) quaisquer = parada.getConselheiro();
            if (parada.getId().equals(Tabuleiro.KARAKORUM)) karakorum = parada.getConselheiro();
            if (parada.getId().equals("R1")) assertNull(parada.getConselheiro());
        }
        assertNotNull(fixo);
        assertFalse(fixo.isQuaisquer());
        assertEquals(Integer.valueOf(2), fixo.getExigenciaFixa().get("PELES"));
        assertEquals(1, fixo.getVotos());
        assertTrue(quaisquer.isQuaisquer());
        assertEquals(3, quaisquer.getQuantidadeQualquer());
        assertNull(quaisquer.getExigenciaFixa());
        assertEquals(2, karakorum.getVotos());
        assertEquals("1 PELES + 1 FERRO + 1 SEDA", karakorum.descricao());
    }

    @Test public void entregaSemTesourosFalhaSemNotificarENaoMudaVotos() {
        jogo.ativarColuna(Coluna.PERSIA);
        int antes = gravador.eventos.size();
        try {
            jogo.entregarTesouros(Tabuleiro.KARAKORUM, new java.util.HashMap<String, Integer>());
            fail("o jogador não tem tesouros");
        } catch (RegraInvalidaException esperado) {
            assertEquals(antes, gravador.eventos.size());
            assertEquals(0, jogo.getVotos("VERMELHO"));
        }
    }

    @Test public void consultasDeKurultaiETiposDeTesouro() {
        assertEquals(14, jogo.getLimiteKurultai());
        assertEquals(0, jogo.getOcupacaoKurultai());
        assertEquals(5, jogo.getTiposTesouro().size());
        Map<String, Integer> votos = jogo.getVotosFinais();
        assertEquals(Arrays.asList("VERMELHO", "AZUL", "VERDE"), new ArrayList<>(votos.keySet()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void numeroDeJogadoresInvalido() { jogo.novaPartida(6, 1L); }
}
