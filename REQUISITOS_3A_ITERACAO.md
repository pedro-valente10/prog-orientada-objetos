# Verificação dos enunciados — 2ª e 3ª iterações

Fontes: `../2a-Iteracao.pdf`, `../3a-Iteracao.pdf`, `../01_Trabalho/Enunciados/Regas.pdf` e cartelas/imagens em `../01_Trabalho/Imagens/Arquivos`. A 2ª iteração usa exemplos de outro jogo (cômodos e bloco de notas); eles não são mecânicas de *Herdeiros do Khan*.

## Ponto de partida

A cópia inicial veio de `main` no commit `e0c0a25fe98498143d593815365ecb0fc2a207b4`. Nesse estado, 38 testes JUnit passaram. A pasta original `02_Projeto` foi preservada. O bloqueio relatado de botões de ataque em `x=474` não ocorria nesse commit; os botões começavam em `x=14`, `129` e `244`. A correção necessária foi escalar o mapa e os cliques em telas menores.

## Matriz

| Fonte | Exigência | Estado | Evidência / observação |
|---|---|---|---|
| 2ª, pp. 1 e 4 | Swing e Java2D; tabuleiro, peões e cartas com `drawImage` | **Atendida** | Mapa, peões e 24 cartas oficiais são desenhados como imagens; cinco cartelas originais acessíveis em aba. |
| 2ª, p. 1 | Janelas até 1400 × 1050 | **Atendida** | Principal mede 1356 × 925 na tela de teste; auxiliares verificadas dentro da área útil. |
| 2ª, pp. 2–3 e 9 | Início e escolha de personagens | **Atendida no escopo da 3ª** | `Main` inicia nova partida e permite escolher 2 a 5 herdeiros distintos. Continuar partida depende de salvamento, reservado à última iteração. |
| 2ª, p. 5 | Indicar vez e automatizar efeitos sem escolha | **Atendida** | Painel mostra vez, recursos e Khan; votos, bônus e revelação são automáticos. |
| 2ª, pp. 6 e 9 | Mover por clique; clique inválido não muda estado | **Atendida** | Controller e painel do mapa; smoke confirma 34 centros de paradas. |
| 2ª, p. 7 | Janela de cartas aberta/fechada pela interface | **Atendida** | `JanelaCartas`, botão Cartas, eventos Observer e fechamento pelo título. |
| 2ª, p. 8 | Bloco de notas do jogo de exemplo | **Não aplicável** | Não existe mecânica equivalente no manual de Khan. |
| 2ª, p. 10 | Sem lambdas nos ouvintes gráficos | **Atendida** | Ouvintes são classes internas do Controller. |
| 3ª, p. 1 | Regras do Model acionáveis pela interface | **Atendida no Model existente e ampliado** | Carta secreta, coluna, movimento, tributo, Khan, melhorias, iurta, ataque, conquista, entrega, bônus, votos, turno e cinco herdeiros por clique. A geometria do mapa continua aproximada. |
| 3ª, p. 1 | Singleton e Façade em `Jogo` | **Atendida** | Construtor privado, `getInstance()`, comandos/consultas pela fachada; testes. |
| 3ª, p. 1 | Observer com interfaces na atualização da View | **Atendida** | `ObservadoIF`, `ObservadorIF`, `Jogo` e observadores; testes de mudanças e janelas. |
| 3ª, p. 1 | Controller organiza a sequência | **Atendida** | Carta e Khan obrigatórios, modos de clique, escolha de melhoria, bônus e fim. |
| 3ª, p. 1 | Salvar e recuperar | **Previsto para a última iteração** | Adiado expressamente pelo PDF. |
| Manual, pp. 16–19 | Cartas, bônus e votos secretos | **Implementados** | 14 abertas, 10 secretas, 7 bônus, cartas secretas somadas ao resultado; imagens oficiais. A pontuação geográfica depende do mapa aproximado. |
| Manual, p. 20 | Habilidades dos cinco herdeiros | **Implementadas** | Perfis de ação e habilidades de Altani, Jochi, Chagatai, Tolui e Ogedei; JUnit e cliques. |

## Verificação final

Temurin Java 8 (`1.8.0_512`): **54/54 testes JUnit**. `InterfaceSmoke` passou com todas as ações principais, quatro ataques (`1+2+2+2` espadas), conquista, tesouros, entrega e votos; botões das cidades reveladas dentro do painel, 34 paradas clicáveis e janelas auxiliares acessíveis. `InterfaceHerdeirosSmoke` passou para os cinco herdeiros. Os cenários de interface preparam recursos para cobrir as ações; não medem equilíbrio econômico de uma partida espontânea.
