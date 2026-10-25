package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

/**
 * Partida: aplica as regras do jogo sobre o tabuleiro, os jogadores e o estado do turno.
 * É a classe "de regras" do Model; quem a expõe para fora do pacote é a classe pública Jogo.
 *
 * Regras implementadas na 1ª iteração: movimento, coleta de tributos, construção de iurtas,
 * uso do Khan, ataque/conquista de cidades, compra de melhorias, entrega a conselheiros,
 * votos no Kurultai, fim de jogo, influência regional e desempate.
 */
class Partida {

    private static final int TESOUROS_POR_TIPO = 8;
    private static final int TESOUROS_POR_CIDADE = 4;

    private final Tabuleiro tabuleiro;
    private final Map<CorJogador, Jogador> jogadores = new HashMap<>();
    private final List<CorJogador> ordem = new ArrayList<>();
    private final Khan khan = new Khan();
    private final AreaMelhorias area;
    private final Kurultai kurultai;
    private final Deque<TipoTesouro> pilhaTesouros = new ArrayDeque<>();

    // estado do turno
    private int indiceVez;
    private boolean turnoAtivo;
    private int movimentosRestantes;
    private int punhosRestantes;
    private Coluna colunaAtiva;
    private boolean turnoDeColuna;
    private boolean khanPendente;
    private int moedasVirtuais;
    private int espadasVirtuais;
    private int yurtsVirtuais;
    private final Map<TipoTesouro, Integer> tesourosVirtuais = new HashMap<>();
    private final Set<String> paradasVisitadas = new HashSet<>();
    private final Map<String, Integer> tesourosRetiradosNoTurno = new HashMap<>();

    // estado de fim de jogo
    private boolean fimAcionado;
    private int viradasDeRodadaAposFim;
    private boolean jogoEncerrado;

    Partida(List<CorJogador> cores, Tabuleiro tabuleiro, long semente) {
        this.tabuleiro = tabuleiro;
        this.area = new AreaMelhorias(semente ^ 0x5EEDBEEFL);
        this.kurultai = new Kurultai(cores);

        List<TipoTesouro> todos = new ArrayList<>();
        for (TipoTesouro tipo : TipoTesouro.values()) {
            for (int i = 0; i < TESOUROS_POR_TIPO; i++) {
                todos.add(tipo);
            }
        }
        Collections.shuffle(todos, new Random(semente));
        pilhaTesouros.addAll(todos);

        for (int i = 0; i < cores.size(); i++) {
            Jogador j = new Jogador(cores.get(i), Tabuleiro.KARAKORUM);
            // 1º e 2º jogadores começam com 1 moeda; os demais com 2
            j.adicionarTributo(TipoTributo.MOEDA, i < 2 ? 1 : 2);
            jogadores.put(cores.get(i), j);
            ordem.add(cores.get(i));
            tabuleiro.getParada(Tabuleiro.KARAKORUM).adicionarPeao(cores.get(i));
        }
        for (Cidade c : tabuleiro.getCidadesReveladas()) {
            if (c.estaVazia()) {
                c.abastecer(sacarTesouros(TESOUROS_POR_CIDADE));
            }
        }
    }

    // ---------------------------------------------------------------- consultas
    Tabuleiro getTabuleiro() { return tabuleiro; }
    Khan getKhan() { return khan; }
    AreaMelhorias getArea() { return area; }
    Kurultai getKurultai() { return kurultai; }
    Jogador getJogador(CorJogador cor) { return jogadores.get(cor); }
    CorJogador jogadorDaVez() { return ordem.get(indiceVez); }
    List<CorJogador> getOrdem() { return Collections.unmodifiableList(ordem); }
    boolean isFimAcionado() { return fimAcionado; }
    boolean isJogoEncerrado() { return jogoEncerrado; }
    int getMovimentosRestantes() { return movimentosRestantes; }
    int getPunhosRestantes() { return punhosRestantes; }
    boolean isTurnoAtivo() { return turnoAtivo; }
    boolean isKhanPendente() { return khanPendente; }
    Coluna getColunaAtiva() { return colunaAtiva; }
    int getMoedasVirtuais() { return moedasVirtuais; }
    int getEspadasVirtuais() { return espadasVirtuais; }
    int getYurtsVirtuais() { return yurtsVirtuais; }
    int getTesouroVirtual(TipoTesouro tipo) { return tesourosVirtuais.getOrDefault(tipo, 0); }
    Set<String> getParadasVisitadas() { return Collections.unmodifiableSet(paradasVisitadas); }

    private Jogador jogadorAtual() { return jogadores.get(jogadorDaVez()); }

    private void exigirTurnoAtivo() {
        if (jogoEncerrado) {
            throw new RegraInvalidaException("O jogo já terminou.");
        }
        if (!turnoAtivo) {
            throw new RegraInvalidaException("Nenhum turno em andamento.");
        }
    }

    private List<TipoTesouro> sacarTesouros(int quantidade) {
        List<TipoTesouro> sacados = new ArrayList<>();
        for (int i = 0; i < quantidade && !pilhaTesouros.isEmpty(); i++) {
            sacados.add(pilhaTesouros.pollFirst());
        }
        return sacados;
    }

    // ---------------------------------------------------------------- turno
    /** Inicia o turno escolhendo uma das quatro fichas ainda disponíveis. */
    void ativarColuna(Coluna coluna) {
        if (jogoEncerrado || turnoAtivo)
            throw new RegraInvalidaException("Não é possível ativar uma coluna agora.");
        if (coluna == null) throw new RegraInvalidaException("Escolha uma coluna.");
        TabuleiroJogador individual = jogadorAtual().getTabuleiroIndividual();
        if (individual.bonusBrancosPendentes() > 0)
            throw new RegraInvalidaException("Escolha primeiro a melhoria branca pendente.");
        TabuleiroJogador.Acoes acoes = individual.ativar(coluna);
        prepararTurno(acoes.cavalos, acoes.punhos);
        colunaAtiva = coluna;
        turnoDeColuna = true;
        khanPendente = acoes.khan;
        moedasVirtuais = acoes.moedas;
        espadasVirtuais = acoes.espadas;
        yurtsVirtuais = acoes.yurts;
        tesourosVirtuais.clear();
        for (TipoTesouro tipo : TipoTesouro.values())
            if (acoes.tesouros[tipo.ordinal()] > 0)
                tesourosVirtuais.put(tipo, acoes.tesouros[tipo.ordinal()]);
    }

    /** Gancho de compatibilidade para cenários antigos de unidade; não exposto pela fachada. */
    void iniciarTurno(int cavalos, int punhos) {
        if (jogoEncerrado) {
            throw new RegraInvalidaException("O jogo já terminou.");
        }
        if (turnoAtivo) {
            throw new RegraInvalidaException("O turno anterior ainda não foi encerrado.");
        }
        if (cavalos < 0 || punhos < 0) {
            throw new RegraInvalidaException("Movimentos e ações de tributo não podem ser negativos.");
        }
        prepararTurno(cavalos, punhos);
        colunaAtiva = null;
        turnoDeColuna = false;
        khanPendente = false;
        moedasVirtuais = espadasVirtuais = yurtsVirtuais = 0;
        tesourosVirtuais.clear();
    }

    private void prepararTurno(int cavalos, int punhos) {
        turnoAtivo = true;
        movimentosRestantes = cavalos;
        punhosRestantes = punhos;
        paradasVisitadas.clear();
        paradasVisitadas.add(jogadorAtual().getPosicao());
        tesourosRetiradosNoTurno.clear();
    }

    /** Encerra o turno e passa a vez. Controla a última rodada após o gatilho de fim. */
    void encerrarTurno() {
        exigirTurnoAtivo();
        if (khanPendente)
            throw new RegraInvalidaException("É obrigatório usar o Khan desta coluna.");
        if (jogadorAtual().getTabuleiroIndividual().bonusBrancosPendentes() > 0)
            throw new RegraInvalidaException("Escolha a melhoria gratuita da coluna branca.");
        turnoAtivo = false;
        colunaAtiva = null;
        turnoDeColuna = false;
        movimentosRestantes = 0;
        punhosRestantes = 0;
        moedasVirtuais = espadasVirtuais = yurtsVirtuais = 0;
        tesourosVirtuais.clear();
        paradasVisitadas.clear();
        tesourosRetiradosNoTurno.clear();
        indiceVez = (indiceVez + 1) % ordem.size();
        if (fimAcionado && indiceVez == 0) {
            viradasDeRodadaAposFim++;
            // 1ª virada: terminou a rodada atual; 2ª virada: terminou a rodada extra
            if (viradasDeRodadaAposFim == 2) {
                jogoEncerrado = true;
            }
        }
    }

    // ---------------------------------------------------------------- movimento
    /**
     * Move o peão do jogador da vez ao longo de um caminho (lista de paradas, cada uma
     * vizinha da anterior). Cada parada custa 1 movimento, exceto paradas intermediárias
     * com iurta do próprio jogador, que são "saltadas" de graça (e não contam como visitadas).
     * Pode-se passar por paradas ocupadas, mas só terminar em uma parada com vaga.
     */
    void mover(List<String> caminho) {
        exigirTurnoAtivo();
        if (caminho == null || caminho.isEmpty()) {
            throw new RegraInvalidaException("Caminho vazio.");
        }
        Jogador jogador = jogadorAtual();
        CorJogador cor = jogador.getCor();
        String atual = jogador.getPosicao();
        int custo = 0;
        List<String> aVisitar = new ArrayList<>();

        for (int i = 0; i < caminho.size(); i++) {
            String proxima = caminho.get(i);
            Parada p = tabuleiro.getParada(proxima);
            if (!tabuleiro.getParada(atual).getVizinhas().contains(proxima)) {
                throw new RegraInvalidaException("A parada " + proxima + " não é vizinha de " + atual + ".");
            }
            boolean ultima = (i == caminho.size() - 1);
            boolean saltar = !ultima && p.temIurta(cor);
            if (!saltar) {
                custo++;
                aVisitar.add(proxima);
            }
            atual = proxima;
        }
        if (custo > movimentosRestantes) {
            throw new RegraInvalidaException("Movimentos insuficientes.");
        }

        Parada origem = tabuleiro.getParada(jogador.getPosicao());
        Parada destino = tabuleiro.getParada(atual);
        origem.removerPeao(cor);
        if (!destino.podeReceberPeao()) {
            origem.adicionarPeao(cor); // desfaz: estado fica inalterado
            throw new RegraInvalidaException("A parada " + atual + " está ocupada.");
        }
        destino.adicionarPeao(cor);
        jogador.setPosicao(atual);
        movimentosRestantes -= custo;
        paradasVisitadas.addAll(aVisitar);
    }

    /** Menor custo em cavalos, incluindo saltos gratuitos sobre iurtas próprias. */
    List<String> caminhoMaisBarato(String destinoId) {
        exigirTurnoAtivo();
        Parada destino = tabuleiro.getParada(destinoId);
        String origem = jogadorAtual().getPosicao();
        if (origem.equals(destinoId) || !destino.podeReceberPeao())
            throw new RegraInvalidaException("Destino indisponível.");
        Map<String, Integer> custos = new HashMap<>();
        Map<String, Integer> passos = new HashMap<>();
        Map<String, String> anterior = new HashMap<>();
        PriorityQueue<NoRota> fila = new PriorityQueue<>();
        custos.put(origem, 0);
        passos.put(origem, 0);
        fila.add(new NoRota(origem, 0, 0));
        while (!fila.isEmpty()) {
            NoRota atual = fila.poll();
            if (atual.custo != custos.get(atual.id) || atual.passos != passos.get(atual.id))
                continue;
            if (atual.id.equals(destinoId)) break;
            List<String> vizinhas = new ArrayList<>(tabuleiro.getParada(atual.id).getVizinhas());
            Collections.sort(vizinhas);
            for (String vizinha : vizinhas) {
                boolean salto = !vizinha.equals(destinoId)
                        && tabuleiro.getParada(vizinha).temIurta(jogadorAtual().getCor());
                int novoCusto = atual.custo + (salto ? 0 : 1);
                int novosPassos = atual.passos + 1;
                Integer antigo = custos.get(vizinha);
                if (antigo == null || novoCusto < antigo
                        || (novoCusto == antigo && novosPassos < passos.get(vizinha))) {
                    custos.put(vizinha, novoCusto);
                    passos.put(vizinha, novosPassos);
                    anterior.put(vizinha, atual.id);
                    fila.add(new NoRota(vizinha, novoCusto, novosPassos));
                }
            }
        }
        if (!custos.containsKey(destinoId) || custos.get(destinoId) > movimentosRestantes)
            throw new RegraInvalidaException("Movimentos insuficientes para esse destino.");
        List<String> caminho = new ArrayList<>();
        for (String p = destinoId; !p.equals(origem); p = anterior.get(p))
            caminho.add(p);
        Collections.reverse(caminho);
        return caminho;
    }

    void moverPara(String destinoId) { mover(caminhoMaisBarato(destinoId)); }

    private static final class NoRota implements Comparable<NoRota> {
        final String id;
        final int custo, passos;
        NoRota(String id, int custo, int passos) {
            this.id = id;
            this.custo = custo;
            this.passos = passos;
        }
        @Override public int compareTo(NoRota outro) {
            if (custo != outro.custo) return Integer.compare(custo, outro.custo);
            if (passos != outro.passos) return Integer.compare(passos, outro.passos);
            return id.compareTo(outro.id);
        }
    }

    // ---------------------------------------------------------------- tributos e iurtas
    /** Pega 1 tributo de uma província adjacente a uma parada visitada neste turno (gasta 1 punho). */
    void coletarTributo(String provinciaId) {
        exigirTurnoAtivo();
        if (punhosRestantes <= 0) {
            throw new RegraInvalidaException("Sem ações de coletar tributo.");
        }
        Provincia provincia = tabuleiro.getProvincia(provinciaId);
        boolean adjacente = false;
        for (String idParada : paradasVisitadas) {
            if (tabuleiro.getParada(idParada).getProvinciasAdjacentes().contains(provinciaId)) {
                adjacente = true;
                break;
            }
        }
        if (!adjacente) {
            throw new RegraInvalidaException("Província não adjacente às paradas visitadas.");
        }
        if (!provincia.retirarTributo()) {
            throw new RegraInvalidaException("A província está vazia.");
        }
        jogadorAtual().adicionarTributo(provincia.getTipo(), 1);
        punhosRestantes--;
    }

    /** Gasta 1 tributo de iurta e constrói uma iurta em uma parada visitada neste turno. */
    void construirIurta(String paradaId) {
        exigirTurnoAtivo();
        Jogador jogador = jogadorAtual();
        if (!paradasVisitadas.contains(paradaId)) {
            throw new RegraInvalidaException("O jogador não parou em " + paradaId + " neste turno.");
        }
        Parada parada = tabuleiro.getParada(paradaId);
        if (!parada.podeReceberIurta()) {
            throw new RegraInvalidaException("Não há espaço para iurta em " + paradaId + ".");
        }
        if (jogador.getIurtasDisponiveis() == 0) {
            throw new RegraInvalidaException("O jogador não tem mais iurtas.");
        }
        int gastoVirtual = Math.min(1, yurtsVirtuais);
        jogador.gastarTributo(TipoTributo.IURTA, 1 - gastoVirtual);
        yurtsVirtuais -= gastoVirtual;
        jogador.usarIurta();
        parada.adicionarIurta(jogador.getCor());
    }

    // ---------------------------------------------------------------- Khan
    /** Coloca o Khan em uma província Khan ou na área de melhorias (ação obrigatória da coluna). */
    void posicionarKhan(String local) {
        exigirTurnoAtivo();
        if (turnoDeColuna && !khanPendente)
            throw new RegraInvalidaException("A coluna ativada não permite usar o Khan.");
        if (local == null) {
            throw new RegraInvalidaException("O local do Khan é obrigatório.");
        }
        boolean naArea = Khan.AREA_MELHORIAS.equals(local);
        if (!naArea && !tabuleiro.getProvincia(local).isProvinciaKhan()) {
            throw new RegraInvalidaException(local + " não é uma província Khan.");
        }
        khan.mover(local); // lança exceção se o Khan já está nesse local
        khanPendente = false;

        if (naArea) {
            Jogador jogador = jogadorAtual();
            if (area.iconeMoedaVisivel()) {
                jogador.adicionarTributo(TipoTributo.MOEDA, 1);
            }
            if (area.iconeEspadaVisivel()) {
                jogador.adicionarTributo(TipoTributo.ESPADA, 1);
            }
            area.reabastecer();
        } else {
            Provincia provincia = tabuleiro.getProvincia(local);
            provincia.adicionarTributo();
            for (String adj : provincia.getAdjacentesKhan()) {
                tabuleiro.getProvincia(adj).adicionarTributo();
            }
        }
    }

    // ---------------------------------------------------------------- cidades
    /**
     * Saqueia "quantidade" tesouros de uma cidade adjacente a uma parada visitada.
     * O 1º tesouro tirado da cidade no turno custa 1 espada; cada seguinte custa 2.
     * Quem leva o último tesouro conquista a cidade: ganha uma iurta nela e outra cidade é revelada.
     */
    void atacarCidade(String cidadeId, int quantidade) {
        exigirTurnoAtivo();
        if (quantidade <= 0) {
            throw new RegraInvalidaException("Quantidade inválida.");
        }
        Cidade cidade = tabuleiro.getCidade(cidadeId);
        if (cidade.isConquistada()) {
            throw new RegraInvalidaException("A cidade já foi conquistada.");
        }
        if (quantidade > cidade.quantidadeTesouros()) {
            throw new RegraInvalidaException("A cidade não tem tantos tesouros.");
        }
        boolean adjacente = false;
        for (String idParada : cidade.getParadasAdjacentes()) {
            if (paradasVisitadas.contains(idParada)) {
                adjacente = true;
                break;
            }
        }
        if (!adjacente) {
            throw new RegraInvalidaException("O jogador não parou ao lado da cidade.");
        }

        int jaRetirados = tesourosRetiradosNoTurno.getOrDefault(cidadeId, 0);
        int custo = 0;
        for (int i = 0; i < quantidade; i++) {
            custo += (jaRetirados + i == 0) ? 1 : 2;
        }
        Jogador jogador = jogadorAtual();
        int gastoVirtual = Math.min(custo, espadasVirtuais);
        jogador.gastarTributo(TipoTributo.ESPADA, custo - gastoVirtual);
        espadasVirtuais -= gastoVirtual;

        for (int i = 0; i < quantidade; i++) {
            jogador.adicionarTesouro(cidade.retirarTesouro(), 1);
        }
        tesourosRetiradosNoTurno.put(cidadeId, jaRetirados + quantidade);

        if (cidade.estaVazia()) {
            conquistar(cidade, jogador);
        }
    }

    private void conquistar(Cidade cidade, Jogador jogador) {
        if (jogador.getIurtasDisponiveis() > 0) {
            jogador.usarIurta();
            cidade.marcarConquistada(jogador.getCor());
        } else {
            cidade.marcarConquistada(null);
        }
        Cidade nova = tabuleiro.revelarProximaCidade();
        if (nova != null && nova.estaVazia()) {
            nova.abastecer(sacarTesouros(TESOUROS_POR_CIDADE));
        }
        verificarGatilhoDeFim();
    }

    // ---------------------------------------------------------------- melhorias
    /** Compra da oferta ou do topo; a cor da peça define a coluna colorida. */
    Melhoria comprarMelhoria(int vagaArea) { return comprarMelhoriaInterna(null, vagaArea); }

    /** Sobrecarga legada: valida a coluna informada antes de qualquer débito. */
    void comprarMelhoria(Regiao coluna, int vagaArea) {
        comprarMelhoriaInterna(coluna, vagaArea);
    }

    private Melhoria comprarMelhoriaInterna(Regiao coluna, int vagaArea) {
        exigirTurnoAtivo();
        if (vagaArea < -1 || vagaArea >= AreaMelhorias.VAGAS) {
            throw new RegraInvalidaException("Posição de melhoria inválida.");
        }
        Melhoria peca = vagaArea < 0 ? area.consultarTopo() : area.consultar(vagaArea);
        if (coluna != null && peca.cor() != coluna)
            throw new RegraInvalidaException("A cor da peça não corresponde à coluna.");
        TabuleiroJogador individual = jogadorAtual().getTabuleiroIndividual();
        Coluna destino = Coluna.valueOf(peca.getCor());
        if (!individual.temEspaco(destino))
            throw new RegraInvalidaException("A coluna de melhorias está cheia.");
        int gastoVirtual = Math.min(Jogador.CUSTO_MELHORIA, moedasVirtuais);
        jogadorAtual().gastarTributo(TipoTributo.MOEDA, Jogador.CUSTO_MELHORIA - gastoVirtual);
        moedasVirtuais -= gastoVirtual;
        if (vagaArea < 0) area.retirarTopo();
        else area.retirar(vagaArea);
        individual.colocar(destino, peca);
        return peca;
    }

    /** Uma linha colorida completa concede uma peça gratuita para a coluna branca. */
    Melhoria receberBonusBranco(int vagaArea) {
        exigirTurnoAtivo();
        TabuleiroJogador individual = jogadorAtual().getTabuleiroIndividual();
        if (individual.bonusBrancosPendentes() == 0)
            throw new RegraInvalidaException("Não há bônus de linha pendente.");
        if (!individual.temEspaco(Coluna.BRANCA))
            throw new RegraInvalidaException("A coluna branca está cheia.");
        if (vagaArea < -1 || vagaArea >= AreaMelhorias.VAGAS)
            throw new RegraInvalidaException("Posição de melhoria inválida.");
        Melhoria peca = vagaArea < 0 ? area.consultarTopo() : area.consultar(vagaArea);
        if (vagaArea < 0) area.retirarTopo();
        else area.retirar(vagaArea);
        individual.colocarBonusBranco(peca);
        return peca;
    }

    // ---------------------------------------------------------------- conselheiros e votos
    /** Entrega tesouros ao conselheiro de uma parada onde o jogador parou neste turno. */
    void entregarTesouros(String paradaId, Map<TipoTesouro, Integer> oferta) {
        exigirTurnoAtivo();
        if (!paradasVisitadas.contains(paradaId)) {
            throw new RegraInvalidaException("O jogador não parou em " + paradaId + " neste turno.");
        }
        Conselheiro conselheiro = tabuleiro.getParada(paradaId).getConselheiro();
        if (conselheiro == null) {
            throw new RegraInvalidaException("Não há conselheiro em " + paradaId + ".");
        }
        int votos = conselheiro.entregar(jogadorAtual(), oferta, tesourosVirtuais);
        ganharVotos(jogadorAtual().getCor(), votos);
    }

    private void ganharVotos(CorJogador cor, int quantidade) {
        kurultai.adicionarVotos(cor, quantidade);
        verificarGatilhoDeFim();
    }

    private void verificarGatilhoDeFim() {
        if (!fimAcionado && (kurultai.estaCheio() || tabuleiro.todasCidadesConquistadas())) {
            fimAcionado = true;
        }
    }

    // ---------------------------------------------------------------- resultado
    /** Votos finais: votos do Kurultai somados aos votos de influência regional. */
    Map<CorJogador, Integer> votosFinais() {
        Map<CorJogador, Integer> influencia = Pontuacao.votosDeInfluencia(tabuleiro, jogadores.values());
        Map<CorJogador, Integer> total = new HashMap<>();
        for (CorJogador cor : ordem) {
            total.put(cor, kurultai.getVotos(cor) + influencia.get(cor));
        }
        return total;
    }

    List<CorJogador> vencedores() {
        if (!jogoEncerrado) {
            throw new RegraInvalidaException("O jogo ainda não terminou.");
        }
        return Pontuacao.vencedores(votosFinais(), jogadores.values(), tabuleiro);
    }
}
