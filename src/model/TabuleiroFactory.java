package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Monta um tabuleiro simplificado com 13 paradas, 8 províncias e 9 cidades.
 */
class TabuleiroFactory {

    private TabuleiroFactory() { }

    static String idParada(Regiao regiao, int numero) {
        return regiao.name() + numero;
    }

    static Tabuleiro criarTabuleiroBasico() {
        Tabuleiro t = new Tabuleiro();
        Regiao[] regioes = Regiao.values();

        // Paradas: Karakorum + 4 por região (2 e 4 são duplas)
        t.adicionarParada(new Parada(Tabuleiro.KARAKORUM, null, false, true));
        for (Regiao r : regioes) {
            for (int i = 1; i <= 4; i++) {
                t.adicionarParada(new Parada(idParada(r, i), r, i % 2 == 0, false));
            }
        }
        for (Regiao r : regioes) {
            t.ligar(Tabuleiro.KARAKORUM, idParada(r, 1));
            for (int i = 1; i < 4; i++) {
                t.ligar(idParada(r, i), idParada(r, i + 1));
            }
        }
        for (int k = 0; k < regioes.length; k++) {
            t.ligar(idParada(regioes[k], 4), idParada(regioes[(k + 1) % regioes.length], 4));
        }

        // Províncias em anel: K1 P1 K2 P2 K3 P3 K4 P4 (K = província Khan)
        String[] ids = { "K1", "P1", "K2", "P2", "K3", "P3", "K4", "P4" };
        TipoTributo[] tipos = TipoTributo.values();
        for (int i = 0; i < ids.length; i++) {
            List<String> adj = Arrays.asList(ids[(i + 1) % ids.length], ids[(i + ids.length - 1) % ids.length]);
            Provincia p = new Provincia(ids[i], tipos[i % tipos.length], ids[i].startsWith("K"), adj);
            p.adicionarTributo(); // 1 tributo inicial em cada província
            t.adicionarProvincia(p);
        }
        int n = 0;
        for (Regiao r : regioes) {
            for (int i = 1; i <= 4; i++) {
                Parada p = t.getParada(idParada(r, i));
                p.adicionarProvinciaAdjacente(ids[n % ids.length]);
                p.adicionarProvinciaAdjacente(ids[(n + 1) % ids.length]);
                n++;
            }
        }

        // Conselheiros: 5 pedem 2 tesouros iguais, 1 pede 3 quaisquer; Karakorum pede 3 específicos
        TipoTesouro[] tes = TipoTesouro.values();
        int c = 0;
        for (Regiao r : regioes) {
            for (int i = 2; i <= 4; i += 2) {
                Parada p = t.getParada(idParada(r, i));
                p.setConselheiro(c < tes.length
                        ? Conselheiro.doisIguais("CONS_" + p.getId(), tes[c])
                        : Conselheiro.tresQuaisquer("CONS_" + p.getId()));
                c++;
            }
        }
        t.getParada(Tabuleiro.KARAKORUM)
                .setConselheiro(Conselheiro.tresEspecificos("CONS_KARAKORUM", tes[0], tes[1], tes[2]));

        // Cidades (as 3 primeiras começam reveladas; as demais ficam na pilha)
        String[] nomes = { "SAMARCANDA", "BAGDA", "SARAI", "TABRIZ", "KIEV", "PEQUIM", "XANGAI", "CANTAO", "NOVGOROD" };
        Regiao[] regs = { Regiao.PERSIA, Regiao.PERSIA, Regiao.RUSSIA, Regiao.PERSIA, Regiao.RUSSIA,
                Regiao.CHINA, Regiao.CHINA, Regiao.CHINA, Regiao.RUSSIA };
        Map<Regiao, Integer> contador = new HashMap<>();
        for (int i = 0; i < nomes.length; i++) {
            int j = contador.getOrDefault(regs[i], 0); // 0, 1, 2
            contador.put(regs[i], j + 1);
            Set<String> adj = new HashSet<>(
                    Arrays.asList(idParada(regs[i], j + 1), idParada(regs[i], j + 2)));
            Cidade cidade = new Cidade(nomes[i], regs[i], adj);
            if (i < 3) {
                t.adicionarCidadeRevelada(cidade);
            } else {
                t.adicionarCidadeNaPilha(cidade);
            }
        }
        return t;
    }
}
