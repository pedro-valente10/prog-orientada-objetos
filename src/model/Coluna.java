package model;

/** Ordem das colunas no tabuleiro individual. */
public enum Coluna {
    CHINA, PERSIA, RUSSIA, BRANCA;

    Regiao regiao() { return this == BRANCA ? null : Regiao.valueOf(name()); }
}
