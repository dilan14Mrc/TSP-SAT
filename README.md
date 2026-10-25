# TSP ≤p SAT — Reducción polinomial del Problema del Viajante a SAT

Programa en Java que recibe una instancia del TSP (versión de decisión: ¿existe un
ciclo que visite todas las ciudades una vez y cueste ≤ K?), la **traduce a una fórmula
CNF (DIMACS)** y la entrega al SAT-Solver **Sat4j**. El código **no resuelve el TSP**:
solo traduce y luego interpreta el resultado del solver.

## Requisitos

* Java 11 o superior (JDK)
* Sat4j 2.3.5 (`lib/org.ow2.sat4j.core-2.3.5.jar`, ya incluido en el repositorio)

## Estructura

```
TSP-SAT/
├── src/tspsat/
│   ├── Main.java                    # Interfaz de consola y orquestación de las 3 fases
│   ├── TspInstance.java             # Datos de la instancia (N, distancias, K)
│   ├── InstanceReader.java          # Lectura de grafos desde archivo de texto
│   ├── RandomInstanceGenerator.java # Instancias aleatorias para pruebas
│   ├── TspToSatReducer.java         # Fase 1: reducción TSP -> CNF
│   ├── CnfFormula.java              # Representación de la fórmula CNF
│   ├── DimacsWriter.java            # Escritura del archivo .cnf
│   ├── Sat4jRunner.java             # Fase 2: integración con Sat4j
│   ├── SolverResult.java            # Resultado del solver
│   └── RouteDecoder.java            # Fase 3: reconstrucción de la ruta
├── lib/                             # Sat4j
└── instancias/                      # Ejemplos de entrada
```

## Formato del archivo de entrada

```
# comentario
N K
matriz de distancias NxN (una fila por línea)
```

Ejemplo (`instancias/n4\_sat.txt`):

```
4 80
0 10 15 20
10 0 35 25
15 35 0 30
20 25 30 0
```

## Compilar y ejecutar

### Desde Eclipse

1. `File > Import > General > Existing Projects into Workspace` y elegir la carpeta `TSP-SAT`.
2. Clic derecho en `Main.java` > `Run As > Run Configurations...` > pestaña **Arguments**.
3. En *Program arguments* escribir, por ejemplo: `instancias/n4\_sat.txt`
4. `Run`.

### Desde consola (Linux/macOS)

```bash
mkdir -p bin
javac -encoding UTF-8 -cp lib/org.ow2.sat4j.core-2.3.5.jar -d bin src/tspsat/\*.java
java -cp bin:lib/org.ow2.sat4j.core-2.3.5.jar tspsat.Main instancias/n4\_sat.txt
```

En Windows reemplazar `:` por `;` en el classpath.

## Ejemplos de uso

```bash
# Instancia desde archivo
java -cp bin:lib/org.ow2.sat4j.core-2.3.5.jar tspsat.Main instancias/n4\_sat.txt

# Cambiar K sin editar el archivo
java -cp ... tspsat.Main instancias/n4\_sat.txt -k 79

# Instancia aleatoria: N=10, K=40, semilla 1, límite de 60 s
java -cp ... tspsat.Main --random 10 40 1 -t 60

# Tabla de rendimiento: N = 3,4,10,11,12, K = 2\*N, semilla 1, límite 80 s por caso
java -cp ... tspsat.Main --bench 3,4,10,11,12 2 1 -t 80
```

Opciones: `-k K` (reemplaza el umbral), `-o archivo.cnf` (dónde guardar el CNF),
`-t segundos` (límite de tiempo del solver), `--sin-simetria` (no fija la ciudad 0 en el paso 0).

## Salida esperada

* Si es satisfacible: imprime la ruta y su costo real.
* Si no: `NO existe ninguna ruta con costo menor o igual a K`.

## Modelo (resumen)

* `X(i,t)`: la ciudad `i` se visita en el paso `t`.
* `Y(i,j,t)`: en el paso `t` se viaja de `i` a `j` (el último paso regresa al origen).
* Visita única, ocupación única y restricción de costo total ≤ K (contador secuencial
ponderado). Detalle matemático en el informe técnico.

## Resultados de las pruebas
* 
* | N | K | Variables | Cláusulas | Traducción (ms) | Solver (ms) | Resultado |
* |---|---|-----------|-----------|-----------------|-------------|-----------|
* | 3 | 12 | 243 | 531 | 13 | 92 | SAT |
* | 4 | 80 | 3904 | 8775 | 26 | 113 | SAT |
* | 4 | 79 | 3856 | 8680 | 27 | 133 | UNSAT |
* | 10 | 40 | 37000 | 79095 | 79 | 534 | SAT |
* | 10 | 20 | 19000 | 43115 | 65 | 7064 | UNSAT |
* | 11 | 22 | 27951 | 62585 | 83 | 14212 | UNSAT |
* | 12 | 24 | 39744 | 88315 | 31 | 68545 | UNSAT |
* | 13 | 26 | 54925 | 121389 | 46 | 80088 | TIMEOUT (> 80 s) |
* 
* El punto de quiebre en este equipo está en N = 13 (límite de 80 s por caso).
* Análisis completo en el informe técnico.

