package tspsat;

/** Reconstruye la ruta a partir del modelo booleano entregado por el solver. */
public final class RouteDecoder {

    private RouteDecoder() {
    }

    /**
     * Interpreta las variables X(i,t) verdaderas del modelo.
     *
     * @param model modelo del solver (literales positivos = verdadero)
     * @param n     número de ciudades
     * @return arreglo {@code route} donde {@code route[t]} es la ciudad visitada en el paso t
     * @throws IllegalStateException si el modelo no describe una permutación válida
     */
    public static int[] decode(int[] model, int n) {
        int[] route = new int[n];
        java.util.Arrays.fill(route, -1);
        for (int lit : model) {
            if (lit > 0 && lit <= n * n) {
                int i = (lit - 1) / n;
                int t = (lit - 1) % n;
                if (route[t] != -1) {
                    throw new IllegalStateException("Dos ciudades en el paso " + t);
                }
                route[t] = i;
            }
        }
        for (int t = 0; t < n; t++) {
            if (route[t] == -1) {
                throw new IllegalStateException("Paso " + t + " sin ciudad.");
            }
        }
        return route;
    }

    /**
     * Formatea la ruta como texto, cerrando el ciclo.
     * Las ciudades se muestran numeradas desde 0.
     */
    public static String format(int[] route) {
        StringBuilder sb = new StringBuilder();
        for (int city : route) {
            sb.append(city).append(" -> ");
        }
        sb.append(route[0]);
        return sb.toString();
    }
}
