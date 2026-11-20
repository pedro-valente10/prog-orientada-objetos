# Herdeiros do Khan — quarta iteração

Projeto Swing/Java2D para Java 8. A entrega Eclipse usa o nome 2411003-2510899-2510382 e inclui o arquivo leiame.txt com os integrantes e a classe principal.

## Importar no Eclipse

Use **File > Import > General > Existing Projects into Workspace > Select archive file** e escolha `2411003-2510899-2510382.zip`. O projeto importado deve se chamar `2411003-2510899-2510382`. Configure um JDK 8 se o Eclipse solicitar. A configuração `.classpath` usa JUnit 4.

Para abrir o jogo, execute `src/controller/Main.java` como **Java Application**. Para os testes, execute `test/model/SuiteDeTestes.java` como **JUnit Test**.

Depois que o link do vídeo estiver disponível, substitua `PENDENTE` em `leiame.txt` e execute `python exportar_eclipse.py` nesta pasta para recriar o ZIP antes do upload.

## Executar

No PowerShell, dentro desta pasta, com JDK 8 no PATH:

```powershell
New-Item -ItemType Directory -Force build/classes | Out-Null
$fontes = Get-ChildItem src -Recurse -Filter '*.java' | ForEach-Object FullName
javac -source 8 -target 8 -encoding UTF-8 -d build/classes $fontes
java -cp 'build/classes;src' controller.Main
```

Na primeira janela, escolha **Nova partida** ou **Recuperar partida**. Na nova partida, escolha de 2 a 5 herdeiros distintos e informe o nome de cada jogador. A ordem das escolhas define a ordem dos turnos. A cartela ativa uma coluna; no mapa, clique em uma parada alcançável para mover.

No mapa, **R** identifica paradas da Rússia, **P** paradas da Pérsia e **C** paradas da China. Nos botões de coleta, **K1–K4** identificam províncias Khan e **P1–P4** outras províncias; por exemplo, a parada P1 e a província P1 são elementos diferentes. A linha abaixo da posição do peão mostra as províncias adjacentes à parada atual. Passe o mouse sobre qualquer parada ou clique em **Áreas K/P** para ver as ligações usadas pelo jogo. **Conselheiros** lista todos os pedidos e votos antes de viajar até eles; a dica de mouse também mostra o pedido nas paradas com C.

Os botões **Coletar K1...P4** ficam claros apenas quando há punho, tributo disponível e uma parada visitada neste turno é vizinha da província. Ataques, construção de iurta, entrega a conselheiros e compras de melhorias também ficam apagados quando indisponíveis. Botões apagados não recebem cliques; os do painel mostram um motivo curto. O mesmo vale para as opções do Khan e **Encerrar** quando há uma obrigação pendente. Encerrar não exige gastar todos os movimentos e punhos.

**Salvar** só fica disponível entre turnos, antes de ativar uma coluna. **Recuperar** permite escolher um arquivo `.txt` com `JFileChooser`, inclusive na janela inicial. O arquivo é texto ASCII com cabeçalho legível e o estado integral codificado em Base64. A escolha do nome e do local do arquivo é livre. Ao fim da partida, depois da pontuação, o programa pergunta se os jogadores querem começar outra.

## Testes

A suíte usa JUnit 4.13.2 e Hamcrest 1.3; os JARs estão incluídos em `lib`:

```powershell
New-Item -ItemType Directory -Force build/classes | Out-Null
$cp = 'build/classes;src;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar'
$fontes = Get-ChildItem src,test -Recurse -Filter '*.java' | ForEach-Object FullName
javac -source 8 -target 8 -encoding UTF-8 -cp $cp -d build/classes $fontes
java '-Djava.awt.headless=true' -cp $cp org.junit.runner.JUnitCore model.SuiteDeTestes
java -cp 'build/classes;src' model.InterfaceSmoke
java -cp 'build/classes;src' model.InterfaceFimSmoke
foreach ($h in 'ALTANI','JOCHI','CHAGATAI','TOLUI','OGEDEI') {
    java -cp 'build/classes;src' model.InterfaceHerdeirosSmoke $h
}
```

Verificação em Java 8, inclusive a partir de uma cópia extraída do ZIP: **62/62 testes JUnit passaram**. O percurso `InterfaceSmoke` e os cinco cenários `InterfaceHerdeirosSmoke` passaram. Os testes cobrem salvamento e recuperação do turno, mapa, jogadores, nomes e carta secreta; ASCII; arquivo inválido; mapeamento de paradas e províncias; e habilitação dinâmica das ações. Os percursos de interface cobrem o ciclo principal, salvamento/recuperação pelo painel e a exibição do vencedor.

## Estado da quarta iteração

Veja `REQUISITOS_4A_ITERACAO.md` para a matriz de requisitos e os itens de entrega. O mapa e as províncias ainda são uma transcrição aproximada da fotografia do tabuleiro. O ZIP da terceira iteração na pasta superior não inclui estas alterações; use o ZIP `2411003-2510899-2510382.zip` para esta versão. O link do vídeo ainda deve ser incluído no `leiame.txt` antes do upload final.
