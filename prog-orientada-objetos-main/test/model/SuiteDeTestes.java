package model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/** Executa todos os casos de teste do componente Model de uma só vez. */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    MovimentoTest.class,
    TributosKhanTest.class,
    CidadeTest.class,
    ConselheiroKurultaiTest.class,
    FimDeJogoTest.class,
    PontuacaoTest.class
})
public class SuiteDeTestes {
}
