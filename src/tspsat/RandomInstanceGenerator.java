package tspsat;

import java.util.Random;

/**
 * Genera instancias aleatorias simétricas para las pruebas de rendimiento.
 * No resuelve el TSP: solo crea datos de entrada.
 */
public final class RandomInstanceGenerator {

    private RandomInstanceGenerator() {
    }

    /**
     * @param n       número de ciudades
     * @param k       umbral de costo máximo
     * @param seed    semilla (misma semilla = misma instancia, reproducible)
     * @param maxDist distancia máxima entre dos ciudades (las distancias van de 1 a maxDist)
     * @return instancia con distancias simétricas y diagonal en cero
     */
    public static TspInstance generate(int n, int k, long seed, int maxDist) {
        Random rnd = new Random(seed);
        int[][] d = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int w = 1 + rnd.nextInt(maxDist);
                d[i][j] = w;
                d[j][i] = w;
            }
        }
        return new TspInstance(d, k);
    }
}
