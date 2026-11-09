# Herdeiros do Khan — terceira iteração

Aplicação Swing/Java2D para Java 8. Esta pasta é uma cópia isolada do projeto do GitHub; a pasta `02_Projeto` foi preservada. A interface permite escolher de 2 a 5 herdeiros, jogar com as cinco cartelas e usar as regras implementadas no Model por cliques.

A verificação por requisito está em [REQUISITOS_3A_ITERACAO.md](REQUISITOS_3A_ITERACAO.md); as decisões e limites do projeto estão em [DECISOES_3A_ITERACAO.md](DECISOES_3A_ITERACAO.md).

## Executar

No PowerShell, dentro desta pasta, com JDK 8 no PATH:

```powershell
New-Item -ItemType Directory -Force build/classes | Out-Null
$fontes = Get-ChildItem src -Recurse -Filter '*.java' | ForEach-Object FullName
javac -source 8 -target 8 -encoding UTF-8 -d build/classes $fontes
java -cp 'build/classes;src' controller.Main
```

`src` no classpath carrega as imagens oficiais do mapa, das 24 cartas e das cinco cartelas. Ao abrir, escolha o número de jogadores e os herdeiros, sem repetir personagem. Jochi escolhe sua melhoria inicial. Na janela **Cartas**, cada jogador escolhe uma de duas cartas secretas antes de seu primeiro turno. Na **Cartela**, a aba **Jogo** ativa a coluna e mostra as melhorias; a aba **Cartela original** exibe a arte. Clique nas paradas do mapa para mover. O painel de controle reúne tributo, Khan, compra de melhoria, iurta, ataque, entrega, habilidade e fim do turno. Para Altani, a escolha do peão aparece ao ativar a coluna. Para Ogedei, clique **Habilidade** e depois **Pegar** na cidade.

## Testes

A suíte usa JUnit 4.13.2 e Hamcrest 1.3. Se os JARs não estiverem disponíveis, baixe-os:

```powershell
New-Item -ItemType Directory -Force build/lib, build/classes | Out-Null
Invoke-WebRequest 'https://repo.maven.apache.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar' -OutFile build/lib/junit-4.13.2.jar
Invoke-WebRequest 'https://repo.maven.apache.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar' -OutFile build/lib/hamcrest-core-1.3.jar
$cp = 'build/classes;build/lib/junit-4.13.2.jar;build/lib/hamcrest-core-1.3.jar'
$fontes = Get-ChildItem src,test -Recurse -Filter '*.java' | ForEach-Object FullName
javac -source 8 -target 8 -encoding UTF-8 -cp $cp -d build/classes $fontes
java '-Djava.awt.headless=true' -cp $cp org.junit.runner.JUnitCore model.SuiteDeTestes
java -cp 'build/classes;src' model.InterfaceSmoke
foreach ($h in 'ALTANI','JOCHI','CHAGATAI','TOLUI','OGEDEI') {
    java -cp 'build/classes;src' model.InterfaceHerdeirosSmoke $h
}
```

Referência executada com Temurin JDK 8 (`1.8.0_512`): **54/54 testes JUnit passaram**. `InterfaceSmoke` clicou carta, coluna, movimento, tributo, Khan, melhoria, iurta, quatro ataques com custo de espadas, conquista, entrega, votos e fim do turno. Também confirmou as 34 paradas clicáveis, as cidades reveladas com botão dentro do painel e a abertura/fechamento das janelas. `InterfaceHerdeirosSmoke` passou para os cinco herdeiros. Os cenários de interface preparam recursos no teste para percorrer várias ações numa rodada.

## Escopo

A terceira iteração pede integração View–Model, ações pela interface, Singleton, Façade, Observer com interfaces e Controller. Salvamento e recuperação são reservados pelo enunciado para a última iteração. O mapa e as províncias deste projeto são uma transcrição aproximada da fotografia do tabuleiro; isso limita a fidelidade de rotas, adjacências e pontuação dependente da geografia. Veja a matriz de requisitos para essa ressalva.
