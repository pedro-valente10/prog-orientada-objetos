package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

/** Arquivo ASCII de partida: cabecalho legivel e estado completo em Base64. */
final class PersistenciaPartida {
    private static final String CABECALHO = "HERDEIROS_DO_KHAN_SAVE_V1";
    private static final String MARCADOR = "\nestado-base64:\n";
    private static final long TAMANHO_MAXIMO = 5_000_000;

    private PersistenciaPartida() { }

    static void salvar(Path arquivo, Partida partida) throws IOException {
        if (partida.isTurnoAtivo())
            throw new IllegalStateException("Salve entre turnos, antes de ativar uma coluna.");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream saida = new ObjectOutputStream(bytes)) {
            saida.writeObject(partida);
        }
        StringBuilder texto = new StringBuilder(CABECALHO).append('\n');
        texto.append("jogadores=");
        for (int i = 0; i < partida.getOrdem().size(); i++) {
            if (i > 0) texto.append(',');
            texto.append(partida.getOrdem().get(i).name());
        }
        texto.append("\nvez=").append(partida.jogadorDaVez().name());
        texto.append(MARCADOR);
        texto.append(Base64.getMimeEncoder(76, new byte[] { '\n' }).encodeToString(bytes.toByteArray()));
        texto.append('\n');
        Path destino = arquivo.toAbsolutePath();
        Path temporario = Files.createTempFile(destino.getParent(), "khan-save-", ".tmp");
        try {
            Files.write(temporario, texto.toString().getBytes(StandardCharsets.US_ASCII));
            try {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporario);
        }
    }

    static Partida recuperar(Path arquivo) throws IOException {
        if (Files.size(arquivo) > TAMANHO_MAXIMO)
            throw new IOException("Arquivo de partida grande demais.");
        byte[] bruto = Files.readAllBytes(arquivo);
        for (byte b : bruto) if ((b & 0x80) != 0)
            throw new IOException("A partida precisa estar em texto ASCII.");
        String texto = new String(bruto, StandardCharsets.US_ASCII).replace("\r\n", "\n");
        if (!texto.startsWith(CABECALHO + "\n"))
            throw new IOException("Arquivo de partida ou versao desconhecida.");
        int inicio = texto.indexOf(MARCADOR);
        if (inicio < 0) throw new IOException("Estado da partida ausente.");
        String metadados = texto.substring(0, inicio);
        byte[] dados;
        try {
            dados = Base64.getMimeDecoder().decode(texto.substring(inicio + MARCADOR.length()));
        } catch (IllegalArgumentException e) {
            throw new IOException("Dados da partida invalidos.", e);
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(dados)) {
            @Override protected Class<?> resolveClass(ObjectStreamClass descricao)
                    throws IOException, ClassNotFoundException {
                String nome = descricao.getName();
                if (!(nome.startsWith("model.") || nome.startsWith("java.util.")
                        || nome.startsWith("java.lang.") || nome.startsWith("[")))
                    throw new InvalidClassException("Classe nao permitida", nome);
                return super.resolveClass(descricao);
            }
            @Override protected Class<?> resolveProxyClass(String[] interfaces) throws IOException {
                throw new InvalidClassException("Proxies nao permitidos");
            }
        }) {
            Object lido = entrada.readObject();
            if (!(lido instanceof Partida)) throw new IOException("Estado de partida invalido.");
            Partida partida = (Partida) lido;
            if (partida.isTurnoAtivo()) throw new IOException("Partida salva durante uma jogada.");
            if (!metadados.contains("\nvez=" + partida.jogadorDaVez().name()))
                throw new IOException("Cabecalho da partida inconsistente.");
            return partida;
        } catch (ClassNotFoundException | RuntimeException e) {
            throw new IOException("Nao foi possivel recuperar esta partida.", e);
        }
    }
}