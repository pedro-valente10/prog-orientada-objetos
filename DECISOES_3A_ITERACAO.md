# Decisões e limites da terceira iteração

O enunciado de 31/10/2026 não traz requisitos novos: pede (1) terminar a integração View–Model para que todas as regras sejam acionáveis pela interface, (2) usar Singleton, Façade e Observer nessa integração e (3) consolidar o Controller.

## Singleton e Façade — `model.Jogo`

- `Jogo` tem construtor privado e `Jogo.getInstance()`. Como a instância é única, a partida passou a ser criada por `novaPartida(n)` / `novaPartida(n, semente)`; os observadores registrados sobrevivem a uma nova partida.
- `Jogo` já era a API pública do Model e continua sendo a única fachada: Controller e View não enxergam `Partida`, `Tabuleiro` etc. (todas package-private). Para fechar a integração, a fachada ganhou consultas que antes a View/Controller calculavam ou não tinham: `getParadasAlcancaveis()` (antes montado no Controller), `getOcupacaoKurultai()`, `getLimiteKurultai()`, `getVotosFinais()`, `getTiposTesouro()` e, em `ParadaInfo`, `getConselheiro()` (`ConselheiroInfo`, imutável, com a exigência de entrega).
- O teste que usava `new Jogo(3, 2L)` passou a usar `Jogo.getInstance().novaPartida(3, 2L)`. Os demais testes de regras trabalham direto com `Partida` e não mudaram.

## Observer (Capítulo 17)

Estruturado como o enunciado pede, com interfaces para os dois lados e classes que as implementam:

- `ObservadorIF.notify(ObservadoIF)` e `ObservadoIF.add/remove/get(int)`.
- **Observado**: `Jogo`. Cada ação bem-sucedida chama os observadores; ações rejeitadas pelas regras não notificam (o estado não mudou). Quem recebe lê o que mudou por `get(Jogo.INFO_EVENTO)` (um `EventoJogo`) e `get(Jogo.INFO_DETALHE)` (parada, cor, coluna...).
- **Observadores** (registrados em `JanelaJogo`): `PainelTabuleiro` (relê o mapa e redesenha: movimento de peão, iurtas, alcançáveis), `PainelControle` (turno, recursos, Kurultai, botões), `JanelaCartela` e `JanelaMelhorias` (abrem/fecham e se redesenham).
- O Controller **não** manda a View se redesenhar: ele só chama o Model, e a View reage à notificação. Foi removido o antigo `atualizarVisao()`.

### Mudanças de estado citadas no enunciado

- *Movimentação de um pião*: `EventoJogo.PEAO_MOVIDO`.
- *Exibição da janela da cartela com as colunas*: a cartela é o tabuleiro individual do jogador (quatro colunas). Qual cartela está na tela, ou se a janela está oculta, é estado mantido pelo `Jogo` (`exibirCartela`, `ocultarCartela`, `getCartelaExibida`) e comunicado por `CARTELA_EXIBIDA` / `CARTELA_OCULTADA`. Ao encerrar o turno a cartela exibida passa a ser a do próximo jogador.
- *Exibição da janela de cartas*: o enunciado é genérico e o Model deste jogo não tem cartas (as DECISOES da 2ª iteração já deixavam isso fora). A janela equivalente aqui é a de **Melhorias** (oferta de peças), tratada do mesmo modo (`exibirMelhorias`, `ocultarMelhorias`, `MELHORIAS_EXIBIDAS` / `MELHORIAS_OCULTADAS`). Se o grupo tiver outra leitura para "cartas", basta trocar o conteúdo dessa janela: o mecanismo de observação é o mesmo.
- Fechar uma janela auxiliar pelo botão da barra de título também passa pelo Model (o Controller chama `ocultarCartela()` / `ocultarMelhorias()`), para o estado não divergir da tela.

## Controller

`Controlador` organiza a sequência de uma partida: abre a partida e a cartela do primeiro jogador; interpreta cliques da janela principal, da cartela, das melhorias e do mapa (todos com classes internas, sem lambdas); guarda o modo de clique (mover, yurt, entregar); após cada ação orienta o próximo passo (Khan obrigatório, escolha da melhoria branca, encerrar), abre a janela de Melhorias quando há peça gratuita pendente, avisa quando o fim de jogo é acionado e exibe o resultado quando a partida termina. Clique inválido de movimento continua sem efeito (2ª iteração); nos modos Yurt e Entregar o motivo da recusa é mostrado.

## Regras agora acionáveis pela interface

- **Entrega de tesouros a conselheiros e votos no Kurultai** (antes só no Model): modo *Entregar*, com confirmação para exigência fixa e escolha de tesouros para "quaisquer". Paradas com conselheiro aparecem marcadas com **C**.
- **Fim de jogo**: aviso do gatilho, última rodada e diálogo final com votos (Kurultai + influência regional) e vencedor(es).
- **Melhoria branca gratuita**: a janela de Melhorias abre sozinha quando a linha é completada.
- A tela mostra o que antes só existia no Model: tesouros do jogador (reais e virtuais), votos de todos e ocupação do Kurultai, local do Khan, tipo e quantidade de tributos de cada província, tesouros restantes de cada cidade.

## Limites que permanecem

- Número de jogadores fixo em 3 (constante em `Main`); o Model aceita de 2 a 5.
- O ataque a cidades retira um tesouro por clique (o Model cobra 1 espada no primeiro e 2 nos seguintes, no mesmo turno).
- Salvar e recuperar partida ficam para a última iteração, como diz o enunciado. A fachada em Singleton e o estado concentrado em `Partida` já deixam um ponto único para isso.
- As simplificações de tabuleiros individuais e peças descritas em `DECISOES_2A_ITERACAO.md` continuam valendo.
- A View não tem testes automatizados; foi verificada em execução manual.
