package tspsat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee instancias TSP desde un archivo de texto.
 *
 * <p>Formato del archivo:</p>
 * <pre>
 * # Las líneas que empiezan con # son comentarios
 * N K
 * d00 d01 ... d0(N-1)
 * ...
 * d(N-1)0 ... d(N-1)(N-1)
 * </pre>
 * Los números pueden separarse por espacios, tabulaciones o saltos de línea.
 */
public final class InstanceReader {

    private InstanceReader() {
    }

    /**
     * Carga una instancia desde un archivo.
     *
     * @param file ruta del archivo de texto
     * @return la instancia leída
     * @throws IOException              si no se puede leer el archivo
     * @throws IllegalArgumentException si el contenido no respeta el formato
     */
    public static TspInstance read(Path file) throws IOException {
        List<Integer> tokens = new ArrayList<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String clean = line.trim();
            if (clean.isEmpty() || clean.startsWith("#")) {
                continue;
            }
            for (String piece : clean.split("\\s+")) {
                try {
                    tokens.add(Integer.parseInt(piece));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Valor no numérico en el archivo: '" + piece + "'");
                }
            }
        }
        if (tokens.size() < 2) {
            throw new IllegalArgumentException("El archivo debe iniciar con: N K");
        }
        int n = tokens.get(0);
        int k = tokens.get(1);
        if (n < 2 || tokens.size() != 2 + n * n) {
            throw new IllegalArgumentException("Se esperaban " + (2 + n * n)
                    + " números (N, K y la matriz NxN) pero hay " + tokens.size() + ".");
        }
        int[][] dist = new int[n][n];
        int pos = 2;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = tokens.get(pos++);
            }
        }
        return new TspInstance(dist, k);
    }
}
