package model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/** Casos de teste da pontuação final (influência regional e desempate). */
public class PontuacaoTest {

    private Partida partida;
    private Jogador vermelho;
    private Jogador azul;
    private Jogador verde;

    @Before
    public void preparar() {
        partida = Cenarios.partida(3);
        vermelho = partida.getJogador(CorJogador.VERMELHO);
        azul = partida.getJogador(CorJogador.AZUL);
        verde = partida.getJogador(CorJogador.VERDE);
    }

    private void darMelhorias(Jogador j, int quantidade) {
        j.adicionarTributo(TipoTributo.MOEDA, 2 * quantidade);
        for (int i = 0; i < quantidade; i++) {
            j.comprarMelhoria(Regiao.RUSSIA);
        }
    }

    private Map<CorJogador, Integer> influencia() {
        return Pontuacao.votosDeInfluencia(partida.getTabuleiro(),
                Arrays.asList(vermelho, azul, verde));
    }

    /**
     * CT-23 Empate no 1º lugar elimina o 2º lugar.
     * Cenário (região RUSSIA): VERMELHO 2 iurtas (2 melhorias), AZUL 2 iurtas (4 melhorias),
     * VERDE 1 iurta (3 melhorias).
     * Esperado (tabela do manual): VERMELHO e AZUL são 1º (2 e 3 votos); VERDE não é 2º -> 0 votos.
     */
    @Test
    public void empateNoPrimeiroLugarAnulaOSegundoLugar() {
        Tabuleiro t = partida.getTabuleiro();
        t.getParada("A").adicionarIurta(CorJogador.VERMELHO);
        t.getParada("C").adicionarIurta(CorJogador.VERMELHO);
        t.getParada("B").adicionarIurta(CorJogador.AZUL);
        t.getParada("B").adicionarIurta(CorJogador.AZUL);
        t.getParada("D").adicionarIurta(CorJogador.VERDE);
        darMelhorias(vermelho, 2);
        darMelhorias(azul, 4);
        darMelhorias(verde, 3);

        Map<CorJogador, Integer> v = influencia();
        assertEquals(2, (int) v.get(CorJogador.VERMELHO));
        assertEquals(3, (int) v.get(CorJogador.AZUL));
        assertEquals(0, (int) v.get(CorJogador.VERDE));
    }

    /**
     * CT-24 Empate no 2º lugar: todos os empatados são 2º.
     * Cenário (RUSSIA): VERMELHO 3 iurtas (1 melhoria); AZUL 1 iurta (3 melhorias);
     * VERDE 1 iurta (2 melhorias).
     * Esperado: VERMELHO 1º = 1 voto; AZUL 2º = 2 votos; VERDE 2º = 1 voto.
     */
    @Test
    public void empateNoSegundoLugarDivideOSegundo() {
        Tabuleiro t = partida.getTabuleiro();
        t.getParada("A").adicionarIurta(CorJogador.VERMELHO);
        t.getParada("C").adicionarIurta(CorJogador.VERMELHO);
        t.getParada("D").adicionarIurta(CorJogador.VERMELHO);
        t.getParada("B").adicionarIurta(CorJogador.AZUL);
        t.getParada("B").adicionarIurta(CorJogador.VERDE);
        darMelhorias(vermelho, 1);
        darMelhorias(azul, 3);
        darMelhorias(verde, 2);

        Map<CorJogador, Integer> v = influencia();
        assertEquals(1, (int) v.get(CorJogador.VERMELHO));
        assertEquals(2, (int) v.get(CorJogador.AZUL));
        assertEquals(1, (int) v.get(CorJogador.VERDE));
    }

    /**
     * CT-27 Primeiro lugar sem melhorias ainda vale 1 voto; segundo sem melhorias vale 0.
     * Cenário (RUSSIA): VERMELHO 2 iurtas e AZUL 1 iurta; nenhum dos dois comprou melhorias.
     * Esperado (tabela do manual, coluna "0"): VERMELHO 1 voto; AZUL 0 votos; VERDE 0.
     */
    @Test
    public void primeiroSemMelhoriasGanhaUmVotoESegundoSemMelhoriasNaoGanha() {
        Tabuleiro t = partida.getTabuleiro();
        t.getParada("A").adicionarIurta(CorJogador.VERMELHO);
        t.getParada("C").adicionarIurta(CorJogador.VERMELHO);
        t.getParada("D").adicionarIurta(CorJogador.AZUL);

        Map<CorJogador, Integer> v = influencia();
        assertEquals(1, (int) v.get(CorJogador.VERMELHO));
        assertEquals(0, (int) v.get(CorJogador.AZUL));
        assertEquals(0, (int) v.get(CorJogador.VERDE));
    }

    /**
     * CT-25 Desempate do vencedor: tesouros e depois proximidade de Karakorum.
     * Cenário: VERMELHO e AZUL empatam em votos (5); VERDE tem 3.
     * Passos: (a) VERMELHO tem 2 tesouros e AZUL 1 -> VERMELHO vence;
     * (b) tesouros iguais, VERMELHO em C (distância 3) e AZUL em A (distância 1) -> AZUL vence;
     * (c) tudo igual -> vitória compartilhada.
     */
    @Test
    public void desempatePorTesourosEDepoisPorDistanciaAKarakorum() {
        Map<CorJogador, Integer> votos = new java.util.HashMap<>();
        votos.put(CorJogador.VERMELHO, 5);
        votos.put(CorJogador.AZUL, 5);
        votos.put(CorJogador.VERDE, 3);
        List<Jogador> todos = Arrays.asList(vermelho, azul, verde);

        vermelho.adicionarTesouro(TipoTesouro.JADE, 2);
        azul.adicionarTesouro(TipoTesouro.JADE, 1);
        assertEquals(Arrays.asList(CorJogador.VERMELHO),
                Pontuacao.vencedores(votos, todos, partida.getTabuleiro()));

        azul.adicionarTesouro(TipoTesouro.JADE, 1); // agora ambos têm 2
        vermelho.setPosicao("C");
        azul.setPosicao("A");
        assertEquals(Arrays.asList(CorJogador.AZUL),
                Pontuacao.vencedores(votos, todos, partida.getTabuleiro()));

        vermelho.setPosicao("A");
        List<CorJogador> compartilhada = Pontuacao.vencedores(votos, todos, partida.getTabuleiro());
        assertEquals(2, compartilhada.size());
    }

    /**
     * CT-26 Distância entre paradas (usada no desempate).
     * Esperado: KARAKORUM->D = 4 passos; A->A = 0.
     */
    @Test
    public void distanciaEntreParadas() {
        assertEquals(4, partida.getTabuleiro().distancia(Tabuleiro.KARAKORUM, "D"));
        assertEquals(0, partida.getTabuleiro().distancia("A", "A"));
    }
}
