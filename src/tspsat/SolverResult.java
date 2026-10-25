package tspsat;

/** Resultado de ejecutar el SAT-Solver sobre un archivo CNF. */
public final class SolverResult {

    /** Estado final del solver. */
    public enum Status { SAT, UNSAT, TIMEOUT }

    private final Status status;
    private final int[] model;
    private final long millis;

    public SolverResult(Status status, int[] model, long millis) {
        this.status = status;
        this.model = model;
        this.millis = millis;
    }

    /** @return SAT, UNSAT o TIMEOUT */
    public Status status() {
        return status;
    }

    /** @return modelo (literales verdaderos/falsos) o null si no es SAT */
    public int[] model() {
        return model;
    }

    /** @return tiempo que tardó el solver en milisegundos */
    public long millis() {
        return millis;
    }
}
