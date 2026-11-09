# Verificação dos enunciados — 2ª e 3ª iterações

Referências lidas nesta pasta de trabalho: `../2a-Iteracao.pdf` (10 páginas), `../3a-Iteracao.pdf` (1 página) e `../01_Trabalho/Enunciados/Regas.pdf` (manual escaneado, 20 páginas impressas). A 2ª iteração contém exemplos de dados, cômodos e bloco de notas de outro jogo; esses exemplos não correspondem a *Herdeiros do Khan*. Os requisitos gerais de interface, eventos e movimento foram aplicados ao jogo deste repositório.

## Estado inicial fixado

- GitHub `main`: commit `e0c0a25fe98498143d593815365ecb0fc2a207b4`; cópia local isolada na branch `codex/terceira-iteracao`.
- JDK 17 com `javac --release 8`: 38 arquivos de produção compilados; JUnit 4.13.2: **38/38** testes passaram.
- `controller.Main` abriu e permaneceu em execução sem erro de inicialização. A inspeção do painel renderizado mostrou que os três botões de ataque já estavam em `x=14`, `129` e `244` (centros `67`, `182`, `297`), dentro dos 480 pixels. A afirmação de que começavam em `x=474` não corresponde ao commit fixado. O problema real de geometria foi o mapa cortado em uma tela pequena, antes de escalar o desenho e as coordenadas de clique.

## Matriz de exigências

| Fonte | Exigência | Estado | Evidência ou limite |
|---|---|---|---|
| 2ª, p. 1 e 4 | Swing e Java2D; tabuleiro, peões e cartas via `Graphics2D.drawImage()` | **Parcial** | Mapa e peões são imagens desenhadas por `drawImage`; cartas são geradas em `BufferedImage` e desenhadas por `drawImage`, mas faltam as artes oficiais das cartas e dos cinco tabuleiros individuais. |
| 2ª, p. 1 | Cada janela até 1400 × 1050 | **Atendida** | `JanelaJogo` verifica o limite e ajusta o tamanho à área útil. O teste de interface confirmou janela principal 1356 × 925, dentro de uma tela de 1920 × 1080, e auxiliares dentro da área útil. |
| 2ª, p. 2–3 e 9 | Janelas iniciais de nova/continuar e escolha de personagens | **Parcial** | A 2ª iteração as dispensa; agora há escolha de 2–5 jogadores, mas ainda não de cores/personagens nem recuperação de partida. Salvar/recuperar são explicitamente adiados pela 3ª. |
| 2ª, p. 5 | Indicar claramente o jogador da vez e automatizar efeitos sem escolha | **Atendida** | Painel mostra cor, turno e recursos; efeitos de tributo, Khan, ataque, voto e cartas são automáticos. Diálogos só pedem escolhas necessárias. |
| 2ª, p. 6 e 9 | Mover por clique; clique inválido sem mensagem ou mudança de vez | **Atendida** | `PainelTabuleiro.paradaEm` e `Controlador.executarCliqueNoMapa`; teste de interface confere 34 centros de paradas na tela reduzida. |
| 2ª, p. 7 | Janela própria de cartas aberta/fechada pela interface | **Atendida** | `JanelaCartas` e botão Cartas; eventos Observer; fechamento pela barra de título testado. |
| 2ª, p. 8 | Bloco de notas para cartas mostradas por adversários | **Ausente** | É mecânica do jogo de exemplo do PDF, sem equivalente no manual de Khan. |
| 2ª, p. 10 | Sem lambdas nos eventos gráficos | **Atendida** | Ouvintes são classes internas de `Controlador`. |
| 3ª, p. 1 | Todas as regras acionáveis pela interface | **Parcial** | O percurso de clique passa por coluna, movimento, tributo, Khan, melhoria, iurta, ataque, conquista, entrega, voto e fim de turno. Cartas abertas, secretas e bônus entram no fluxo. Permanecem as habilidades próprias dos herdeiros, os perfis originais e a transcrição fiel do tabuleiro. |
| 3ª, p. 1 | Singleton em `Jogo` | **Atendida** | Construtor privado, `getInstance()` e testes `TerceiraIteracaoTest`. |
| 3ª, p. 1 | Façade na integração View–Model | **Atendida** | Controller e View usam `Jogo` para comandos/consultas; `Partida` e `Tabuleiro` são internos ao pacote. |
| 3ª, p. 1 | Observer com interfaces e atualização da View | **Atendida** | `ObservadoIF`, `ObservadorIF`, `Jogo` e observadores das janelas/painéis, incluindo cartas. Testes cobrem eventos de movimento, janelas e carta secreta. |
| 3ª, p. 1 | Controller organiza a sequência | **Atendida** | `Controlador` interpreta cliques, exige carta secreta e Khan, abre melhoria branca e anuncia encerramento. |
| 3ª, p. 1 | Salvamento e recuperação na última iteração | **Ausente nesta entrega** | O próprio enunciado reserva essas funções para a iteração seguinte. |
| Manual, pp. 16–19 | Cartas abertas, secretas, bônus e votos secretos | **Parcial** | 14 condições de carta aberta, 10 secretas e 7 bônus modelados; quatro abertas por partida, duas secretas oferecidas por jogador, primeira entrega concede bônus, cartas secretas somam aos votos finais. As peças usam texto gerado em vez da arte oficial; a precisão de condições ligadas ao mapa depende da transcrição simplificada. |
| Manual, p. 20 | Habilidades especiais dos cinco herdeiros | **Ausente** | O Model ainda usa o mesmo perfil inicial por cor; nenhuma habilidade única foi aplicada. |

## Verificação da versão entregue

- Compilação e execução com Temurin JDK 8 (`1.8.0_512`): **47/47 testes passaram**. `controller.Main` abriu sem erro de inicialização.
- `InterfaceSmoke`: todas as 34 paradas acessíveis; três janelas auxiliares dentro da tela; botões de ataque legíveis e clicáveis antes e depois de uma conquista; primeiro e segundo ataques no mesmo turno, custos de espada `1+2+2+2`, tesouros, conquista, entrega e votos; troca de jogador ao encerrar.
- Os testes de interface usam recursos preparados no cenário de teste para alcançar todas as ações em um único turno. Isso prova os caminhos de clique e regras dessas ações, não a fidelidade integral do manual ou a progressão econômica de uma partida espontânea.
