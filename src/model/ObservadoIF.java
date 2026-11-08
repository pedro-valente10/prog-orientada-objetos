package model;

/**
 * Padrão Observer (Capítulo 17) - lado do observado.
 * Mantém a lista de observadores e permite que eles consultem o que mudou por meio de
 * {@link #get(int)}.
 */
public interface ObservadoIF {

    /** Registra um observador. */
    void add(ObservadorIF observador);

    /** Remove um observador. */
    void remove(ObservadorIF observador);

    /** Informação sobre a última mudança de estado (o significado de cada índice é de quem implementa). */
    Object get(int i);
}
