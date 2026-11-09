# Decisões da terceira iteração

## Integração

`Jogo` é Singleton e Façade do Model. `Controlador` envia comandos à fachada e organiza a sequência de escolhas e ações. `Jogo` implementa `ObservadoIF` e notifica observadores que implementam `ObservadorIF`; mapa, controle e janelas auxiliares se atualizam após mudanças do estado. Exibir e ocultar as janelas também produz eventos. A janela de cartas é própria para cartas abertas e secretas, separada da janela de melhorias.

A interface oferece 2 a 5 herdeiros distintos, conforme as cinco cartelas disponíveis. Jochi escolhe uma das duas melhorias iniciais e pode colocar futuras melhorias em qualquer coluna com espaço. Altani escolhe qual peão age em cada turno; Chagatai troca movimento por espada; Ogedei compra um tesouro remoto uma vez por turno; Tolui começa com iurta em Karakorum e pode construir em parada adjacente. As ações base de cada coluna seguem a cartela do herdeiro.

As 14 condições de carta aberta, 10 cartas secretas e 7 bônus estão modelados. A primeira entrega em cada conselheiro dá o bônus correspondente; cartas secretas entram nos votos finais. As imagens oficiais das 24 cartas e das cinco cartelas, fornecidas nos arquivos do trabalho, estão em `src/view/img`.

## Limite de fidelidade

O grafo das 34 paradas, as relações de vizinhança e as províncias foram transcritos da fotografia usada no projeto, sem uma especificação digital de coordenadas e conexões. A interface permite acionar as regras do Model; rotas, tributos e objetivos ligados à geografia podem divergir do tabuleiro físico. Esse limite não é ocultado nem descrito como implementação integral do manual.

O enunciado da terceira iteração deixa salvamento e recuperação da partida para a última etapa.
