# Herdeiros do Khan — terceira iteração

Projeto Java 8 em MVC, com interface Swing/Java2D. Execute `controller.Main` no Eclipse ou por linha de comando com `src` no classpath para carregar `view/img/tabuleiro.jpg`. Não há dependências de produção além do Java 8. A janela principal tem 1330 × 854 pixels de conteúdo e confere o limite externo de 1400 × 1050.

## Padrões desta iteração

| Padrão | Onde está |
|---|---|
| **Singleton** | `model.Jogo` (`Jogo.getInstance()`, construtor privado; a partida é criada com `novaPartida(n)`) |
| **Façade** | `model.Jogo`: única porta de entrada do Model para o Controller e a View |
| **Observer** (Cap. 17) | `model.ObservadorIF` e `model.ObservadoIF`; `Jogo` é o observado; `PainelTabuleiro`, `PainelControle`, `JanelaCartela` e `JanelaMelhorias` são os observadores |

As decisões e o mapeamento para o enunciado estão em [DECISOES_3A_ITERACAO.md](DECISOES_3A_ITERACAO.md).

## Como jogar

A interface tem a janela principal (mapa + painel de controle) e duas janelas auxiliares, **Cartela** e **Melhorias**, que ficam ao lado da principal quando a tela comporta (podem ser arrastadas). Elas também abrem pelos botões *Cartela* e *Melhorias*.

1. Na **Cartela**, clique no cabeçalho de uma coluna do jogador da vez. As ações da coluna são calculadas nesse momento; uma melhoria comprada só vale na próxima ativação. Clicar numa aba de jogador mostra a cartela dele.
2. No **mapa**, clique num círculo amarelo para mover o peão (menor custo em cavalos, com saltos gratuitos pelas próprias iurtas). Mover não encerra o turno.
3. Pelos **botões**: coletar tributo (cada botão mostra o tipo e a quantidade da província), atacar cidade revelada, colocar o Khan, comprar melhoria.
4. **Yurt** e **Entregar** são modos: ligue o modo e clique numa parada visitada no turno. Em *Entregar*, a parada precisa ter conselheiro (marcado com **C** no mapa); abre-se um diálogo para confirmar a entrega (exigência fixa) ou escolher os tesouros (conselheiro de "tesouros quaisquer"). Cada entrega rende votos no Kurultai.
5. Ao completar a mesma linha nas três colunas coloridas, a janela **Melhorias** abre sozinha para escolher a peça gratuita da coluna branca. Depois de usar o Khan obrigatório, clique **Encerrar** para passar a vez.
6. Quando o Kurultai enche ou todas as cidades são conquistadas, o fim de jogo é acionado (termina a rodada e mais uma); ao final aparece o diálogo com os votos finais e o(s) vencedor(es).

## Verificação

Compile os arquivos de `src` e `test` com `javac --release 8 -encoding UTF-8` e JUnit 4.13.2/Hamcrest 1.3 no classpath. Execute `org.junit.runner.JUnitCore model.SuiteDeTestes` (38 testes: os originais, as regressões da 2ª iteração e `TerceiraIteracaoTest`, que cobre Singleton, Observer e as novas consultas da fachada). A interface foi exercitada manualmente em execução real (ativar coluna, mover, trocar de jogador, abrir melhorias, entregar a conselheiro, encerrar turno).

Documentos anteriores: [DECISOES_2A_ITERACAO.md](DECISOES_2A_ITERACAO.md).
