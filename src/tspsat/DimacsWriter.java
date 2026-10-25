package tspsat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Escribe una {@link CnfFormula} en formato DIMACS CNF. */
public final class DimacsWriter {

    private DimacsWriter() {
    }

    /**
     * Guarda la fórmula en un archivo {@code .cnf}.
     *
     * @param formula fórmula a guardar
     * @param file    destino
     * @param comment comentario descriptivo para la cabecera (puede ser null)
     * @throws IOException si falla la escritura
     */
    public static void write(CnfFormula formula, Path file, String comment) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            if (comment != null) {
                w.write("c " + comment);
                w.newLine();
            }
            w.write("p cnf " + formula.numVars() + " " + formula.numClauses());
            w.newLine();
            StringBuilder sb = new StringBuilder();
            for (int[] clause : formula.clauses()) {
                sb.setLength(0);
                for (int lit : clause) {
                    sb.append(lit).append(' ');
                }
                sb.append('0');
                w.write(sb.toString());
                w.newLine();
            }
        }
    }
}
