# Decisões e limites da segunda iteração

## Fonte das regras

O manual `Regas.pdf` de *Herdeiros do Khan* orientou as ações implementadas. `2a-Iteracao.pdf` exige Swing, Java2D, `Graphics2D.drawImage()`, janela de até 1400 × 1050 e proíbe lambdas nos eventos. Seu texto sobre dados, cômodos, passagens secretas e bloco de notas descreve outro jogo. Esta implementação **não usa dados nem passagens secretas**: os cavalos, punhos e benefícios virtuais vêm da coluna ativada, como no manual de Khan. O vídeo mencionado na página 9 da segunda iteração ficou fora desta entrega de código.

## Regras cobertas

- Cada jogador guarda seu próprio conjunto de quatro colunas, quatro espaços por coluna, melhorias identificadas e fichas de ativação usadas. A coluna branca libera as fichas.
- A oferta começa com cinco peças de uma reserva finita de 44. A compra custa duas moedas, exige espaço na coluna da cor da peça e preenche de baixo para cima. As peças restantes deslizam à direita; espaços vazios só são repostos com o Khan na área de melhorias. Ao concluir uma linha colorida, uma peça da oferta ou do topo é colocada gratuitamente na coluna branca.
- Melhorias fornecem moeda, espada, iurta ou tesouro virtual, punho adicional ou dois cavalos somente quando sua coluna é ativada. Benefícios não são retroativos à compra. Os recursos virtuais são gastos antes dos físicos e expiram no fim do turno.
- Uma ativação permite combinar movimentos e demais ações em qualquer ordem. O Khan indicado pela coluna é obrigatório. O peão pode atravessar paradas ocupadas, mas precisa terminar numa com vaga. Uma iurta própria intermediária não consome cavalo. O Controller pede ao Model o caminho de menor custo, em vez de usar o menor número de arestas.

## Simplificações ainda visíveis

- O repositório original fornece a fotografia do mapa, mas não as artes dos cinco tabuleiros individuais nem a lista por cor das 44 peças. Para tornar esta etapa jogável, o Model usa um perfil inicial comum às cores (dois cavalos por coluna, um punho na Pérsia, Khan na China e Rússia) e distribui as cores das melhorias entre os efeitos listados no manual. As identidades, o número de peças e seus efeitos são estáveis durante a partida; as capacidades próprias de cada herdeiro ainda dependem dos tabuleiros originais.
- O mapa clicável passou de 13 para 34 paradas posicionadas nos círculos da fotografia. As ligações jogáveis são destacadas e vêm da mesma fonte que valida o movimento. A transcrição integral de todos os trechos gráficos do tabuleiro e das províncias ainda exige conferência com um exemplar físico legível. As províncias e seus tipos de tributo continuam simplificados no Model herdado.
- A interface desta etapa não oferece uma escolha de tesouros para entrega a conselheiros, embora o Model já valide a entrega e aplique tesouros virtuais. Cartas, assimetrias dos herdeiros e pontuação final completa permanecem fora do recorte desta iteração.
