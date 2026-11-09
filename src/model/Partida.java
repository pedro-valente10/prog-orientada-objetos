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
    private final Cartas cartas;
    private final List<TipoTesouro> escolhaTesouroBonus = new ArrayList<>();
    private String ultimoBonus;
    private final List<Melhoria> opcoesIniciaisJochi = new ArrayList<>();
    private boolean habilidadeUsadaNoTurno;
    private String cidadeOgedei;
    private boolean descontoOgedei;


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
        this(cores, tabuleiro, semente, false);
    }

    Partida(List<CorJogador> cores, Tabuleiro tabuleiro, long semente, boolean comHerdeiros) {
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
            Herdeiro herdeiro = comHerdeiros ? Herdeiro.daCor(cores.get(i)) : null;
            Jogador j = new Jogador(cores.get(i), herdeiro, Tabuleiro.KARAKORUM);
            // 1º e 2º jogadores começam com 1 moeda; os demais com 2.
            j.adicionarTributo(TipoTributo.MOEDA, i < 2 ? 1 : 2);
            jogadores.put(cores.get(i), j);
            ordem.add(cores.get(i));
            for (String posicao : j.getPosicoes()) tabuleiro.getParada(posicao).adicionarPeao(cores.get(i));
            if (herdeiro == Herdeiro.TOLUI) {
                j.usarIurta();
                tabuleiro.getParada(Tabuleiro.KARAKORUM).adicionarIurta(j.getCor());
            }
            if (herdeiro == Herdeiro.JOCHI) {
                opcoesIniciaisJochi.add(area.retirarTopo());
                opcoesIniciaisJochi.add(area.retirarTopo());
            }
        }
        for (Cidade c : tabuleiro.getCidadesReveladas()) {
            if (c.estaVazia()) {
                c.abastecer(sacarTesouros(TESOUROS_POR_CIDADE));
            }
        }
        this.cartas = new Cartas(ordem, tabuleiro, semente);
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
    List<String> getCartasAbertas() { return cartas.abertas(); }
    List<String> getImagensCartasAbertas() { return cartas.imagensAbertas(); }
    List<String> getImagensOpcoesSecretas(CorJogador cor) { return cartas.imagensOpcoes(cor); }
    String getImagemCartaSecreta(CorJogador cor) { return cartas.imagemEscolhida(cor); }
    List<String> getOpcoesSecretas(CorJogador cor) { return cartas.opcoes(cor); }
    String getCartaSecreta(CorJogador cor) { return cartas.escolhida(cor); }
    int getVotosSecretos(CorJogador cor) { return cartas.votosSecretos(this, cor); }
    int getBonusRecebidos(CorJogador cor) { return cartas.bonusRecebidos(cor); }
    String getUltimoBonus() { return ultimoBonus; }
    Herdeiro getHerdeiro(CorJogador cor) { return getJogador(cor).getHerdeiro(); }
    List<String> getPosicoesPeoes(CorJogador cor) { return getJogador(cor).getPosicoes(); }
    boolean isEscolhaPeaoPendente() { return jogadorAtual().isEscolhaPeaoPendente(); }
    void escolherPeao(int indice) {
        if (turnoAtivo) throw new RegraInvalidaException("Escolha o peão antes da coluna.");
        jogadorAtual().escolherPeao(indice);
    }
    List<Melhoria> getOpcoesIniciaisJochi() {
        return Collections.unmodifiableList(opcoesIniciaisJochi);
    }
    void escolherMelhoriaInicialJochi(int indice) {
        if (opcoesIniciaisJochi.size() != 2 || indice < 0 || indice > 1)
            throw new RegraInvalidaException("Escolha uma das duas melhorias iniciais de Jochi.");
        for (Jogador j : jogadores.values()) if (j.getHerdeiro() == Herdeiro.JOCHI)
            j.getTabuleiroIndividual().colocar(Coluna.BRANCA, opcoesIniciaisJochi.get(indice));
        opcoesIniciaisJochi.clear();
    }
    boolean isHabilidadeUsadaNoTurno() { return habilidadeUsadaNoTurno; }

    List<TipoTesouro> getOpcoesTesouroBonus() {
        return Collections.unmodifiableList(new ArrayList<>(escolhaTesouroBonus));
    }
    void escolherCartaSecreta(CorJogador cor, int indice) { cartas.escolher(cor, indice); }
    void escolherTesouroBonus(int indice) {
        exigirTurnoAtivo();
        if (indice < 0 || indice >= escolhaTesouroBonus.size())
            throw new RegraInvalidaException("Escolha um dos tesouros do bônus.");
        jogadorAtual().adicionarTesouro(escolhaTesouroBonus.get(indice), 1);
        escolhaTesouroBonus.clear();
        verificarCartasAbertas();
    }
    void verificarCartasAbertas() {
        int votos = cartas.verificarAbertas(this, jogadorAtual());
        if (votos > 0) ganharVotos(jogadorDaVez(), votos);
    }

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
        if (jogadorAtual().isEscolhaPeaoPendente())
            throw new RegraInvalidaException("Escolha qual peão de Altani agirá neste turno.");
        if (jogadorAtual().getHerdeiro() == Herdeiro.JOCHI && !opcoesIniciaisJochi.isEmpty())
            throw new RegraInvalidaException("Escolha a melhoria inicial de Jochi.");
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
        habilidadeUsadaNoTurno = false;
        cidadeOgedei = null;
        descontoOgedei = false;
    }

    /** Encerra o turno e passa a vez. Controla a última rodada após o gatilho de fim. */
    void encerrarTurno() {
        exigirTurnoAtivo();
        if (khanPendente)
            throw new RegraInvalidaException("É obrigatório usar o Khan desta coluna.");
        if (!escolhaTesouroBonus.isEmpty())
            throw new RegraInvalidaException("Escolha primeiro o tesouro do bônus.");
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
        jogadorAtual().reiniciarEscolhaPeao();
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
        boolean visitada = paradasVisitadas.contains(paradaId);
        boolean adjacenteTolui = jogador.getHerdeiro() == Herdeiro.TOLUI
                && tabuleiro.getParada(jogador.getPosicao()).getVizinhas().contains(paradaId);
        if (!visitada && !adjacenteTolui)
            throw new RegraInvalidaException("A parada não foi visitada nem é adjacente a Tolui.");
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
            boolean desconto = descontoOgedei && cidadeId.equals(cidadeOgedei)
                    && jaRetirados + i == 1;
            custo += (jaRetirados + i == 0 || desconto) ? 1 : 2;
        }
        Jogador jogador = jogadorAtual();
        int gastoVirtual = Math.min(custo, espadasVirtuais);
        jogador.gastarTributo(TipoTributo.ESPADA, custo - gastoVirtual);
        espadasVirtuais -= gastoVirtual;

        for (int i = 0; i < quantidade; i++) {
            jogador.adicionarTesouro(cidade.retirarTesouro(), 1);
        }
        tesourosRetiradosNoTurno.put(cidadeId, jaRetirados + quantidade);
        if (descontoOgedei && cidadeId.equals(cidadeOgedei) && jaRetirados < 2)
            descontoOgedei = false;

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

    /** Chagatai troca uma ação de cavalo por uma espada física, uma vez no turno. */
    void usarHabilidadeChagatai() {
        exigirTurnoAtivo();
        if (jogadorAtual().getHerdeiro() != Herdeiro.CHAGATAI || habilidadeUsadaNoTurno
                || movimentosRestantes < 1)
            throw new RegraInvalidaException("Habilidade de Chagatai indisponível.");
        movimentosRestantes--;
        jogadorAtual().adicionarTributo(TipoTributo.ESPADA, 1);
        habilidadeUsadaNoTurno = true;
    }

    /** Ogedei paga uma moeda para tomar um tesouro remoto, sem tomar o último. */
    void usarHabilidadeOgedei(String cidadeId) {
        exigirTurnoAtivo();
        if (jogadorAtual().getHerdeiro() != Herdeiro.OGEDEI || habilidadeUsadaNoTurno)
            throw new RegraInvalidaException("Habilidade de Ogedei indisponível.");
        Cidade cidade = tabuleiro.getCidade(cidadeId);
        if (cidade.isConquistada() || cidade.quantidadeTesouros() < 2)
            throw new RegraInvalidaException("Ogedei não pode tomar o último tesouro.");
        int gastoVirtual = Math.min(1, moedasVirtuais);
        jogadorAtual().gastarTributo(TipoTributo.MOEDA, 1 - gastoVirtual);
        moedasVirtuais -= gastoVirtual;
        jogadorAtual().adicionarTesouro(cidade.retirarTesouro(), 1);
        int anteriores = tesourosRetiradosNoTurno.getOrDefault(cidadeId, 0);
        tesourosRetiradosNoTurno.put(cidadeId, anteriores + 1);
        cidadeOgedei = cidadeId;
        descontoOgedei = anteriores == 0;
        habilidadeUsadaNoTurno = true;
    }

    // ---------------------------------------------------------------- melhorias
    /** Compra da oferta ou do topo; a cor da peça define a coluna colorida. */
    Melhoria comprarMelhoria(int vagaArea) { return comprarMelhoriaInterna(null, vagaArea, null); }

    Melhoria comprarMelhoriaJochi(int vagaArea, Coluna destino) {
        if (jogadorAtual().getHerdeiro() != Herdeiro.JOCHI || destino == null)
            throw new RegraInvalidaException("Somente Jochi pode escolher livremente a coluna.");
        return comprarMelhoriaInterna(null, vagaArea, destino);
    }

    /** Sobrecarga legada: valida a coluna informada antes de qualquer débito. */
    void comprarMelhoria(Regiao coluna, int vagaArea) {
        comprarMelhoriaInterna(coluna, vagaArea, null);
    }

    private Melhoria comprarMelhoriaInterna(Regiao coluna, int vagaArea, Coluna destinoEscolhido) {
        exigirTurnoAtivo();
        if (vagaArea < -1 || vagaArea >= AreaMelhorias.VAGAS) {
            throw new RegraInvalidaException("Posição de melhoria inválida.");
        }
        Melhoria peca = vagaArea < 0 ? area.consultarTopo() : area.consultar(vagaArea);
        if (coluna != null && peca.cor() != coluna)
            throw new RegraInvalidaException("A cor da peça não corresponde à coluna.");
        TabuleiroJogador individual = jogadorAtual().getTabuleiroIndividual();
        Coluna destino = destinoEscolhido == null ? Coluna.valueOf(peca.getCor()) : destinoEscolhido;
        if (!individual.temEspaco(destino))
            throw new RegraInvalidaException("A coluna de melhorias está cheia.");
        int gastoVirtual = Math.min(Jogador.CUSTO_MELHORIA, moedasVirtuais);
        jogadorAtual().gastarTributo(TipoTributo.MOEDA, Jogador.CUSTO_MELHORIA - gastoVirtual);
        moedasVirtuais -= gastoVirtual;
        if (vagaArea < 0) area.retirarTopo();
        else area.retirar(vagaArea);
        if (destinoEscolhido == null) individual.colocar(destino, peca);
        else individual.colocarLivre(destino, peca);
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
        boolean primeiraEntrega = conselheiro.getPrimeiroEntregador() == null;
        int votos = conselheiro.entregar(jogadorAtual(), oferta, tesourosVirtuais);
        ultimoBonus = null;
        ganharVotos(jogadorAtual().getCor(), votos);
        if (primeiraEntrega) aplicarBonus(cartas.receberBonus(paradaId, jogadorDaVez()));
    }

    private void aplicarBonus(Cartas.Bonus bonus) {
        if (bonus == null) return;
        ultimoBonus = bonus.descricao;
        switch (bonus) {
            case VOTO: ganharVotos(jogadorDaVez(), 1); break;
            case ESPADAS: jogadorAtual().adicionarTributo(TipoTributo.ESPADA, 2); break;
            case MOEDAS: jogadorAtual().adicionarTributo(TipoTributo.MOEDA, 2); break;
            case IURTAS: jogadorAtual().adicionarTributo(TipoTributo.IURTA, 2); break;
            case MOVIMENTOS: movimentosRestantes += 3; punhosRestantes++; break;
            case PUNHOS: punhosRestantes += 2; break;
            case TESOUROS: escolhaTesouroBonus.addAll(sacarTesouros(3)); break;
            default: throw new IllegalStateException("Bônus desconhecido.");
        }
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
    /** Votos finais: Kurultai, influência regional e carta secreta. */
    Map<CorJogador, Integer> votosFinais() {
        Map<CorJogador, Integer> influencia = Pontuacao.votosDeInfluencia(tabuleiro, jogadores.values());
        Map<CorJogador, Integer> total = new HashMap<>();
        for (CorJogador cor : ordem) {
            total.put(cor, kurultai.getVotos(cor) + influencia.get(cor)
                    + cartas.votosSecretos(this, cor));
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
