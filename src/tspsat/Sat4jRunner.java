package tspsat;

import java.io.IOException;
import java.nio.file.Path;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/** Interfaz con la librería Sat4j: lee un archivo DIMACS CNF y lo resuelve. */
public final class Sat4jRunner {

    private Sat4jRunner() {
    }

    /**
     * Resuelve el archivo CNF con Sat4j.
     *
     * @param cnfFile        archivo en formato DIMACS
     * @param timeoutSeconds límite de tiempo en segundos (0 = sin límite)
     * @return resultado con estado, modelo y tiempo empleado
     * @throws IOException si no se puede leer el archivo
     */
    public static SolverResult solve(Path cnfFile, int timeoutSeconds) throws IOException {
        long start = System.nanoTime();
        ISolver solver = SolverFactory.newDefault();
        if (timeoutSeconds > 0) {
            solver.setTimeout(timeoutSeconds);
        }
        DimacsReader reader = new DimacsReader(solver);
        try {
            IProblem problem = reader.parseInstance(cnfFile.toString());
            boolean sat = problem.isSatisfiable();
            long ms = (System.nanoTime() - start) / 1_000_000;
            if (sat) {
                return new SolverResult(SolverResult.Status.SAT, problem.model(), ms);
            }
            return new SolverResult(SolverResult.Status.UNSAT, null, ms);
        } catch (ContradictionException e) {
            // El solver detectó la contradicción al cargar las cláusulas
            long ms = (System.nanoTime() - start) / 1_000_000;
            return new SolverResult(SolverResult.Status.UNSAT, null, ms);
        } catch (TimeoutException e) {
            long ms = (System.nanoTime() - start) / 1_000_000;
            return new SolverResult(SolverResult.Status.TIMEOUT, null, ms);
        } catch (ParseFormatException e) {
            throw new IOException("Archivo CNF mal formado: " + e.getMessage(), e);
        }
    }
}
