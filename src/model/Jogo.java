package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** API pública do componente Model. */
public class Jogo {

    private final Partida partida;

    public Jogo(int numeroJogadores) {
        this(numeroJogadores, System.nanoTime());
    }

    /** A semente torna o embaralhamento reprodutível (útil em testes). */
    public Jogo(int numeroJogadores, long semente) {
        if (numeroJogadores < 2 || numeroJogadores > 5) {
            throw new IllegalArgumentException("O jogo é para 2 a 5 jogadores.");
        }
        List<CorJogador> cores = new ArrayList<>();
        for (int i = 0; i < numeroJogadores; i++) {
            cores.add(CorJogador.values()[i]);
        }
        this.partida = new Partida(cores, TabuleiroFactory.criarTabuleiroBasico(semente), semente);
    }

    /** Cores dos jogadores, na ordem dos turnos. */
    public List<String> getJogadores() {
        List<String> nomes = new ArrayList<>();
        for (CorJogador c : partida.getOrdem()) {
            nomes.add(c.name());
        }
        return Collections.unmodifiableList(nomes);
    }

    // ---- turno ----
    public String getJogadorDaVez() { return partida.jogadorDaVez().name(); }
    public void ativarColuna(Coluna coluna) { partida.ativarColuna(coluna); }
    public void encerrarTurno() { partida.encerrarTurno(); }
    public boolean isTurnoAtivo() { return partida.isTurnoAtivo(); }
    public boolean isKhanPendente() { return partida.isKhanPendente(); }
    public Coluna getColunaAtiva() { return partida.getColunaAtiva(); }
    public int getMovimentosRestantes() { return partida.getMovimentosRestantes(); }
    public int getPunhosRestantes() { return partida.getPunhosRestantes(); }
    public int getMoedasVirtuais() { return partida.getMoedasVirtuais(); }
    public int getEspadasVirtuais() { return partida.getEspadasVirtuais(); }
    public int getYurtsVirtuais() { return partida.getYurtsVirtuais(); }
    public int getTesouroVirtual(String tipo) {
        return partida.getTesouroVirtual(TipoTesouro.valueOf(tipo));
    }

    // ---- ações ----
    public void mover(List<String> caminho) { partida.mover(caminho); }
    public void moverPara(String destino) { partida.moverPara(destino); }
    public List<String> getCaminhoMaisBarato(String destino) {
        return Collections.unmodifiableList(partida.caminhoMaisBarato(destino));
    }
    public void coletarTributo(String provinciaId) { partida.coletarTributo(provinciaId); }
    public void construirIurta(String paradaId) { partida.construirIurta(paradaId); }
    public void posicionarKhan(String local) { partida.posicionarKhan(local); }
    public void atacarCidade(String cidadeId, int quantidade) { partida.atacarCidade(cidadeId, quantidade); }
    public void comprarMelhoria(String coluna, int vagaArea) {
        partida.comprarMelhoria(Regiao.valueOf(coluna), vagaArea);
    }
    public Melhoria comprarMelhoria(int vagaArea) { return partida.comprarMelhoria(vagaArea); }
    public Melhoria receberBonusBranco(int vagaArea) { return partida.receberBonusBranco(vagaArea); }

    /** oferta: nome do tipo de tesouro -> quantidade; exigências fixas também são validadas. */
    public void entregarTesouros(String paradaId, Map<String, Integer> oferta) {
        Map<TipoTesouro, Integer> convertida = new HashMap<>();
        if (oferta != null) {
            for (Map.Entry<String, Integer> e : oferta.entrySet()) {
                convertida.put(TipoTesouro.valueOf(e.getKey()), e.getValue());
            }
        }
        partida.entregarTesouros(paradaId, convertida);
    }

    // ---- consultas ----
    public String getPosicao(String cor) { return partida.getJogador(CorJogador.valueOf(cor)).getPosicao(); }
    public int getVotos(String cor) { return partida.getKurultai().getVotos(CorJogador.valueOf(cor)); }
    public int getTributos(String cor, String tipo) {
        return partida.getJogador(CorJogador.valueOf(cor)).quantidadeTributo(TipoTributo.valueOf(tipo));
    }
    public int getTesouros(String cor, String tipo) {
        return partida.getJogador(CorJogador.valueOf(cor)).quantidadeTesouro(TipoTesouro.valueOf(tipo));
    }
    public Set<String> getParadasVizinhas(String paradaId) {
        return partida.getTabuleiro().getParada(paradaId).getVizinhas();
    }
    public List<ParadaInfo> getParadas() {
        List<ParadaInfo> resultado = new ArrayList<>();
        for (Parada p : partida.getTabuleiro().getParadas()) resultado.add(new ParadaInfo(p));
        Collections.sort(resultado, new java.util.Comparator<ParadaInfo>() {
            @Override public int compare(ParadaInfo a, ParadaInfo b) {
                return a.getId().compareTo(b.getId());
            }
        });
        return Collections.unmodifiableList(resultado);
    }
    public List<Melhoria> getOfertaMelhorias() { return partida.getArea().oferta(); }
    public int getMelhoriasNaPilha() { return partida.getArea().quantidadeNaPilha(); }
    public List<Melhoria> getMelhorias(String cor, Coluna coluna) {
        return partida.getJogador(CorJogador.valueOf(cor)).getTabuleiroIndividual().melhorias(coluna);
    }
    public boolean isColunaDisponivel(String cor, Coluna coluna) {
        return partida.getJogador(CorJogador.valueOf(cor)).getTabuleiroIndividual().disponivel(coluna);
    }
    public int getBonusBrancosPendentes(String cor) {
        return partida.getJogador(CorJogador.valueOf(cor)).getTabuleiroIndividual().bonusBrancosPendentes();
    }
    public int getIurtasDisponiveis(String cor) {
        return partida.getJogador(CorJogador.valueOf(cor)).getIurtasDisponiveis();
    }
    public int getCavalosIniciais(Coluna coluna) { return TabuleiroJogador.cavalosIniciais(coluna); }
    public int getPunhosIniciais(Coluna coluna) { return TabuleiroJogador.punhosIniciais(coluna); }
    public boolean isKhanInicial(Coluna coluna) { return TabuleiroJogador.khanInicial(coluna); }
    public List<String> getProvincias() {
        List<String> ids = new ArrayList<>();
        for (Provincia p : partida.getTabuleiro().getProvincias()) ids.add(p.getId());
        Collections.sort(ids);
        return Collections.unmodifiableList(ids);
    }
    public int getTributosDaProvincia(String id) {
        return partida.getTabuleiro().getProvincia(id).getTributos();
    }
    public String getTipoDaProvincia(String id) {
        return partida.getTabuleiro().getProvincia(id).getTipo().name();
    }
    public List<String> getCidadesReveladas() {
        List<String> ids = new ArrayList<>();
        for (Cidade c : partida.getTabuleiro().getCidadesReveladas())
            if (!c.isConquistada()) ids.add(c.getId());
        Collections.sort(ids);
        return Collections.unmodifiableList(ids);
    }
    public int getTesourosDaCidade(String id) {
        return partida.getTabuleiro().getCidade(id).quantidadeTesouros();
    }
    public Set<String> getParadasVisitadas() { return partida.getParadasVisitadas(); }
    public String getLocalDoKhan() { return partida.getKhan().getLocal(); }
    public boolean isFimAcionado() { return partida.isFimAcionado(); }
    public boolean isJogoEncerrado() { return partida.isJogoEncerrado(); }

    public List<String> getVencedores() {
        List<String> nomes = new ArrayList<>();
        for (CorJogador c : partida.vencedores()) {
            nomes.add(c.name());
        }
        return Collections.unmodifiableList(nomes);
    }
}
