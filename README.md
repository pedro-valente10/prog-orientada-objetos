# Herdeiros do Khan — segunda iteração

Projeto Java 8 em MVC, com interface Swing/Java2D. Execute `controller.Main` no Eclipse ou por linha de comando com `src` no classpath para carregar `view/img/tabuleiro.jpg`. Não há dependências de produção além do Java 8. A janela tem 1330 × 854 pixels de conteúdo e confere o limite externo de 1400 × 1050.

## Como jogar a versão demonstrável

1. Clique no cabeçalho de uma das quatro colunas do tabuleiro individual do jogador da vez. As ações da coluna são calculadas nesse momento; uma melhoria comprada passa a valer apenas na próxima ativação.
2. Clique em um círculo amarelo do mapa para mover o peão. O caminho escolhido usa o menor custo em cavalos, inclusive saltos gratuitos pelas próprias iurtas. Um movimento não encerra o turno.
3. Use os botões para coletar tributo, atacar uma cidade revelada, colocar o Khan ou comprar uma melhoria da oferta ou da pilha. Para construir uma iurta, ative o modo **Yurt** e clique numa parada visitada no turno. Ações inválidas preservam o estado.
4. Ao completar a mesma linha nas três colunas coloridas, escolha uma melhoria gratuita para a coluna branca. Depois de usar o Khan obrigatório da coluna, clique **Encerrar** para passar a vez. Ativar a coluna branca recupera as fichas de ativação.

As abas coloridas permitem consultar o tabuleiro individual de cada jogador. As peças de melhoria mostram efeito, cor e identidade. O mapa usa a imagem original; círculos e rotas destacados são exatamente as paradas e conexões expostas pelo Model.

## Verificação

Compile os arquivos de `src` e `test` com `javac --release 8 -encoding UTF-8` e JUnit 4.13.2/Hamcrest 1.3 no classpath. Execute `org.junit.runner.JUnitCore model.SuiteDeTestes`. A suíte reúne os testes originais e as regressões da segunda iteração.

As escolhas de regras, divergências dos enunciados e limitações restantes estão em [DECISOES_2A_ITERACAO.md](DECISOES_2A_ITERACAO.md).
