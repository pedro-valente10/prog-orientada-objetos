# Herdeiros do Khan — terceira iteração

Projeto Swing/Java2D em Java 8. Esta pasta é uma cópia do commit `e0c0a25fe98498143d593815365ecb0fc2a207b4` da branch `main` de [pedro-valente10/prog-orientada-objetos](https://github.com/pedro-valente10/prog-orientada-objetos), trabalhada na branch local `codex/terceira-iteracao`. O projeto anterior em `02_Projeto` não foi alterado.

O estado dos requisitos dos PDFs está em [REQUISITOS_3A_ITERACAO.md](REQUISITOS_3A_ITERACAO.md). As decisões de arquitetura e os limites do manual estão em [DECISOES_3A_ITERACAO.md](DECISOES_3A_ITERACAO.md).

## Executar

Use um JDK 8. No PowerShell, dentro desta pasta:

```powershell
New-Item -ItemType Directory -Force build/classes | Out-Null
$fontes = Get-ChildItem src -Recurse -Filter '*.java' | ForEach-Object FullName
javac -source 8 -target 8 -encoding UTF-8 -d build/classes $fontes
java -cp 'build/classes;src' controller.Main
```

`src` precisa estar no classpath para carregar `view/img/tabuleiro.jpg`. Com JDK 9 ou posterior, também é possível compilar com `javac --release 8` em vez de `-source 8 -target 8`.

Ao abrir, escolha entre 2 e 5 jogadores. Na janela **Cartas**, o jogador da vez escolhe uma das duas cartas secretas; na **Cartela**, ativa uma coluna. As duas janelas se fecham após a escolha e podem ser reabertas pelos botões. Clique nos círculos do mapa para mover. Use o painel para tributo, Khan, melhorias, iurta, ataque, entrega e encerramento. O botão **Bônus** reabre a escolha de tesouro se o diálogo for fechado. A janela de **Melhorias** fecha após uma compra e reaparece quando há uma melhoria branca gratuita pendente.

Em telas menores, o mapa é escalado e as janelas auxiliares ficam sobre seu lado esquerdo para deixar o painel de controle livre. Feche uma janela auxiliar pela barra de título para acessar as paradas cobertas.

## Testes

A suíte precisa de JUnit 4.13.2 e Hamcrest 1.3. Para obtê-los do Maven Central e executar a suíte:

```powershell
New-Item -ItemType Directory -Force build/lib, build/classes | Out-Null
Invoke-WebRequest 'https://repo.maven.apache.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar' -OutFile build/lib/junit-4.13.2.jar
Invoke-WebRequest 'https://repo.maven.apache.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar' -OutFile build/lib/hamcrest-core-1.3.jar
$cp = 'build/classes;build/lib/junit-4.13.2.jar;build/lib/hamcrest-core-1.3.jar'
$testes = Get-ChildItem test -Recurse -Filter '*.java' | ForEach-Object FullName
javac -source 8 -target 8 -encoding UTF-8 -cp $cp -d build/classes $testes
java '-Djava.awt.headless=true' -cp $cp org.junit.runner.JUnitCore model.SuiteDeTestes
java -cp 'build/classes;src' model.InterfaceSmoke
```

A execução de referência com Temurin JDK 8 (`1.8.0_512`) passou em **47 testes JUnit**. `InterfaceSmoke` prepara recursos apenas para o teste e aciona os ouvintes de clique do Swing: percorre as ações principais, quatro ataques à mesma cidade, conquista, entrega e votos. Também confere os centros das 34 paradas, os botões de cidades e a abertura/fechamento das janelas. Suas capturas são geradas em `build/screenshots/`.

## Limites conhecidos

A partida ainda não reproduz o manual completo. Os tabuleiros individuais usam um perfil comum, o grafo do mapa e as províncias são uma transcrição simplificada, e as habilidades próprias de Altani, Chagatai, Jochi, Ogedei e Tolui ainda não foram integradas. A escolha das cores/personagens e a tela inicial de nova/continuar partida não existem. Salvar e recuperar ficam para a última iteração, conforme o PDF da terceira. As cartas são desenhadas como peças textuais em Java2D porque as artes oficiais não constam do repositório. Esses pontos estão classificados individualmente no documento de requisitos; a partida não é apresentada como implementação integral do manual.
