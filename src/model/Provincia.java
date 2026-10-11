package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Província: área entre as rotas onde ficam as peças de tributo.
 * Uma província "Khan" pode receber o peão do Khan e, quando isso acontece,
 * ela e mais 2 províncias adjacentes recebem 1 tributo cada.
 */
class Provincia {

    /** Máximo de peças de tributo que cabem em uma província. */
    static final int MAX_TRIBUTOS = 3;

    private final String id;
    private final TipoTributo tipo;
    private final boolean provinciaKhan;
    private final List<String> adjacentesKhan;
    private int tributos;

    Provincia(String id, TipoTributo tipo, boolean provinciaKhan, List<String> adjacentesKhan) {
        this.id = id;
        this.tipo = tipo;
        this.provinciaKhan = provinciaKhan;
        this.adjacentesKhan = new ArrayList<>(adjacentesKhan);
    }

    String getId() { return id; }
    TipoTributo getTipo() { return tipo; }
    boolean isProvinciaKhan() { return provinciaKhan; }
    int getTributos() { return tributos; }

    /** Províncias que também recebem tributo quando o Khan é colocado aqui. */
    List<String> getAdjacentesKhan() {
        return Collections.unmodifiableList(adjacentesKhan);
    }

    /** Coloca 1 tributo; retorna false se a província já está cheia (3). */
    boolean adicionarTributo() {
        if (tributos >= MAX_TRIBUTOS) {
            return false;
        }
        tributos++;
        return true;
    }

    /** Retira 1 tributo; retorna false se a província está vazia. */
    boolean retirarTributo() {
        if (tributos == 0) {
            return false;
        }
        tributos--;
        return true;
    }
}
