package tspsat;

import java.util.ArrayList;
import java.util.List;

/**
 * Fórmula booleana en Forma Normal Conjuntiva (CNF).
 *
 * <p>Una cláusula es un arreglo de enteros no nulos: el entero {@code v > 0}
 * representa la variable {@code v} y {@code -v} su negación (convención DIMACS).</p>
 */
public final class CnfFormula {

    private final List<int[]> clauses = new ArrayList<>();
    private int numVars;

    /**
     * Reserva una variable nueva.
     *
     * @return el número (desde 1) de la variable creada
     */
    public int newVar() {
        return ++numVars;
    }

    /**
     * Agrega una cláusula (disyunción de literales).
     *
     * @param literals literales de la cláusula
     */
    public void addClause(int... literals) {
        clauses.add(literals.clone());
    }

    /** @return cantidad de variables reservadas */
    public int numVars() {
        return numVars;
    }

    /** @return cantidad de cláusulas */
    public int numClauses() {
        return clauses.size();
    }

    /** @return lista de cláusulas */
    public List<int[]> clauses() {
        return clauses;
    }
}
