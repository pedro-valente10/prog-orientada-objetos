package model;

/**
 * Padrão Observer (Capítulo 17) - lado do observador.
 * Quem deseja ser avisado das mudanças de estado de uma partida implementa esta interface
 * e se registra em um {@link ObservadoIF}.
 */
public interface ObservadorIF {

    /** Chamado pelo observado sempre que o seu estado muda. */
    void notify(ObservadoIF observado);
}
