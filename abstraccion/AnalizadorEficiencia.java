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
        // Definimos los tres casos que vamos a probar
        private enum TipoCaso { Promedio, Mejor, Peor }

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

                // Warm-up para el sistema
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
                        if (n == 0) continue; 
                        
                        System.out.println("\nTamaño del arreglo: " + n);

                        // Generar un arreglo base (lo creamos una vez por tamaño)
                        Arreglo base = new Arreglo(n);

                        for (EstrategiaOrdenamiento algoritmo : algoritmos) 
                            {
                                String nombre = algoritmo.getNombre();

                                for (TipoCaso caso : TipoCaso.values())
                                    {
                                        // 1. Obtenemos el arreglo correcto para cdcada caso
                                        int[] datosBaseCaso;
                                        String nombreCaso = caso.toString();

                                        switch (caso)
                                            {
                                                case Mejor:
                                                    datosBaseCaso = base.getDatosOrdenados();
                                                    break;
                                                case Peor:
                                                    datosBaseCaso = base.getDatosOrdenadosInversos();
                                                    break;
                                                case Promedio:
                                                default:
                                                    datosBaseCaso = base.getDatos();
                                                    break;
                                            }

                                        // 2. Ejecutamos las repeticiones
                                        long totalNs = 0L;
                                        for (int r = 0; r < REPETICIONES; r++) 
                                            {
                                                // Usamos la copia del caso correspondiente
                                                int[] copia = Arrays.copyOf(datosBaseCaso, datosBaseCaso.length);

                                                long inicio = System.nanoTime();
                                                algoritmo.ordenar(copia);
                                                long fin = System.nanoTime();

                                                long deltaNs = fin - inicio;
                                                totalNs += deltaNs;
                                            }

                                        // 3. Calculamos promedios
                                        double promedioNs = totalNs / (double) REPETICIONES;
                                        double promedioMs = promedioNs / 1_000_000.0;

                                        // 4. Imprimimos en consola
                                        if (promedioNs < 1_000_000.0) 
                                            {
                                                double promedioUs = promedioNs / 1000.0;
                                                System.out.printf("   %-12s (%-8s) → %8.3f µs (promedio de %d ejecuciones)%n",
                                                        nombre, nombreCaso, promedioUs, REPETICIONES);
                                            } 
                                        else 
                                            {
                                                System.out.printf("   %-12s (%-8s) → %8.3f ms (promedio de %d ejecuciones)%n",
                                                    nombre, nombreCaso, promedioMs, REPETICIONES);
                                            }

                                        // 5. Guardar en gestor
                                        gestor.agregarResultado(new Resultado(nombre, n, promedioMs, nombreCaso));
                                    
                                    } // Fin delciclo de casos
                            } // Fin del bucle de algoritmoss
                    } // Fin del bucle de los tamaños

                System.out.println("\nAnálisis completado (Mejor, Peor y Promedio). Resultados en memoria.");

            }
    }

