package tspsat;

import java.util.ArrayList;
import java.util.List;

/**
 * Reducción polinomial TSP (decisión) a SAT.
 *
 * <p>Variables:</p>
 * <ul>
 *   <li>{@code X(i,t)}: la ciudad {@code i} se visita en el paso {@code t}
 *       (i, t en 0..N-1). Ocupan las variables 1..N^2.</li>
 *   <li>{@code Y(i,j,t)}: en el paso {@code t} se viaja de {@code i} a {@code j}
 *       (el paso N-1 regresa al paso 0, cerrando el ciclo).</li>
 *   <li>{@code S(m,c)}: variables auxiliares del contador secuencial que
 *       limita la suma de costos a K.</li>
 * </ul>
 *
 * <p>Este reductor NO resuelve el TSP: solo traduce la instancia a cláusulas.</p>
 */
public final class TspToSatReducer {

    private final boolean fixStart;

    /**
     * @param fixStart si es true, fija la ciudad 0 en el paso 0 (ruptura de
     *                 simetría válida porque el recorrido es un ciclo)
     */
    public TspToSatReducer(boolean fixStart) {
        this.fixStart = fixStart;
    }

    /**
     * Número de la variable booleana X(i,t).
     *
     * @param n número de ciudades
     * @param i ciudad (0..n-1)
     * @param t paso (0..n-1)
     * @return número de variable DIMACS (empieza en 1)
     */
    public static int cityVar(int n, int i, int t) {
        return i * n + t + 1;
    }

    /**
     * Construye la fórmula CNF equivalente a la instancia.
     *
     * @param inst instancia TSP de decisión
     * @return fórmula CNF satisfacible si y solo si existe un ciclo hamiltoniano de costo menor o igual a K
     */
    public CnfFormula reduce(TspInstance inst) {
        final int n = inst.size();
        final int k = inst.bound();
        CnfFormula f = new CnfFormula();

        // Variables X(i,t): 1..n^2
        for (int v = 0; v < n * n; v++) {
            f.newVar();
        }

        // Restricción 1: visita única (cada ciudad en exactamente un paso)
        for (int i = 0; i < n; i++) {
            int[] alo = new int[n];
            for (int t = 0; t < n; t++) {
                alo[t] = cityVar(n, i, t);
            }
            f.addClause(alo);                                   // al menos un paso
            for (int t = 0; t < n; t++) {
                for (int u = t + 1; u < n; u++) {
                    f.addClause(-cityVar(n, i, t), -cityVar(n, i, u)); // a lo sumo un paso
                }
            }
        }

        // Restricción 2: ocupación única (cada paso tiene exactamente una ciudad)
        for (int t = 0; t < n; t++) {
            int[] alo = new int[n];
            for (int i = 0; i < n; i++) {
                alo[i] = cityVar(n, i, t);
            }
            f.addClause(alo);                                   // al menos una ciudad
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    f.addClause(-cityVar(n, i, t), -cityVar(n, j, t)); // a lo sumo una ciudad
                }
            }
        }

        // Ruptura de simetría: el ciclo puede rotarse, así que fijamos el origen
        if (fixStart) {
            f.addClause(cityVar(n, 0, 0));
        }

        // Restricción 3: costo total <= K
        // 3a) Y(i,j,t) se activa cuando se usa la arista i->j entre el paso t y t+1
        List<int[]> weighted = new ArrayList<>(); // pares {variableY, peso}
        for (int t = 0; t < n; t++) {
            int next = (t + 1) % n;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == j) {
                        continue;
                    }
                    int y = f.newVar();
                    f.addClause(-cityVar(n, i, t), -cityVar(n, j, next), y);
                    weighted.add(new int[] {y, inst.distance(i, j)});
                }
            }
        }
        // 3b) La suma de pesos de las Y verdaderas debe ser <= K
        addWeightedAtMost(f, weighted, k);
        return f;
    }

    /**
     * Codifica en CNF la restricción "suma de pesos de literales verdaderos <= k"
     * con un contador secuencial ponderado. Usa O(m*k) variables y cláusulas,
     * con m = cantidad de literales.
     */
    private static void addWeightedAtMost(CnfFormula f, List<int[]> items, int k) {
        List<int[]> active = new ArrayList<>();
        for (int[] it : items) {
            int lit = it[0];
            int w = it[1];
            if (w == 0) {
                continue;                       // no aporta costo
            }
            if (w > k) {
                f.addClause(-lit);              // esa arista nunca cabe en el presupuesto
            } else {
                active.add(it);
            }
        }
        if (active.isEmpty() || k == 0) {
            return;
        }
        int m = active.size();
        int[][] s = new int[m][k + 1];          // s[i][c]: suma de los primeros i+1 >= c
        for (int i = 0; i < m; i++) {
            for (int c = 1; c <= k; c++) {
                s[i][c] = f.newVar();
            }
        }
        // Primer elemento
        int x0 = active.get(0)[0];
        int w0 = active.get(0)[1];
        for (int c = 1; c <= w0; c++) {
            f.addClause(-x0, s[0][c]);
        }
        for (int c = w0 + 1; c <= k; c++) {
            f.addClause(-s[0][c]);
        }
        // Elementos siguientes
        for (int i = 1; i < m; i++) {
            int xi = active.get(i)[0];
            int wi = active.get(i)[1];
            for (int c = 1; c <= wi; c++) {
                f.addClause(-xi, s[i][c]);
            }
            for (int c = 1; c <= k; c++) {
                f.addClause(-s[i - 1][c], s[i][c]);
            }
            for (int c = 1; c <= k - wi; c++) {
                f.addClause(-xi, -s[i - 1][c], s[i][c + wi]);
            }
            for (int c = k - wi + 1; c <= k; c++) {
                f.addClause(-xi, -s[i - 1][c]);  // desbordaría el umbral K
            }
        }
    }
}
