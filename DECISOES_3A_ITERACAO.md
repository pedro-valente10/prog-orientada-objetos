# Decisões e limites da terceira iteração

## Integração MVC

`Jogo` continua Singleton e Façade. O Controller conhece apenas sua API pública e os objetos imutáveis de consulta. `Jogo` emite eventos para `PainelTabuleiro`, `PainelControle`, `JanelaCartela`, `JanelaMelhorias` e `JanelaCartas`; cada observador redesenha após mudanças de estado. As janelas auxiliares também comunicam fechamento pelo botão da barra de título ao Model.

A expressão “janela de cartas” da 3ª iteração não é mais interpretada como a janela de Melhorias. Há agora uma janela própria de cartas de conselheiro, com quatro cartas abertas e duas opções secretas para o jogador da vez. As cartas são apresentadas como imagens geradas em `BufferedImage` e mostradas por `Graphics2D.drawImage()`. A arte oficial não veio no GitHub.

## Regras acrescentadas

- O baralho de 14 tipos de carta aberta é embaralhado; quatro são exibidas. O primeiro jogador que cumpre cada condição ganha um voto no Kurultai, e a carta é retirada.
- Cada jogador recebe duas das 10 cartas secretas, escolhe uma antes de ativar uma coluna e recebe de 0 a 3 votos dela no resultado final. O Controller impede a ativação sem escolha.
- Sete bônus são distribuídos às paradas com conselheiros. A primeira entrega bem-sucedida em cada parada concede o bônus correspondente. Recursos e ações são aplicados imediatamente; o bônus de tesouros pede a escolha de um entre três sorteados e impede encerrar o turno enquanto houver escolha pendente.
- A interface agora seleciona 2 a 5 jogadores. Em telas menores, o mapa é escalado; as auxiliares são posicionadas sobre a esquerda do mapa, mantendo o controle livre, e escolhas/compras fecham a janela correspondente.

## Limites ainda relevantes

O tabuleiro individual continua com um perfil comum para todas as cores; os valores e habilidades próprios dos cinco herdeiros não foram implementados. O grafo das 34 paradas e as províncias são uma transcrição simplificada da foto. Não há escolha explícita de personagem/cor, tela de continuar partida nem salvamento. O enunciado da 3ª iteração deixa salvamento e recuperação para a última etapa. As cartas ligadas a iurtas, cidades e províncias usam o mapa simplificado e devem ser conferidas contra um tabuleiro físico legível antes de declarar conformidade integral com o manual.

A matriz de cada exigência e os testes executados estão em [REQUISITOS_3A_ITERACAO.md](REQUISITOS_3A_ITERACAO.md).
