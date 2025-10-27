package abstraccion;

import java.util.Arrays;
import java.util.List;

/**
 * Orquesta la ejecución de las pruebas de rendimiento para un conjunto de algoritmos de ordenamiento.
 * <p>Esta clase es el motor principal del análisis. Es responsable de:
 * <ul>
 * <li>Preparar los "escalones" de tamaños de arreglos (basado en 'n').</li>
 * <li>Realizar un "calentamiento" (warm-up) de la JVM para activar el JIT.</li>
 * <li>Ejecutar cada {@link EstrategiaOrdenamiento} múltiples veces por cada tamaño.</li>
 * <li>Calcular el tiempo promedio de ejecución y mostrarlo en consola.</li>
 * <li>Delegar el almacenamiento de cada {@link Resultado} al {@link GestorResultados}.</li>
 * </ul>
 *
 * @see EstrategiaOrdenamiento
 * @see GestorResultados
 * @see Resultado
 */
public class AnalizadorEficiencia 
    {

        private final List<EstrategiaOrdenamiento> algoritmos = Arrays.asList
            (
                new BubbleSort(),
                new InsertionSort(), 
                new SelectionSort(),
                new MergeSort(),
                new QuickSort()
            );

        // Número de repeticiones por tamaño
        private static final int REPETICIONES = 50;

        // Tamaños que se usarán
        private final int[] tamanios;

        /**
         * Constructor que genera los tamaños basado en 'n' (10 segmentos).
         * @param n Tamaño máximo.
         */
        public AnalizadorEficiencia(int n) 
            {
                int segmentos;

                if (n <= 0) 
                    {
                        // 1. Red de seguridad
                        n = 1;
                        segmentos = 1;
                    } 
                else if (n < 1000) 
                    {
                        // 2. Ajuste para n pequeño
                        segmentos = Math.max(1, n / 100);
                    } 
                else 
                    {
                        // 3. Caso estándar
                        segmentos = 10;
                    }

                int incremento = n / segmentos;

                this.tamanios = new int[segmentos];
                for (int i = 0; i < segmentos; i++) 
                    {
                        this.tamanios[i] = (i + 1) * incremento;
                    }
            }


        /**
         * Ejecuta el análisis para los tamaños definidos.
         */
        public void ejecutarAnalisis() 
            {
                GestorResultados gestor = GestorResultados.getInstancia();

                // Warm-up
                int warmUpSize = Math.max(1000, (tamanios.length > 0 ? tamanios[0] : 1000));
                Arreglo warm = new Arreglo(warmUpSize);

                for (EstrategiaOrdenamiento estrategia : algoritmos) 
                    {
                        int[] copia = Arrays.copyOf(warm.getDatos(), warm.getDatos().length);
                        estrategia.ordenar(copia); // sin medir
                    }
                
                // Itera sobre los tamaños generados
                for (int n : tamanios) 
                    {
                        // Evitar tamaños 0 si 'n' era muy pequeño
                        if (n == 0) continue; 
                        
                        System.out.println("\nTamaño del arreglo: " + n);

                        // Generar un arreglo base (se reutiliza la misma muestra para todas las repeticiones)
                        Arreglo base = new Arreglo(n);
                        int[] datosBase = base.getDatos();

                        for (EstrategiaOrdenamiento algoritmo : algoritmos) 
                            {
                                String nombre = algoritmo.getNombre();

                                long totalNs = 0L;

                                for (int r = 0; r < REPETICIONES; r++) 
                                    {
                                        int[] copia = Arrays.copyOf(datosBase, datosBase.length);

                                        long inicio = System.nanoTime();
                                        algoritmo.ordenar(copia);
                                        long fin = System.nanoTime();

                                        long deltaNs = fin - inicio;
                                        totalNs += deltaNs;
                                    }

                                // Promedio en nanosegundos (double para evitar truncamiento)
                                double promedioNs = totalNs / (double) REPETICIONES;
                                double promedioMs = promedioNs / 1_000_000.0; // para almacenar/CSV

                                if (promedioNs < 1_000_000.0) 
                                    {
                                        double promedioUs = promedioNs / 1000.0;
                                        System.out.printf("   %-12s → %8.3f µs (promedio de %d ejecuciones)%n",
                                                nombre, promedioUs, REPETICIONES);
                                    } 
                                else 
                                    {
                                        System.out.printf("   %-12s → %8.3f ms (promedio de %d ejecuciones)%n",
                                            nombre, promedioMs, REPETICIONES);
                                    }

                                // Guardar en gestor (se mantiene)
                                gestor.agregarResultado(new Resultado(nombre, n, promedioMs));
                            }
                    }

                System.out.println("\nAnálisis completado. Resultados en memoria.");

            }
    }

