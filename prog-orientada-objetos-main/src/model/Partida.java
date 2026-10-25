package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private final AreaMelhorias area = new AreaMelhorias();
    private final Kurultai kurultai;
    private final Deque<TipoTesouro> pilhaTesouros = new ArrayDeque<>();

    // estado do turno
    private int indiceVez;
    private boolean turnoAtivo;
    private int movimentosRestantes;
    private int punhosRestantes;
    private final Set<String> paradasVisitadas = new HashSet<>();
    private final Map<String, Integer> tesourosRetiradosNoTurno = new HashMap<>();

    // estado de fim de jogo
    private boolean fimAcionado;
    private int viradasDeRodadaAposFim;
    private boolean jogoEncerrado;

    Partida(List<CorJogador> cores, Tabuleiro tabuleiro, long semente) {
        this.tabuleiro = tabuleiro;
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
    /**
     * Inicia o turno do jogador da vez. Os números de cavalos (movimentos) e punhos
     * (coletas de tributo) vêm da coluna de ativação escolhida .
     */
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
        turnoAtivo = false;
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
        jogador.gastarTributo(TipoTributo.IURTA, 1);
        jogador.usarIurta();
        parada.adicionarIurta(jogador.getCor());
    }

    // ---------------------------------------------------------------- Khan
    /** Coloca o Khan em uma província Khan ou na área de melhorias (ação obrigatória da coluna). */
    void posicionarKhan(String local) {
        exigirTurnoAtivo();
        if (local == null) {
            throw new RegraInvalidaException("O local do Khan é obrigatório.");
        }
        boolean naArea = Khan.AREA_MELHORIAS.equals(local);
        if (!naArea && !tabuleiro.getProvincia(local).isProvinciaKhan()) {
            throw new RegraInvalidaException(local + " não é uma província Khan.");
        }
        khan.mover(local); // lança exceção se o Khan já está nesse local

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
        jogador.gastarTributo(TipoTributo.ESPADA, custo); // lança exceção se faltar espada

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
    /**
     * Compra uma melhoria por 2 moedas e a coloca na coluna da região.
     * vagaArea >= 0: pega da área de melhorias; vagaArea < 0: pega do topo da pilha.
     */
    void comprarMelhoria(Regiao coluna, int vagaArea) {
        exigirTurnoAtivo();
        if (coluna == null) {
            throw new RegraInvalidaException("A coluna da melhoria é obrigatória.");
        }
        if (vagaArea < -1 || vagaArea >= AreaMelhorias.VAGAS) {
            throw new RegraInvalidaException("Posição de melhoria inválida.");
        }
        if (vagaArea >= 0 && !area.estaOcupada(vagaArea)) {
            throw new RegraInvalidaException("A vaga " + vagaArea + " está vazia.");
        }
        jogadorAtual().comprarMelhoria(coluna);
        if (vagaArea >= 0) {
            area.remover(vagaArea);
        }
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
        int votos = conselheiro.entregar(jogadorAtual(), oferta);
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
