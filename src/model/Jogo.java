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
        this.partida = new Partida(cores, TabuleiroFactory.criarTabuleiroBasico(), semente);
    }

    // ---- turno ----
    public String getJogadorDaVez() { return partida.jogadorDaVez().name(); }
    public void iniciarTurno(int cavalos, int punhos) { partida.iniciarTurno(cavalos, punhos); }
    public void encerrarTurno() { partida.encerrarTurno(); }

    // ---- ações ----
    public void mover(List<String> caminho) { partida.mover(caminho); }
    public void coletarTributo(String provinciaId) { partida.coletarTributo(provinciaId); }
    public void construirIurta(String paradaId) { partida.construirIurta(paradaId); }
    public void posicionarKhan(String local) { partida.posicionarKhan(local); }
    public void atacarCidade(String cidadeId, int quantidade) { partida.atacarCidade(cidadeId, quantidade); }
    public void comprarMelhoria(String coluna, int vagaArea) {
        partida.comprarMelhoria(Regiao.valueOf(coluna), vagaArea);
    }

    /** oferta: nome do tipo de tesouro -> quantidade (ignorada em conselheiros de exigência fixa). */
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
