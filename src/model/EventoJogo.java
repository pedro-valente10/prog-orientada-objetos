package model;

/** Tipos de mudança de estado de uma partida que o Model comunica aos observadores. */
public enum EventoJogo {
    PARTIDA_INICIADA,
    COLUNA_ATIVADA,
    TURNO_ENCERRADO,
    PEAO_MOVIDO,
    TRIBUTO_COLETADO,
    IURTA_CONSTRUIDA,
    KHAN_POSICIONADO,
    CIDADE_ATACADA,
    MELHORIA_ADQUIRIDA,
    TESOUROS_ENTREGUES,
    CARTELA_EXIBIDA,
    CARTELA_OCULTADA,
    MELHORIAS_EXIBIDAS,
    MELHORIAS_OCULTADAS,
    JOGO_ENCERRADO
}
