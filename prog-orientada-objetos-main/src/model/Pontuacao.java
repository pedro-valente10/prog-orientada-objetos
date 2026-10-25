package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Regras de pontuação final: influência nas regiões e desempate. */
final class Pontuacao {

    /**
     * Tabela impressa no tabuleiro (página 15 do manual). Índice = nº de melhorias na coluna
     * da região (0 a 4; acima de 4 vale o mesmo que 4):
     *   melhorias:   0  1  2  3  4
     *   1º lugar:    1  1  2  2  3
     *   2º lugar:    0  1  1  2  2
     */
    static final int[] VOTOS_PRIMEIRO = { 1, 1, 2, 2, 3 };
    static final int[] VOTOS_SEGUNDO = { 0, 1, 1, 2, 2 };

    private Pontuacao() { }

    private static int valor(int[] tabela, int melhorias) {
        return tabela[Math.min(melhorias, tabela.length - 1)];
    }

    /**
     * Votos de influência regional. Em cada região, quem tem mais iurtas é 1º e o seguinte é 2º.
     * Empate no 1º: todos são 1º e ninguém é 2º. Empate no 2º: todos os empatados são 2º.
     * Quem tem 0 iurtas na região não pontua.
     */
    static Map<CorJogador, Integer> votosDeInfluencia(Tabuleiro tabuleiro, Collection<Jogador> jogadores) {
        Map<CorJogador, Integer> votos = new HashMap<>();
        for (Jogador j : jogadores) {
            votos.put(j.getCor(), 0);
        }
        for (Regiao regiao : Regiao.values()) {
            Map<CorJogador, Integer> contagem = new HashMap<>();
            TreeSet<Integer> valoresDistintos = new TreeSet<>();
            for (Jogador j : jogadores) {
                int n = tabuleiro.contarIurtas(j.getCor(), regiao);
                if (n > 0) {
                    contagem.put(j.getCor(), n);
                    valoresDistintos.add(n);
                }
            }
            if (valoresDistintos.isEmpty()) {
                continue;
            }
            int maior = valoresDistintos.last();
            int segundo = -1;
            int primeiros = 0;
            for (int n : contagem.values()) {
                if (n == maior) {
                    primeiros++;
                }
            }
            if (primeiros == 1 && valoresDistintos.size() > 1) {
                segundo = valoresDistintos.lower(maior);
            }
            for (Jogador j : jogadores) {
                Integer n = contagem.get(j.getCor());
                if (n == null) {
                    continue;
                }
                int melhorias = j.quantidadeMelhorias(regiao);
                if (n == maior) {
                    CorJogador cor = j.getCor();
                    votos.put(cor, votos.getOrDefault(cor, 0) + valor(VOTOS_PRIMEIRO, melhorias));
                } else if (n == segundo) {
                    CorJogador cor = j.getCor();
                    votos.put(cor, votos.getOrDefault(cor, 0) + valor(VOTOS_SEGUNDO, melhorias));
                }
            }
        }
        return votos;
    }

    /**
     * Vencedores: mais votos; empate -> mais tesouros não entregues;
     * empate -> mais perto de Karakorum; se persistir, vitória compartilhada.
     */
    static List<CorJogador> vencedores(Map<CorJogador, Integer> votosTotais,
                                       Collection<Jogador> jogadores, Tabuleiro tabuleiro) {
        List<Jogador> candidatos = new ArrayList<>(jogadores);

        int maxVotos = Integer.MIN_VALUE;
        for (Jogador j : candidatos) {
            maxVotos = Math.max(maxVotos, votosTotais.get(j.getCor()));
        }
        List<Jogador> filtrados = new ArrayList<>();
        for (Jogador j : candidatos) {
            if (votosTotais.get(j.getCor()) == maxVotos) {
                filtrados.add(j);
            }
        }
        candidatos = filtrados;

        if (candidatos.size() > 1) {
            int maxTesouros = -1;
            for (Jogador j : candidatos) {
                maxTesouros = Math.max(maxTesouros, j.totalTesouros());
            }
            filtrados = new ArrayList<>();
            for (Jogador j : candidatos) {
                if (j.totalTesouros() == maxTesouros) {
                    filtrados.add(j);
                }
            }
            candidatos = filtrados;
        }

        if (candidatos.size() > 1) {
            int menorDistancia = Integer.MAX_VALUE;
            for (Jogador j : candidatos) {
                menorDistancia = Math.min(menorDistancia,
                        tabuleiro.distancia(j.getPosicao(), Tabuleiro.KARAKORUM));
            }
            filtrados = new ArrayList<>();
            for (Jogador j : candidatos) {
                if (tabuleiro.distancia(j.getPosicao(), Tabuleiro.KARAKORUM) == menorDistancia) {
                    filtrados.add(j);
                }
            }
            candidatos = filtrados;
        }

        List<CorJogador> resultado = new ArrayList<>();
        for (Jogador j : candidatos) {
            resultado.add(j.getCor());
        }
        return resultado;
    }
}
