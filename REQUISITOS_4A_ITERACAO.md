# Verificação da quarta iteração (20/11/2026)

Fontes: `../4A-ITERACAO.PDF` e `../4A-ITERACAO-ENTREGA.PDF`.

| Exigência | Estado | Evidência / observação |
| --- | --- | --- |
| Encerrar a partida, mostrar vencedor e perguntar por nova partida | Implementado | `Controlador.concluir`, `JanelaJogo.mostrarFimDeJogo` e `perguntarNovaPartida`; resposta positiva reabre a escolha de personagens. O resultado, a pergunta por nova partida e o fechamento foram testados em `InterfaceFimSmoke`; a escolha positiva de nova partida ainda precisa de verificação manual. |
| Encerrar pelo fechamento do tabuleiro | Implementado | Janela principal usa `EXIT_ON_CLOSE`. |
| Salvar apenas entre jogadas | Implementado | `Jogo.podeSalvar`, `Partida.isTurnoAtivo`, botão Salvar desabilitado após ativar coluna. |
| Recuperar partida | Implementado | Janela inicial e botão Recuperar; estado integral da `Partida` restaurado e Observer notificado. |
| Arquivo `.txt` em ASCII puro | Implementado com ressalva | Cabeçalho legível e estado serializado codificado em Base64; todos os bytes são ASCII. Se o professor exigir cada campo diretamente legível, será preciso trocar o formato. |
| Nome/local livres; `JFileChooser` obrigatório | Implementado | `JFileChooser` para salvar e abrir, com filtro `.txt`. |
| Integração View-Model-Controller e Observer | Implementado na estrutura atual | A fachada `Jogo` notifica observadores após recuperação e mudanças de estado; painel e janelas auxiliares redesenham. |
| Tornar coleta e ações indisponíveis claras | Implementado | Coleta, Khan, Encerrar, Salvar, ataque, iurta, entrega e compra de melhorias desbotam e deixam de receber cliques quando indisponíveis. A legenda, dicas do mapa e as consultas Áreas K/P e Conselheiros mostram as relações e os pedidos. |
| Regras anteriores restantes | Pendente de verificação final | O mapa e as adjacências seguem aproximação da imagem. A referência a “bloco de notas” nas instruções parece vir de outro jogo; este projeto possui cartela e janela de cartas, sem bloco de notas separado. |
| Projeto Eclipse com nome de matrícula, `leiame.txt`, ZIP homônimo | Preparado e validado | Projeto e ZIP `2411003-2510899-2510382`; `leiame.txt` contém nomes, matrículas e `controller.Main`. Cópia extraída compilou em Java 8, passou em 62 testes JUnit e nos percursos de interface. Falta inserir o link do vídeo no `leiame.txt` e reexportar antes do upload. |
| Vídeo com exatamente 4 jogadores, salvamento, reinicialização, fim e arquitetura | Pendente de gravação | Roteiro descrito no PDF de entrega. |

Validação local em Java 8: 62 testes JUnit 4; `InterfaceSmoke`; `InterfaceFimSmoke`; cinco `InterfaceHerdeirosSmoke`.
