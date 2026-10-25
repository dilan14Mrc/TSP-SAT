package tspsat;

/**
 * Instancia del Problema del Viajante (TSP) en su versión de decisión.
 *
 * <p>Contiene el número de ciudades {@code n}, la matriz de distancias
 * {@code dist[i][j]} (puede ser asimétrica) y el costo máximo permitido
 * {@code k}. La pregunta de decisión es: ¿existe un recorrido que visite
 * todas las ciudades exactamente una vez, regrese al origen y cuyo costo
 * total sea menor o igual a {@code k}?</p>
 */
public final class TspInstance {

    private final int n;
    private final int[][] dist;
    private final int k;

    /**
     * Crea una instancia validando sus datos.
     *
     * @param dist matriz cuadrada de distancias no negativas
     * @param k    umbral de costo máximo (no negativo)
     * @throws IllegalArgumentException si la matriz no es cuadrada, tiene
     *                                  menos de 2 ciudades o hay valores negativos
     */
    public TspInstance(int[][] dist, int k) {
        if (dist == null || dist.length < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 ciudades.");
        }
        if (k < 0) {
            throw new IllegalArgumentException("K no puede ser negativo.");
        }
        this.n = dist.length;
        this.dist = new int[n][n];
        for (int i = 0; i < n; i++) {
            if (dist[i].length != n) {
                throw new IllegalArgumentException("La matriz de distancias debe ser cuadrada.");
            }
            for (int j = 0; j < n; j++) {
                if (dist[i][j] < 0) {
                    throw new IllegalArgumentException("Distancia negativa en (" + i + "," + j + ").");
                }
                this.dist[i][j] = dist[i][j];
            }
        }
        this.k = k;
    }

    /** @return número de ciudades N */
    public int size() {
        return n;
    }

    /** @return distancia de la ciudad {@code i} a la ciudad {@code j} */
    public int distance(int i, int j) {
        return dist[i][j];
    }

    /** @return umbral de costo máximo K */
    public int bound() {
        return k;
    }

    /** @return una nueva instancia idéntica pero con otro umbral K */
    public TspInstance withBound(int newK) {
        return new TspInstance(dist, newK);
    }

    /**
     * Calcula el costo real de un recorrido cerrado (incluye el regreso al origen).
     *
     * @param route ciudades en el orden visitado (longitud N)
     * @return costo total del ciclo
     */
    public int tourCost(int[] route) {
        int total = 0;
        for (int t = 0; t < route.length; t++) {
            total += dist[route[t]][route[(t + 1) % route.length]];
        }
        return total;
    }
}
