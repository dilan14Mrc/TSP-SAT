package tspsat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Programa principal: TSP (decisión) reducido a SAT y resuelto con Sat4j.
 *
 * <pre>
 * Uso:
 *   Main instancia.txt [-k K] [-o salida.cnf] [-t segundos] [--sin-simetria]
 *   Main --random N K [semilla] [-o salida.cnf] [-t segundos]
 *   Main --bench N1,N2,... FACTOR_K [semilla] [-t segundos]
 * </pre>
 */
public final class Main {

    private static final int MAX_DIST_RANDOM = 10;

    private Main() {
    }

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                usage();
                return;
            }
            if (args[0].equals("--bench")) {
                bench(args);
                return;
            }
            Path out = Paths.get("tsp.cnf");
            int timeout = 0;
            boolean fixStart = true;
            Integer overrideK = null;
            TspInstance inst;
            int idx;
            if (args[0].equals("--random")) {
                int n = Integer.parseInt(args[1]);
                int k = Integer.parseInt(args[2]);
                long seed = 1;
                idx = 3;
                if (args.length > 3 && !args[3].startsWith("-")) {
                    seed = Long.parseLong(args[3]);
                    idx = 4;
                }
                inst = RandomInstanceGenerator.generate(n, k, seed, MAX_DIST_RANDOM);
            } else {
                inst = InstanceReader.read(Paths.get(args[0]));
                idx = 1;
            }
            for (int a = idx; a < args.length; a++) {
                switch (args[a]) {
                    case "-k": overrideK = Integer.parseInt(args[++a]); break;
                    case "-o": out = Paths.get(args[++a]); break;
                    case "-t": timeout = Integer.parseInt(args[++a]); break;
                    case "--sin-simetria": fixStart = false; break;
                    default: throw new IllegalArgumentException("Opción desconocida: " + args[a]);
                }
            }
            if (overrideK != null) {
                inst = inst.withBound(overrideK);
            }
            run(inst, out, timeout, fixStart, true);
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /** Ejecuta las 3 fases y devuelve {vars, clausulas, msTraduccion, msSolver, estado}. */
    private static long[] run(TspInstance inst, Path out, int timeout, boolean fixStart, boolean verbose)
            throws IOException {
        int n = inst.size();
        if (verbose) {
            System.out.println("=== TSP <=p SAT ===");
            System.out.println("N = " + n + ", K = " + inst.bound());
        }
        // Fase 1: reducción polinomial
        long t0 = System.nanoTime();
        CnfFormula cnf = new TspToSatReducer(fixStart).reduce(inst);
        DimacsWriter.write(cnf, out, "TSP N=" + n + " K=" + inst.bound());
        long msTrans = (System.nanoTime() - t0) / 1_000_000;
        if (verbose) {
            System.out.println("[Fase 1] CNF generado en " + out + ": " + cnf.numVars()
                    + " variables, " + cnf.numClauses() + " clausulas, " + msTrans + " ms");
        }
        // Fase 2: SAT-Solver
        SolverResult res = Sat4jRunner.solve(out, timeout);
        if (verbose) {
            System.out.println("[Fase 2] Sat4j: " + res.status() + " en " + res.millis() + " ms");
        }
        // Fase 3: reconstrucción
        if (verbose) {
            switch (res.status()) {
                case UNSAT:
                    System.out.println("NO existe ninguna ruta con costo menor o igual a K");
                    break;
                case TIMEOUT:
                    System.out.println("Tiempo limite agotado: no se pudo decidir.");
                    break;
                default:
                    int[] route = RouteDecoder.decode(res.model(), n);
                    int cost = inst.tourCost(route);
                    System.out.println("[Fase 3] SI existe una ruta: " + RouteDecoder.format(route));
                    System.out.println("Costo real = " + cost + " (K = " + inst.bound() + ")");
                    if (cost > inst.bound()) {
                        System.out.println("ADVERTENCIA: el costo supera K, revisar la reduccion.");
                    }
            }
        }
        return new long[] {cnf.numVars(), cnf.numClauses(), msTrans, res.millis(),
                res.status().ordinal()};
    }

    /** Modo de pruebas: una fila de tabla por cada N. */
    private static void bench(String[] args) throws IOException {
        String[] sizes = args[1].split(",");
        int factor = Integer.parseInt(args[2]);
        long seed = args.length > 3 && !args[3].startsWith("-") ? Long.parseLong(args[3]) : 1;
        int timeout = 0;
        for (int a = 3; a < args.length; a++) {
            if (args[a].equals("-t")) {
                timeout = Integer.parseInt(args[++a]);
            }
        }
        System.out.println("N\tK\tvariables\tclausulas\tt_traduccion_ms\tt_solver_ms\tresultado");
        for (String s : sizes) {
            int n = Integer.parseInt(s.trim());
            TspInstance inst = RandomInstanceGenerator.generate(n, factor * n, seed, MAX_DIST_RANDOM);
            long[] r = run(inst, Paths.get("bench_N" + n + ".cnf"), timeout, true, false);
            System.out.println(n + "\t" + inst.bound() + "\t" + r[0] + "\t" + r[1] + "\t"
                    + r[2] + "\t" + r[3] + "\t" + SolverResult.Status.values()[(int) r[4]]);
        }
    }

    private static void usage() {
        System.out.println("Uso:");
        System.out.println("  Main instancia.txt [-k K] [-o salida.cnf] [-t segundos] [--sin-simetria]");
        System.out.println("  Main --random N K [semilla] [-o salida.cnf] [-t segundos]");
        System.out.println("  Main --bench N1,N2,... FACTOR_K [semilla] [-t segundos]");
    }
}
