package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * API pública do componente Model.
 *
 * Papéis desta classe na 3ª iteração:
 *  - Singleton: existe uma única instância, obtida por {@link #getInstance()};
 *  - Façade: é a única porta de entrada do Model para o Controller e para a View;
 *  - Observado (padrão Observer, Capítulo 17): avisa os {@link ObservadorIF} registrados a cada
 *    mudança de estado da partida (peão movido, cartela exibida, melhorias exibidas etc.).
 */
public class Jogo implements ObservadoIF {

    /** Índice de {@link #get(int)} que devolve o {@link EventoJogo} da última mudança. */
    public static final int INFO_EVENTO = 0;
    /** Índice de {@link #get(int)} que devolve um texto de apoio (parada, cor, coluna...) ou null. */
    public static final int INFO_DETALHE = 1;

    private static Jogo instancia;

    private Partida partida;
    private final List<ObservadorIF> observadores = new ArrayList<>();
    private EventoJogo ultimoEvento;
    private String ultimoDetalhe;
    // estado de exibição (janelas), também comunicado pelo Observer
    private String cartelaExibida;
    private boolean melhoriasExibidas;
    private boolean cartasExibidas;

    private Jogo() { }

    /** Única instância do jogo. A partida em si é criada por {@link #novaPartida(int)}. */
    public static synchronized Jogo getInstance() {
        if (instancia == null) {
            instancia = new Jogo();
        }
        return instancia;
    }

    /** Começa uma nova partida; os observadores já registrados continuam registrados. */
    public void novaPartida(int numeroJogadores) {
        novaPartida(numeroJogadores, System.nanoTime());
    }

    /** A semente torna o embaralhamento reprodutível (útil em testes). */
    public void novaPartida(int numeroJogadores, long semente) {
        if (numeroJogadores < 2 || numeroJogadores > 5) {
            throw new IllegalArgumentException("O jogo é para 2 a 5 jogadores.");
        }
        List<CorJogador> cores = new ArrayList<>();
        for (int i = 0; i < numeroJogadores; i++) {
            cores.add(CorJogador.values()[i]);
        }
        this.partida = new Partida(cores, TabuleiroFactory.criarTabuleiroBasico(semente), semente);
        this.cartelaExibida = null;
        this.melhoriasExibidas = false;
        this.cartasExibidas = false;
        notificar(EventoJogo.PARTIDA_INICIADA, null);
    }

    private Partida p() {
        if (partida == null) {
            throw new IllegalStateException("Nenhuma partida em andamento: chame novaPartida().");
        }
        return partida;
    }

    // ---- Observer ----
    @Override public void add(ObservadorIF observador) {
        if (observador != null && !observadores.contains(observador)) observadores.add(observador);
    }

    @Override public void remove(ObservadorIF observador) { observadores.remove(observador); }

    @Override public Object get(int i) {
        switch (i) {
            case INFO_EVENTO: return ultimoEvento;
            case INFO_DETALHE: return ultimoDetalhe;
            default: throw new IllegalArgumentException("Índice de informação inválido: " + i);
        }
    }

    private void concluirAcao(EventoJogo evento, String detalhe) {
        p().verificarCartasAbertas();
        notificar(evento, detalhe);
    }

    private void notificar(EventoJogo evento, String detalhe) {
        ultimoEvento = evento;
        ultimoDetalhe = detalhe;
        // cópia: um observador pode se remover durante a notificação
        for (ObservadorIF observador : new ArrayList<>(observadores)) {
            observador.notify(this);
        }
    }

    /** Cores dos jogadores, na ordem dos turnos. */
    public List<String> getJogadores() {
        List<String> nomes = new ArrayList<>();
        for (CorJogador c : p().getOrdem()) {
            nomes.add(c.name());
        }
        return Collections.unmodifiableList(nomes);
    }

    // ---- turno ----
    public String getJogadorDaVez() { return p().jogadorDaVez().name(); }
    public void ativarColuna(Coluna coluna) {
        p().ativarColuna(coluna);
        notificar(EventoJogo.COLUNA_ATIVADA, coluna.name());
    }

    public void encerrarTurno() {
        p().encerrarTurno();
        if (cartelaExibida != null) cartelaExibida = getJogadorDaVez(); // a cartela acompanha a vez
        notificar(EventoJogo.TURNO_ENCERRADO, getJogadorDaVez());
        if (p().isJogoEncerrado()) notificar(EventoJogo.JOGO_ENCERRADO, null);
    }

    public boolean isTurnoAtivo() { return p().isTurnoAtivo(); }
    public boolean isKhanPendente() { return p().isKhanPendente(); }
    public Coluna getColunaAtiva() { return p().getColunaAtiva(); }
    public int getMovimentosRestantes() { return p().getMovimentosRestantes(); }
    public int getPunhosRestantes() { return p().getPunhosRestantes(); }
    public int getMoedasVirtuais() { return p().getMoedasVirtuais(); }
    public int getEspadasVirtuais() { return p().getEspadasVirtuais(); }
    public int getYurtsVirtuais() { return p().getYurtsVirtuais(); }
    public int getTesouroVirtual(String tipo) {
        return p().getTesouroVirtual(TipoTesouro.valueOf(tipo));
    }

    // ---- ações ----
    public void mover(List<String> caminho) {
        p().mover(caminho);
        notificar(EventoJogo.PEAO_MOVIDO, caminho.get(caminho.size() - 1));
    }

    public void moverPara(String destino) {
        p().moverPara(destino);
        notificar(EventoJogo.PEAO_MOVIDO, destino);
    }

    public List<String> getCaminhoMaisBarato(String destino) {
        return Collections.unmodifiableList(p().caminhoMaisBarato(destino));
    }
    public void coletarTributo(String provinciaId) {
        p().coletarTributo(provinciaId);
        concluirAcao(EventoJogo.TRIBUTO_COLETADO, provinciaId);
    }

    public void construirIurta(String paradaId) {
        p().construirIurta(paradaId);
        concluirAcao(EventoJogo.IURTA_CONSTRUIDA, paradaId);
    }

    public void posicionarKhan(String local) {
        p().posicionarKhan(local);
        concluirAcao(EventoJogo.KHAN_POSICIONADO, local);
    }

    public void atacarCidade(String cidadeId, int quantidade) {
        p().atacarCidade(cidadeId, quantidade);
        concluirAcao(EventoJogo.CIDADE_ATACADA, cidadeId);
    }

    public void comprarMelhoria(String coluna, int vagaArea) {
        p().comprarMelhoria(Regiao.valueOf(coluna), vagaArea);
        concluirAcao(EventoJogo.MELHORIA_ADQUIRIDA, null);
    }

    public Melhoria comprarMelhoria(int vagaArea) {
        Melhoria peca = p().comprarMelhoria(vagaArea);
        concluirAcao(EventoJogo.MELHORIA_ADQUIRIDA, String.valueOf(peca.getId()));
        return peca;
    }

    public Melhoria receberBonusBranco(int vagaArea) {
        Melhoria peca = p().receberBonusBranco(vagaArea);
        concluirAcao(EventoJogo.MELHORIA_ADQUIRIDA, String.valueOf(peca.getId()));
        return peca;
    }

    /** oferta: nome do tipo de tesouro -> quantidade; exigências fixas também são validadas. */
    public void entregarTesouros(String paradaId, Map<String, Integer> oferta) {
        Map<TipoTesouro, Integer> convertida = new HashMap<>();
        if (oferta != null) {
            for (Map.Entry<String, Integer> e : oferta.entrySet()) {
                convertida.put(TipoTesouro.valueOf(e.getKey()), e.getValue());
            }
        }
        p().entregarTesouros(paradaId, convertida);
        concluirAcao(EventoJogo.TESOUROS_ENTREGUES, paradaId);
    }

    // ---- cartas, bônus e consultas ----
    public List<String> getCartasAbertas() { return p().getCartasAbertas(); }
    public List<String> getOpcoesSecretas(String cor) {
        return p().getOpcoesSecretas(CorJogador.valueOf(cor));
    }
    public String getCartaSecreta(String cor) {
        return p().getCartaSecreta(CorJogador.valueOf(cor));
    }
    public int getVotosSecretos(String cor) {
        return p().getVotosSecretos(CorJogador.valueOf(cor));
    }
    public int getBonusRecebidos(String cor) {
        return p().getBonusRecebidos(CorJogador.valueOf(cor));
    }
    public String getUltimoBonus() { return p().getUltimoBonus(); }
    public List<String> getOpcoesTesouroBonus() {
        List<String> resultado = new ArrayList<>();
        for (TipoTesouro tipo : p().getOpcoesTesouroBonus()) resultado.add(tipo.name());
        return Collections.unmodifiableList(resultado);
    }
    public void escolherCartaSecreta(int indice) {
        String cor = getJogadorDaVez();
        p().escolherCartaSecreta(CorJogador.valueOf(cor), indice);
        notificar(EventoJogo.CARTA_SECRETA_ESCOLHIDA, cor);
    }
    public void escolherTesouroBonus(int indice) {
        p().escolherTesouroBonus(indice);
        notificar(EventoJogo.TESOURO_BONUS_ESCOLHIDO, getJogadorDaVez());
    }

    // ---- consultas ----
    public String getPosicao(String cor) { return p().getJogador(CorJogador.valueOf(cor)).getPosicao(); }
    public int getVotos(String cor) { return p().getKurultai().getVotos(CorJogador.valueOf(cor)); }
    public int getTributos(String cor, String tipo) {
        return p().getJogador(CorJogador.valueOf(cor)).quantidadeTributo(TipoTributo.valueOf(tipo));
    }
    public int getTesouros(String cor, String tipo) {
        return p().getJogador(CorJogador.valueOf(cor)).quantidadeTesouro(TipoTesouro.valueOf(tipo));
    }
    public Set<String> getParadasVizinhas(String paradaId) {
        return p().getTabuleiro().getParada(paradaId).getVizinhas();
    }
    public List<ParadaInfo> getParadas() {
        List<ParadaInfo> resultado = new ArrayList<>();
        for (Parada p : p().getTabuleiro().getParadas()) resultado.add(new ParadaInfo(p));
        Collections.sort(resultado, new java.util.Comparator<ParadaInfo>() {
            @Override public int compare(ParadaInfo a, ParadaInfo b) {
                return a.getId().compareTo(b.getId());
            }
        });
        return Collections.unmodifiableList(resultado);
    }
    public List<Melhoria> getOfertaMelhorias() { return p().getArea().oferta(); }
    public int getMelhoriasNaPilha() { return p().getArea().quantidadeNaPilha(); }
    public List<Melhoria> getMelhorias(String cor, Coluna coluna) {
        return p().getJogador(CorJogador.valueOf(cor)).getTabuleiroIndividual().melhorias(coluna);
    }
    public boolean isColunaDisponivel(String cor, Coluna coluna) {
        return p().getJogador(CorJogador.valueOf(cor)).getTabuleiroIndividual().disponivel(coluna);
    }
    public int getBonusBrancosPendentes(String cor) {
        return p().getJogador(CorJogador.valueOf(cor)).getTabuleiroIndividual().bonusBrancosPendentes();
    }
    public int getIurtasDisponiveis(String cor) {
        return p().getJogador(CorJogador.valueOf(cor)).getIurtasDisponiveis();
    }
    public int getCavalosIniciais(Coluna coluna) { return TabuleiroJogador.cavalosIniciais(coluna); }
    public int getPunhosIniciais(Coluna coluna) { return TabuleiroJogador.punhosIniciais(coluna); }
    public boolean isKhanInicial(Coluna coluna) { return TabuleiroJogador.khanInicial(coluna); }
    public List<String> getProvincias() {
        List<String> ids = new ArrayList<>();
        for (Provincia p : p().getTabuleiro().getProvincias()) ids.add(p.getId());
        Collections.sort(ids);
        return Collections.unmodifiableList(ids);
    }
    public int getTributosDaProvincia(String id) {
        return p().getTabuleiro().getProvincia(id).getTributos();
    }
    public String getTipoDaProvincia(String id) {
        return p().getTabuleiro().getProvincia(id).getTipo().name();
    }
    public List<String> getCidadesReveladas() {
        List<String> ids = new ArrayList<>();
        for (Cidade c : p().getTabuleiro().getCidadesReveladas())
            if (!c.isConquistada()) ids.add(c.getId());
        Collections.sort(ids);
        return Collections.unmodifiableList(ids);
    }
    public int getTesourosDaCidade(String id) {
        return p().getTabuleiro().getCidade(id).quantidadeTesouros();
    }
    public Set<String> getParadasVisitadas() { return p().getParadasVisitadas(); }
    public String getLocalDoKhan() { return p().getKhan().getLocal(); }
    public boolean isFimAcionado() { return p().isFimAcionado(); }
    public boolean isJogoEncerrado() { return p().isJogoEncerrado(); }

    /** Paradas que o peão da vez consegue alcançar agora (vazio fora de um turno ativo). */
    public Set<String> getParadasAlcancaveis() {
        Set<String> alcancaveis = new HashSet<>();
        if (!p().isTurnoAtivo() || p().isJogoEncerrado()) return alcancaveis;
        for (Parada parada : p().getTabuleiro().getParadas()) {
            try {
                p().caminhoMaisBarato(parada.getId());
                alcancaveis.add(parada.getId());
            } catch (RegraInvalidaException ignorada) {
                // destino sem vaga ou fora do orçamento de cavalos
            }
        }
        return Collections.unmodifiableSet(alcancaveis);
    }

    /** Nomes dos tipos de tesouro, na ordem canônica. */
    public List<String> getTiposTesouro() {
        List<String> nomes = new ArrayList<>();
        for (TipoTesouro tipo : TipoTesouro.values()) nomes.add(tipo.name());
        return Collections.unmodifiableList(nomes);
    }

    public int getOcupacaoKurultai() { return p().getKurultai().getOcupacao(); }
    public int getLimiteKurultai() { return p().getKurultai().getLimite(); }

    /** Votos do Kurultai, influência regional e carta secreta, na ordem dos turnos. */
    public Map<String, Integer> getVotosFinais() {
        Map<CorJogador, Integer> total = p().votosFinais();
        Map<String, Integer> resultado = new LinkedHashMap<>();
        for (CorJogador cor : p().getOrdem()) resultado.put(cor.name(), total.get(cor));
        return Collections.unmodifiableMap(resultado);
    }

    // ---- estado de exibição (janelas da cartela e das melhorias) ----
    /** Passa a exibir a cartela (tabuleiro individual com as colunas) do jogador indicado. */
    public void exibirCartela(String cor) {
        if (!getJogadores().contains(cor)) throw new IllegalArgumentException("Jogador inexistente: " + cor);
        cartelaExibida = cor;
        notificar(EventoJogo.CARTELA_EXIBIDA, cor);
    }

    public void ocultarCartela() {
        if (cartelaExibida == null) return;
        cartelaExibida = null;
        notificar(EventoJogo.CARTELA_OCULTADA, null);
    }

    /** Cor do jogador cuja cartela está na tela, ou null se a janela está oculta. */
    public String getCartelaExibida() { return cartelaExibida; }

    public void exibirMelhorias() {
        melhoriasExibidas = true;
        notificar(EventoJogo.MELHORIAS_EXIBIDAS, null);
    }

    public void ocultarMelhorias() {
        if (!melhoriasExibidas) return;
        melhoriasExibidas = false;
        notificar(EventoJogo.MELHORIAS_OCULTADAS, null);
    }

    public boolean isMelhoriasExibidas() { return melhoriasExibidas; }

    public void exibirCartas() {
        cartasExibidas = true;
        notificar(EventoJogo.CARTAS_EXIBIDAS, null);
    }
    public void ocultarCartas() {
        if (!cartasExibidas) return;
        cartasExibidas = false;
        notificar(EventoJogo.CARTAS_OCULTADAS, null);
    }
    public boolean isCartasExibidas() { return cartasExibidas; }

    public List<String> getVencedores() {
        List<String> nomes = new ArrayList<>();
        for (CorJogador c : p().vencedores()) {
            nomes.add(c.name());
        }
        return Collections.unmodifiableList(nomes);
    }
}
